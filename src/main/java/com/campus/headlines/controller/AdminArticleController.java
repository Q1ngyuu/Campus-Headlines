package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.dto.ArticleDTO;
import com.campus.headlines.dto.AuditDTO;
import com.campus.headlines.dto.TopDTO;
import com.campus.headlines.service.ArticleService;
import com.campus.headlines.service.CategoryService;
import com.campus.headlines.util.SessionUtil;
import com.campus.headlines.vo.ArticleEditVO;
import com.campus.headlines.vo.ArticleListVO;
import com.campus.headlines.vo.MaterialVO;
import com.campus.headlines.vo.PageVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 管理员资讯接口：审核、置顶、查看全部、编辑、删除。
 *
 * <p>权限有两道：{@code AdminInterceptor} 拦 {@code /admin/**} 做粗筛，
 * Service 里再查一次用户 role 做最终校验——拦截器可能被误配，服务层才是最后防线。</p>
 */
@Controller
@RequestMapping("/admin/article")
public class AdminArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private CategoryService categoryService;

    /** 审核管理页 */
    @GetMapping("/page")
    public String auditPage(Model model, HttpSession session) {
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        return "admin/audit";
    }

    /**
     * 编辑页（管理员可编辑任意状态的文章）。
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable("id") Long id, Model model, HttpSession session) {
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        model.addAttribute("articleId", id);
        model.addAttribute("categories", categoryService.listEnabled());
        return "admin/edit";
    }

    /**
     * 编辑回填数据（JSON）。
     */
    @GetMapping("/edit-data/{id}")
    @ResponseBody
    public Result<ArticleEditVO> editData(@PathVariable("id") Long id, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        return Result.success(articleService.getForAdminEdit(id, adminUserId));
    }

    /**
     * 取该文章作者的素材列表，供管理员换封面。
     */
    @GetMapping("/materials/{id}")
    @ResponseBody
    public Result<List<MaterialVO>> materials(@PathVariable("id") Long id, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        return Result.success(articleService.listAuthorMaterials(id, adminUserId));
    }

    /**
     * 管理员列表：status 为空表示全部。
     *
     * <pre>
     * GET /admin/article/list?status=2&page=1&size=10
     * </pre>
     */
    @GetMapping("/list")
    @ResponseBody
    public Result<PageVO<ArticleListVO>> list(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        return Result.success(articleService.listForAdmin(status, page, size));
    }

    /**
     * 修改文章内容（标题 / 栏目 / 封面 / 正文），状态与发布时间不变。
     */
    @PostMapping("/update")
    @ResponseBody
    public Result<Void> update(@RequestBody ArticleDTO dto, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        articleService.updateByAdmin(dto, adminUserId);
        return Result.success("已保存修改", null);
    }

    /**
     * 删除文章（物理删除，级联清理关联与收藏）。
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public Result<Void> delete(@PathVariable("id") Long id, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        articleService.deleteByAdmin(id, adminUserId);
        return Result.success("已删除", null);
    }

    /**
     * 待审核列表（按提交时间正序）。
     *
     * <pre>
     * GET /admin/article/pending?page=1&size=10
     * </pre>
     */
    @GetMapping("/pending")
    @ResponseBody
    public Result<PageVO<ArticleListVO>> pending(
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        return Result.success(articleService.listPendingAudit(page, size));
    }

    /**
     * 已通过的资讯列表（管理员用，可在此置顶/取消置顶）。
     *
     * <pre>
     * GET /admin/article/published?page=1&size=10
     * </pre>
     */
    @GetMapping("/published")
    @ResponseBody
    public Result<PageVO<ArticleListVO>> published(
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        return Result.success(articleService.listPublishedForAdmin(page, size));
    }

    /**
     * 置顶 / 取消置顶。
     *
     * <pre>
     * POST /admin/article/top
     * {"articleId": 12, "isTop": 1}
     * </pre>
     */
    @PostMapping("/top")
    @ResponseBody
    public Result<Void> top(@RequestBody TopDTO dto, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        articleService.top(dto.getArticleId(), dto.getIsTop(), adminUserId);
        return Result.success(Integer.valueOf(1).equals(dto.getIsTop())
                ? "已置顶到首页顶部" : "已取消置顶", null);
    }

    /**
     * 审核：通过或驳回。
     *
     * <pre>
     * POST /admin/article/audit
     * {"articleId": 12, "pass": 1}
     * {"articleId": 13, "pass": 0, "rejectReason": "配图与内容不符"}
     * </pre>
     */
    @PostMapping("/audit")
    @ResponseBody
    public Result<Void> audit(@RequestBody AuditDTO dto, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        articleService.audit(dto.getArticleId(), dto.getPass(), dto.getRejectReason(), adminUserId);
        return Result.success(Integer.valueOf(1).equals(dto.getPass()) ? "已通过审核" : "已驳回", null);
    }
}
