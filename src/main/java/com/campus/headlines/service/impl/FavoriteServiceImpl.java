package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.headlines.common.BusinessException;
import com.campus.headlines.entity.Article;
import com.campus.headlines.entity.Favorite;
import com.campus.headlines.mapper.ArticleMapper;
import com.campus.headlines.mapper.FavoriteMapper;
import com.campus.headlines.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 收藏业务实现。
 *
 * <p>只允许收藏已通过审核(2)的资讯；唯一索引 (user_id, article_id) 兜底，
 * 并发重复插入时按“已收藏”处理，不把异常抛给用户。</p>
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteServiceImpl.class);

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long articleId, Long userId) {
        if (userId == null) {
            throw new BusinessException(com.campus.headlines.common.Result.CODE_UNAUTHORIZED, "请先登录");
        }
        if (articleId == null) {
            throw new BusinessException("资讯ID不能为空");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null
                || !Integer.valueOf(Article.STATUS_APPROVED).equals(article.getStatus())) {
            throw new BusinessException(com.campus.headlines.common.Result.CODE_NOT_FOUND,
                    "资讯不存在或未通过审核");
        }

        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId));

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            log.info("取消收藏: userId={}, articleId={}", userId, articleId);
            return false;
        }

        try {
            favoriteMapper.insert(new Favorite(userId, articleId));
        } catch (DuplicateKeyException e) {
            // 并发下已被另一请求插入，视为已收藏
            log.info("重复收藏被忽略: userId={}, articleId={}", userId, articleId);
            return true;
        }
        log.info("收藏成功: userId={}, articleId={}", userId, articleId);
        return true;
    }
}
