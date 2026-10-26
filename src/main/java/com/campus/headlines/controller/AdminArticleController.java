package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.dto.AuditDTO;
import com.campus.headlines.service.ArticleService;
import com.campus.headlines.util.SessionUtil;
import com.campus.headlines.vo.ArticleListVO;
import com.campus.headlines.vo.PageVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 管理员资讯接口。
 *
 * <p>权限有两道：{@code AdminInterceptor} 拦 {@code /admin/**} 做粗筛，
 * Service 里再查一次用户 role 做最终校验——拦截器可能被误配，服务层才是最后防线。</p>
 */
@Controller
@RequestMapping("/admin/article")
public class AdminArticleController {

    @Resource
    private ArticleService articleService;

    /** 审核管理页 */
    @GetMapping("/page")
    public String auditPage(Model model, HttpSession session) {
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        return "admin/audit";
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
