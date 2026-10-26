package com.campus.headlines.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.headlines.entity.Article;

/**
 * 资讯 Mapper。
 *
 * <p>首页列表：按 {@code status=2}、{@code categoryId}、{@code isTop desc, publishTime desc}
 * 组合条件 + {@code Page} 分页，用 LambdaQueryWrapper 即可，不必写 XML。</p>
 *
 * <p>浏览量自增建议用 {@code UpdateWrapper.setSql("view_count = view_count + 1")}，
 * 避免先查后改带来的并发覆盖。</p>
 */
public interface ArticleMapper extends BaseMapper<Article> {
}
