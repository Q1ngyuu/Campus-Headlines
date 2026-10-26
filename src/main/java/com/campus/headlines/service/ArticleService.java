package com.campus.headlines.service;

import com.campus.headlines.dto.ArticleDTO;
import com.campus.headlines.vo.ArticleDetailVO;
import com.campus.headlines.vo.ArticleEditVO;
import com.campus.headlines.vo.ArticleListVO;
import com.campus.headlines.vo.PageVO;

import java.util.List;

/**
 * 资讯业务接口：投稿、审核、浏览。
 *
 * <p><b>状态机是整个模块的核心</b>，只允许以下流转：</p>
 * <pre>
 *   0 草稿 ──submitForReview──> 1 待审核 ──audit(pass=1)──> 2 已通过
 *                                     └──audit(pass=0)──> 3 已驳回
 *   3 已驳回 ──saveDraft（编辑）──> 0 草稿 ──submitForReview──> 1 待审核
 * </pre>
 *
 * <p>所有状态变更都用「带 where status=旧状态」的条件更新，
 * 靠数据库行锁保证并发下不会重复审核 / 跳过审核。</p>
 */
public interface ArticleService {

    /**
     * 保存草稿。
     *
     * <p>新建时插入 status=0；更新时要求文章属于当前用户且状态为
     * <b>草稿(0) 或 已驳回(3)</b>。已驳回的稿件编辑后状态回到 0，
     * 这样作者改完再走一次提交流程。</p>
     *
     * @return 资讯 id
     */
    Long saveDraft(ArticleDTO dto, Long userId);

    /**
     * 提交审核：0 → 1。
     *
     * <p>仅作者本人、且当前状态为草稿(0)时可提交。
     * 已驳回(3)的稿件需要先 saveDraft（编辑保存回 0）再提交。</p>
     */
    void submitForReview(Long articleId, Long userId);

    /**
     * 管理员审核。
     *
     * @param pass         1-通过，0-驳回
     * @param rejectReason 驳回原因，pass=0 时必填
     * @param adminUserId  操作人，必须 role=1
     */
    void audit(Long articleId, Integer pass, String rejectReason, Long adminUserId);

    /**
     * 首页公开列表：只出 status=2 的文章。
     *
     * <p>动态条件：{@code categoryId} 可为 null（不限栏目），
     * {@code keyword} 可为空（不搜索标题与正文）。
     * 排序：置顶优先，其次按发布时间倒序。</p>
     */
    PageVO<ArticleListVO> listPublished(Long categoryId, String keyword, long page, long size);

    /**
     * 资讯详情（仅 status=2 可见），并把 view_count 原子 +1。
     *
     * @param currentUserId 当前登录用户，用于判断是否已收藏；未登录传 null
     */
    ArticleDetailVO getDetail(Long articleId, Long currentUserId);

    /**
     * 我的文章：按作者查询，可按状态筛选（status 为 null 表示全部）。
     */
    PageVO<ArticleListVO> listMyArticles(Long userId, Integer status, long page, long size);

    /**
     * 待审核列表（管理员用），按提交时间正序，先来先审。
     */
    PageVO<ArticleListVO> listPendingAudit(long page, long size);

    /**
     * 动态条件查询（演示 MyBatis 手写动态 SQL）。
     *
     * <p>与 {@link #listPublished} 的区别：这里走的是
     * {@code resources/mapper/ArticleMapper.xml} 里的 {@code <where>} + {@code <if>} 写法，
     * 不分页、返回全量（服务层截断到 50 条）。三个条件都可缺省。</p>
     */
    List<ArticleListVO> searchArticles(Long categoryId, String keyword, Integer status);

    /**
     * 编辑回填：取自己的草稿/被驳回稿件。
     */
    ArticleEditVO getForEdit(Long articleId, Long userId);
}
