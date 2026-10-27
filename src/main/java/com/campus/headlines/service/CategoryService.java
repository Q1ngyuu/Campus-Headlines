package com.campus.headlines.service;

import com.campus.headlines.dto.CategoryDTO;
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

    /**
     * 新增或修改栏目（仅管理员）。
     *
     * @param dto        栏目信息，id 为空表示新增
     * @param adminUserId 调用者，必须 role=1
     * @return 保存后的栏目
     */
    Category save(CategoryDTO dto, Long adminUserId);

    /**
     * 删除栏目（仅管理员）。
     *
     * <p>栏目下还有文章时拒绝删除，避免整批文章被静默改成“未分类”。</p>
     */
    void delete(Long id, Long adminUserId);
}
