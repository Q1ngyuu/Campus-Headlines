package com.campus.headlines.service;

import com.campus.headlines.vo.FavoriteVO;

import java.util.List;

/**
 * 收藏业务接口。
 */
public interface FavoriteService {

    /**
     * 切换收藏状态：已收藏则取消，未收藏则收藏。
     *
     * <p>整个“先查后写”过程在一个事务里完成；并发下的重复插入由唯一索引
     * {@code uk_favorite_user_article(user_id, article_id)} 兜底，
     * 捕获后按“已收藏”处理，不给前端抛 500。</p>
     *
     * @param userId    当前用户
     * @param articleId 文章ID
     * @return 操作后的状态：true 已收藏，false 已取消
     */
    boolean toggle(Long userId, Long articleId);

    /**
     * 我的收藏：联表查询文章信息，只返回已通过（status=2）的文章。
     */
    List<FavoriteVO> listByUser(Long userId);
}
