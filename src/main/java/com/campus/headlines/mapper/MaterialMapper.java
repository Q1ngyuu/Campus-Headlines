package com.campus.headlines.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.headlines.entity.Material;

/**
 * 素材 Mapper。
 *
 * <p>按上传者查询、按类型（image/video）筛选都可以用 {@code LambdaQueryWrapper} 完成。
 * 删除素材时注意：article_material 上的外键为 ON DELETE CASCADE，会连带清理关联记录。</p>
 */
public interface MaterialMapper extends BaseMapper<Material> {
}
