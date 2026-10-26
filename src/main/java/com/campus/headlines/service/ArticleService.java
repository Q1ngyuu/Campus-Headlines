package com.campus.headlines.service;

import com.campus.headlines.dto.ArticleSaveDTO;
import com.campus.headlines.entity.Article;

import java.util.List;

/**
 * 资讯业务接口（当前阶段只覆盖“新建/编辑并保存草稿或提交审核”）。
 */
public interface ArticleService {

    /**
     * 保存资讯。
     *
     * @param dto    资讯内容，{@code id} 为空表示新建
     * @param userId 当前用户 id（作者）
     * @param status 目标状态：0-草稿，1-待审核
     * @return 资讯 id
     */
    Long save(ArticleSaveDTO dto, Long userId, int status);

    /**
     * 查询某作者的全部资讯（含草稿），按更新时间倒序，供“我的投稿”使用。
     */
    List<Article> listByAuthor(Long userId);

    /**
     * 按 id 查询，仅供内部使用。
     */
    Article getById(Long id);
}
