package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.entity.Category;
import com.campus.headlines.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 栏目接口。
 *
 * <p>公开接口，供首页筛选与发布页下拉使用。</p>
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /** 启用中的栏目列表 */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.listEnabled());
    }
}
