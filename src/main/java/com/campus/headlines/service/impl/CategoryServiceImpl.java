package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.dto.CategoryDTO;
import com.campus.headlines.entity.Article;
import com.campus.headlines.entity.Category;
import com.campus.headlines.mapper.ArticleMapper;
import com.campus.headlines.mapper.CategoryMapper;
import com.campus.headlines.service.CategoryService;
import com.campus.headlines.util.AdminGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    /** 状态：启用 */
    private static final int STATUS_ENABLED = 1;

    /** 栏目名最大长度，与 varchar(50) 对齐 */
    private static final int NAME_MAX = 50;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private AdminGuard adminGuard;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category save(CategoryDTO dto, Long adminUserId) {
        adminGuard.require(adminUserId, "维护栏目");

        if (dto == null) {
            throw new BusinessException("参数不能为空");
        }
        String name = dto.getName() == null ? "" : dto.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("栏目名称不能为空");
        }
        if (name.length() > NAME_MAX) {
            throw new BusinessException("栏目名称不能超过 " + NAME_MAX + " 个字符");
        }
        int status = dto.getStatus() == null ? STATUS_ENABLED : dto.getStatus();
        if (status != 0 && status != STATUS_ENABLED) {
            throw new BusinessException("栏目状态不合法");
        }
        int sortOrder = dto.getSortOrder() == null ? 0 : dto.getSortOrder();

        // 名称唯一：同名栏目会让前台筛选出现两个一模一样的分组
        Long sameName = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name)
                .ne(dto.getId() != null, Category::getId, dto.getId()));
        if (sameName != null && sameName > 0) {
            throw new BusinessException(Result.CODE_CONFLICT, "已存在同名栏目：" + name);
        }

        Category category;
        if (dto.getId() == null) {
            category = new Category();
            category.setName(name);
            category.setSortOrder(sortOrder);
            category.setStatus(status);
            categoryMapper.insert(category);
        } else {
            category = categoryMapper.selectById(dto.getId());
            if (category == null) {
                throw new BusinessException(Result.CODE_NOT_FOUND, "栏目不存在");
            }
            category.setName(name);
            category.setSortOrder(sortOrder);
            category.setStatus(status);
            categoryMapper.updateById(category);
        }
        log.info("栏目保存成功: id={}, name={}, adminId={}", category.getId(), name, adminUserId);
        return categoryMapper.selectById(category.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long adminUserId) {
        adminGuard.require(adminUserId, "删除栏目");

        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "栏目不存在");
        }
        // 还有文章挂在这个栏目下就先拦住，避免文章被静默改成“未分类”
        Long used = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
        if (used != null && used > 0) {
            throw new BusinessException("该栏目下还有 " + used + " 篇文章，请先转移后再删除");
        }
        categoryMapper.deleteById(id);
        log.info("栏目删除成功: id={}, name={}, adminId={}", id, category.getName(), adminUserId);
    }
}
