package com.campus.headlines.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.headlines.entity.User;

/**
 * 用户表 Mapper。
 *
 * <p>继承 {@link BaseMapper} 即获得单表 CRUD、条件构造器查询与分页能力，
 * 常规需求无需写 XML。复杂 SQL 请放在 {@code resources/mapper/UserMapper.xml}，
 * 且 {@code namespace} 必须与本接口全限定名一致。</p>
 */
public interface UserMapper extends BaseMapper<User> {
}
