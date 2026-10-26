package com.campus.headlines.service;

import com.campus.headlines.entity.Category;

import java.util.List;

/**
 * 栏目业务接口。
 */
public interface CategoryService {

    /**
     * 查询所有启用中的栏目，按 sort_order 升序。
     */
    List<Category> listEnabled();

    /**
     * 查询全部栏目（含停用），供后台维护使用。
     */
    List<Category> listAll();
}
