package com.campus.headlines.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.headlines.entity.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资讯 Mapper。
 *
 * <p>单表 CRUD 由 {@link BaseMapper} 提供，首页分页用 {@code LambdaQueryWrapper} 拼装；
 * 下面这个 {@code searchArticles} 是<b>手写动态 SQL</b> 的示例，
 * SQL 在 {@code resources/mapper/ArticleMapper.xml} 里用 {@code <where>} + {@code <if>} 实现。</p>
 */
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 动态条件查询资讯（不过滤分页，调用方自行控制数量）。
     *
     * <p>三个条件均为可选，全部为空时等价于全表查询（按置顶、发布时间排序）。</p>
     *
     * @param categoryId 栏目ID，null 表示不限
     * @param keyword    关键词，空表示不搜索；命中标题或正文即可
     * @param status     状态，null 表示不限；0草稿 1待审核 2已通过 3已驳回
     * @return 资讯列表，按 is_top 降序、publish_time 降序
     */
    List<Article> searchArticles(@Param("categoryId") Long categoryId,
                                 @Param("keyword") String keyword,
                                 @Param("status") Integer status);
}
