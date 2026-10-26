package com.campus.headlines.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.headlines.entity.Category;

/**
 * 栏目分类 Mapper。
 *
 * <p>栏目数量少且变化不频繁，通常直接全量查询后缓存，不建议频繁分页。</p>
 */
public interface CategoryMapper extends BaseMapper<Category> {
}
