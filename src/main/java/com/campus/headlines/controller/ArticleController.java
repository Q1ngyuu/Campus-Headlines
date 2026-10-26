package com.campus.headlines.controller;

import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.dto.ArticleDTO;
import com.campus.headlines.entity.Article;
import com.campus.headlines.service.ArticleService;
import com.campus.headlines.service.CategoryService;
import com.campus.headlines.util.SessionUtil;
import com.campus.headlines.vo.ArticleDetailVO;
import com.campus.headlines.vo.ArticleEditVO;
import com.campus.headlines.vo.ArticleListVO;
import com.campus.headlines.vo.PageVO;
import com.campus.headlines.vo.UserVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 资讯接口：投稿、浏览。
 *
 * <p>混合控制器：{@code /list}、{@code /my/list}、{@code /edit/{id}}、{@code /draft}、
 * {@code /submit/{id}} 返回 JSON；{@code /detail/{id}}、{@code /my}、{@code /publish}
 * 返回 JSP 页面。因此类上不能加 {@code @RestController}。</p>
 */
@Controller
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private CategoryService categoryService;

    // =====================================================================
    //  页面
    // =====================================================================

    /**
     * 发布页；带 {@code ?id=xxx} 时用于编辑草稿/被驳回的稿件。
     */
    @GetMapping("/publish")
    public String publishPage(@RequestParam(value = "id", required = false) Long id,
                              Model model, HttpSession session) {
        model.addAttribute("categories", categoryService.listEnabled());
        model.addAttribute("articleId", id);
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        return "article/publish";
    }

    /**
     * 详情页（公开）。每次访问浏览量 +1。
     *
     * <p>这是页面路由，查不到（或未通过审核）时不抛给全局异常处理器变成 JSON，
     * 而是带错误信息渲染同一个 JSP，给用户一个正常页面。</p>
     */
    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable("id") Long id, Model model, HttpSession session) {
        UserVO loginUser = SessionUtil.getLoginUser(session);
        model.addAttribute("loginUser", loginUser);
        try {
            ArticleDetailVO article = articleService.getDetail(id, loginUser == null ? null : loginUser.getId());
            model.addAttribute("article", article);
        } catch (BusinessException e) {
            model.addAttribute("errorMsg", e.getMessage());
        }
        return "article/detail";
    }

    /**
     * 我的文章页（需登录，数据由 {@code /article/my/list} 异步加载）。
     */
    @GetMapping("/my")
    public String myPage(Model model, HttpSession session) {
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        return "article/my";
    }

    // =====================================================================
    //  JSON 接口
    // =====================================================================

    /**
     * 首页公开列表：只出已通过的文章，支持栏目与关键词筛选。
     *
     * <pre>
     * GET /article/list?categoryId=1&keyword=运动会&page=1&size=10
     * </pre>
     */
    @GetMapping("/list")
    @ResponseBody
    public Result<PageVO<ArticleListVO>> list(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        return Result.success(articleService.listPublished(categoryId, keyword, page, size));
    }

    /**
     * 动态条件查询（演示 MyBatis 手写动态 SQL）。
     *
     * <pre>
     * GET /article/search?categoryId=1&keyword=图书馆&status=2
     * </pre>
     *
     * <p>安全约束：非管理员无论传什么 status，都会被强制查“已通过”，
     * 否则这个接口就成了草稿/待审核内容的越权入口。</p>
     */
    @GetMapping("/search")
    @ResponseBody
    public Result<List<ArticleListVO>> search(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            HttpSession session) {
        UserVO loginUser = SessionUtil.getLoginUser(session);
        boolean isAdmin = loginUser != null && loginUser.getRole() != null && loginUser.getRole() == 1;
        if (!isAdmin) {
            status = Article.STATUS_APPROVED;
        }
        return Result.success(articleService.searchArticles(categoryId, keyword, status));
    }

    /**
     * 我的文章列表，可按状态筛选。
     *
     * <pre>
     * GET /article/my/list?status=0&page=1&size=10
     * </pre>
     */
    @GetMapping("/my/list")
    @ResponseBody
    public Result<PageVO<ArticleListVO>> myList(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size,
            HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        return Result.success(articleService.listMyArticles(userId, status, page, size));
    }

    /**
     * 编辑回填：取自己的草稿/被驳回稿件。
     */
    @GetMapping("/edit/{id}")
    @ResponseBody
    public Result<ArticleEditVO> editData(@PathVariable("id") Long id, HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        return Result.success(articleService.getForEdit(id, userId));
    }

    /**
     * 保存草稿（新建或更新）。
     */
    @PostMapping("/draft")
    @ResponseBody
    public Result<Long> saveDraft(@RequestBody ArticleDTO dto, HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        Long id = articleService.saveDraft(dto, userId);
        return Result.success("草稿已保存", id);
    }

    /**
     * 提交审核：草稿(0) → 待审核(1)。
     */
    @PostMapping("/submit/{id}")
    @ResponseBody
    public Result<Void> submit(@PathVariable("id") Long id, HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        articleService.submitForReview(id, userId);
        return Result.success("已提交审核，等待管理员处理", null);
    }
}
