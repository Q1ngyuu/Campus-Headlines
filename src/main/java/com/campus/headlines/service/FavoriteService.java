package com.campus.headlines.service;

/**
 * 收藏业务接口。
 */
public interface FavoriteService {

    /**
     * 切换收藏状态：已收藏则取消，未收藏则收藏。
     *
     * @return true 表示操作后处于“已收藏”
     */
    boolean toggle(Long articleId, Long userId);
}
