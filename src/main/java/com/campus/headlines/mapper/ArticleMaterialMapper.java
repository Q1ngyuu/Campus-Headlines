package com.campus.headlines.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.headlines.entity.ArticleMaterial;

/**
 * 资讯-素材关联 Mapper。
 *
 * <p>保存资讯时通常先删后插（按 article_id 清理再批量插入），
 * 或用唯一索引配合 {@code INSERT IGNORE} 做幂等。</p>
 */
public interface ArticleMaterialMapper extends BaseMapper<ArticleMaterial> {
}
