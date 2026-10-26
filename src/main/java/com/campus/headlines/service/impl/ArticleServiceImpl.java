package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.dto.ArticleSaveDTO;
import com.campus.headlines.entity.Article;
import com.campus.headlines.entity.ArticleMaterial;
import com.campus.headlines.entity.Material;
import com.campus.headlines.mapper.ArticleMapper;
import com.campus.headlines.mapper.ArticleMaterialMapper;
import com.campus.headlines.mapper.MaterialMapper;
import com.campus.headlines.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 资讯业务实现。
 *
 * <p>本阶段只做“保存草稿 / 提交审核”两件事，状态只可能是 0 或 1：
 * 学生没有权限直接把 status 写成 2（已通过），审核动作留到管理员模块。</p>
 *
 * <p>素材关联采用「先删后插」：每次保存都按前端提交的完整 materialIds 重建关联，
 * 逻辑简单、天然幂等，不必对比差集。</p>
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);

    /** 标题最大长度，与表结构 varchar(200) 对齐 */
    private static final int TITLE_MAX = 200;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleMaterialMapper articleMaterialMapper;

    @Resource
    private MaterialMapper materialMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(ArticleSaveDTO dto, Long userId, int status) {

        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        if (status != Article.STATUS_DRAFT && status != Article.STATUS_PENDING) {
            throw new BusinessException("非法的资讯状态");
        }

        // ---------- 1. 参数校验 ----------
        String title = dto.getTitle() == null ? "" : dto.getTitle().trim();
        if (title.isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
        if (title.length() > TITLE_MAX) {
            throw new BusinessException("标题不能超过 " + TITLE_MAX + " 个字符");
        }
        String content = dto.getContent() == null ? "" : dto.getContent();
        if (status == Article.STATUS_PENDING && content.trim().isEmpty()) {
            throw new BusinessException("提交审核前请填写正文");
        }

        // ---------- 2. 新建或编辑 ----------
        Article article;
        if (dto.getId() == null) {
            article = new Article();
            article.setUserId(userId);
            article.setViewCount(0);
            article.setIsTop(0);
        } else {
            article = articleMapper.selectById(dto.getId());
            if (article == null) {
                throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
            }
            if (!userId.equals(article.getUserId())) {
                throw new BusinessException(Result.CODE_FORBIDDEN, "只能编辑自己的资讯");
            }
            // 已通过审核的稿子不允许绕过审核直接改
            if (article.getStatus() != null && article.getStatus() == Article.STATUS_APPROVED) {
                throw new BusinessException("已通过的资讯不能直接修改，请联系管理员");
            }
        }

        article.setCategoryId(dto.getCategoryId());
        article.setTitle(title);
        article.setCoverImage(dto.getCoverImage());
        article.setContent(content);
        article.setStatus(status);

        if (dto.getId() == null) {
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
        }
        Long articleId = article.getId();

        // ---------- 3. 重建素材关联 ----------
        articleMaterialMapper.delete(
                new LambdaQueryWrapper<ArticleMaterial>().eq(ArticleMaterial::getArticleId, articleId));
        List<Long> materialIds = distinctIds(dto.getMaterialIds());
        if (!materialIds.isEmpty()) {
            // 只允许关联自己的素材，防止拿别人的素材 id 拼内容
            Long owned = materialMapper.selectCount(
                    new LambdaQueryWrapper<Material>()
                            .in(Material::getId, materialIds)
                            .eq(Material::getUserId, userId));
            if (owned == null || owned != materialIds.size()) {
                throw new BusinessException(Result.CODE_FORBIDDEN, "存在无权使用的素材，请刷新后重试");
            }
            for (Long materialId : materialIds) {
                articleMaterialMapper.insert(new ArticleMaterial(articleId, materialId));
            }
        }

        log.info("资讯保存成功: id={}, userId={}, status={}, 素材 {} 个",
                articleId, userId, status, materialIds.size());
        return articleId;
    }

    @Override
    public List<Article> listByAuthor(Long userId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        return articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getUserId, userId)
                        .orderByDesc(Article::getUpdateTime)
                        .orderByDesc(Article::getId));
    }

    @Override
    public Article getById(Long id) {
        return id == null ? null : articleMapper.selectById(id);
    }

    /** 去重并去掉 null */
    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> set = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null) {
                set.add(id);
            }
        }
        return new ArrayList<>(set);
    }
}
