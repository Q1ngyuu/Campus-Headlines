package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.entity.Article;
import com.campus.headlines.entity.Favorite;
import com.campus.headlines.mapper.ArticleMapper;
import com.campus.headlines.mapper.FavoriteMapper;
import com.campus.headlines.service.FavoriteService;
import com.campus.headlines.vo.FavoriteVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 收藏业务实现。
 *
 * <p>只允许收藏已通过审核(2)的文章——草稿和待审核的文章不该被传播。</p>
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
    public boolean toggle(Long userId, Long articleId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        if (articleId == null) {
            throw new BusinessException("文章ID不能为空");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null
                || !Integer.valueOf(Article.STATUS_APPROVED).equals(article.getStatus())) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "文章不存在或未通过审核");
        }

        // 先查：已收藏 → 取消
        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId));

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            log.info("取消收藏: userId={}, articleId={}", userId, articleId);
            return false;
        }

        // 后写：并发下两个请求可能同时走到这里，唯一索引会让第二个插入失败
        try {
            favoriteMapper.insert(new Favorite(userId, articleId));
        } catch (DuplicateKeyException e) {
            // 说明另一个线程刚刚插入了同一条，属于“已收藏”，不是错误
            log.info("并发重复收藏，按已收藏处理: userId={}, articleId={}", userId, articleId);
            return true;
        }
        log.info("收藏成功: userId={}, articleId={}", userId, articleId);
        return true;
    }

    @Override
    public List<FavoriteVO> listByUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        List<FavoriteVO> list = favoriteMapper.selectFavoriteArticles(userId);
        return list == null ? new ArrayList<>() : list;
    }
}
