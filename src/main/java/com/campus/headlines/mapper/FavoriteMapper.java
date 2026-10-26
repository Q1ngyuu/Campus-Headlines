package com.campus.headlines.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.headlines.entity.Favorite;
import com.campus.headlines.vo.FavoriteVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏 Mapper。
 *
 * <p>单表增删由 {@link BaseMapper} 提供；“我的收藏”列表需要联表文章与栏目，
 * SQL 写在 {@code resources/mapper/FavoriteMapper.xml}。</p>
 */
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 查询某用户收藏的文章（只返回 status=2 的已通过文章），按收藏时间倒序。
     *
     * @param userId 用户ID
     * @return 收藏项列表
     */
    List<FavoriteVO> selectFavoriteArticles(@Param("userId") Long userId);
}
