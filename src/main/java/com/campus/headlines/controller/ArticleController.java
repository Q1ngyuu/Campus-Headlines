package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.dto.ArticleSaveDTO;
import com.campus.headlines.entity.Article;
import com.campus.headlines.service.ArticleService;
import com.campus.headlines.service.CategoryService;
import com.campus.headlines.util.SessionUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 资讯接口（当前只实现发布页与保存草稿/提交审核）。
 *
 * <p>用 {@code @Controller} 而不是 {@code @RestController}：本类里既有返回 JSP 的页面方法，
 * 也有返回 JSON 的接口方法，前者不能加 {@code @ResponseBody}。</p>
 */
@Controller
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private CategoryService categoryService;

    /**
     * 发布页。需要登录（由 LoginInterceptor 拦截 /article/publish）。
     *
     * <p>栏目在后端一次性带进模型，避免页面再发一个请求。</p>
     */
    @GetMapping("/publish")
    public String publishPage(Model model, HttpSession session) {
        model.addAttribute("categories", categoryService.listEnabled());
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        return "article/publish";
    }

    /**
     * 提交审核：状态直接写 1（待审核），学生无权写 2。
     */
    @PostMapping("/publish")
    @ResponseBody
    public Result<Long> publish(@RequestBody ArticleSaveDTO dto, HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        Long articleId = articleService.save(dto, userId, Article.STATUS_PENDING);
        return Result.success("已提交审核，等待管理员处理", articleId);
    }

    /**
     * 保存草稿：状态写 0。
     */
    @PostMapping("/draft")
    @ResponseBody
    public Result<Long> draft(@RequestBody ArticleSaveDTO dto, HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        Long articleId = articleService.save(dto, userId, Article.STATUS_DRAFT);
        return Result.success("草稿已保存", articleId);
    }
}
