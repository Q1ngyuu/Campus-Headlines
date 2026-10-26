package com.campus.headlines.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.headlines.entity.Favorite;

/**
 * 收藏 Mapper。
 *
 * <p>“我的收藏”列表需要联表资讯信息，单表 BaseMapper 不够用，届时在
 * {@code resources/mapper/FavoriteMapper.xml} 里写联表查询并在此声明方法即可。</p>
 */
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
