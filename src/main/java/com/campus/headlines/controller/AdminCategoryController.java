package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.dto.CategoryDTO;
import com.campus.headlines.entity.Category;
import com.campus.headlines.service.CategoryService;
import com.campus.headlines.util.SessionUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 管理员栏目管理。
 */
@Controller
@RequestMapping("/admin/category")
public class AdminCategoryController {

    @Resource
    private CategoryService categoryService;

    /** 栏目管理页 */
    @GetMapping("/page")
    public String page(Model model, HttpSession session) {
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        return "admin/category";
    }

    /** 全部栏目（含停用），后台列表用 */
    @GetMapping("/list")
    @ResponseBody
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    /**
     * 新增或修改。
     *
     * <pre>
     * POST /admin/category/save
     * {"name":"校园安全","sortOrder":50,"status":1}
     * {"id":5,"name":"校园安全","sortOrder":50,"status":0}
     * </pre>
     */
    @PostMapping("/save")
    @ResponseBody
    public Result<Category> save(@RequestBody CategoryDTO dto, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        return Result.success("已保存", categoryService.save(dto, adminUserId));
    }

    /** 删除（栏目下还有文章会被拒绝） */
    @DeleteMapping("/{id}")
    @ResponseBody
    public Result<Void> delete(@PathVariable("id") Long id, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        categoryService.delete(id, adminUserId);
        return Result.success("已删除", null);
    }
}
