package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.service.UserService;
import com.campus.headlines.util.SessionUtil;
import com.campus.headlines.vo.PageVO;
import com.campus.headlines.vo.UserVO;
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
import java.util.Map;

/**
 * 管理员用户管理：列表、启用/禁用、重置密码。
 */
@Controller
@RequestMapping("/admin/user")
public class AdminUserController {

    @Resource
    private UserService userService;

    /** 用户管理页 */
    @GetMapping("/page")
    public String page(Model model, HttpSession session) {
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        return "admin/user";
    }

    /**
     * 用户列表。
     *
     * <pre>
     * GET /admin/user/list?keyword=zhang&role=0&page=1&size=10
     * </pre>
     */
    @GetMapping("/list")
    @ResponseBody
    public Result<PageVO<UserVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) Integer role,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        return Result.success(userService.listForAdmin(keyword, role, page, size));
    }

    /**
     * 启用 / 禁用账号。
     *
     * <pre>
     * POST /admin/user/status  {"userId": 13, "status": 0}
     * </pre>
     */
    @PostMapping("/status")
    @ResponseBody
    public Result<Void> updateStatus(@RequestBody Map<String, Object> body, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        Long userId = toLong(body.get("userId"));
        Integer status = toInt(body.get("status"));
        userService.updateStatus(userId, status, adminUserId);
        return Result.success(Integer.valueOf(0).equals(status) ? "已禁用该账号" : "已恢复该账号", null);
    }

    /**
     * 重置密码。
     *
     * <pre>
     * POST /admin/user/password  {"userId": 13, "password": "newpass123"}
     * </pre>
     */
    @PostMapping("/password")
    @ResponseBody
    public Result<Void> resetPassword(@RequestBody Map<String, Object> body, HttpSession session) {
        Long adminUserId = SessionUtil.requireUserId(session);
        Long userId = toLong(body.get("userId"));
        Object password = body.get("password");
        userService.resetPassword(userId, password == null ? null : String.valueOf(password), adminUserId);
        return Result.success("密码已重置", null);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
