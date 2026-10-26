package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.headlines.entity.Category;
import com.campus.headlines.mapper.CategoryMapper;
import com.campus.headlines.service.CategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 栏目业务实现。
 *
 * <p>栏目数量少、变动不频繁，这里每次查库即可；若后续访问量上来，
 * 可加一层本地缓存（如 Caffeine）或在 Service 里做 @Cacheable。</p>
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    /** 状态：启用 */
    private static final int STATUS_ENABLED = 1;

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listEnabled() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, STATUS_ENABLED)
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId));
    }

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId));
    }
}
