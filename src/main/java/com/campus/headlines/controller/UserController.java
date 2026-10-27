package com.campus.headlines.controller;

import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.dto.LoginDTO;
import com.campus.headlines.dto.PasswordDTO;
import com.campus.headlines.dto.ProfileDTO;
import com.campus.headlines.dto.RegisterDTO;
import com.campus.headlines.interceptor.LoginInterceptor;
import com.campus.headlines.service.UserService;
import com.campus.headlines.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 用户接口：注册、登录、当前用户、注销。
 *
 * <p>全部返回 {@link Result}，前端只判断 {@code code}。
 * 登录态存放在 HttpSession，key 为 {@link LoginInterceptor#SESSION_USER}（即 "loginUser"），
 * 便于后续的 {@code LoginInterceptor} 统一校验。</p>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 注册。
     *
     * <pre>
     * POST /user/register
     * Content-Type: application/json
     * {"username":"zhangsan","password":"123456","nickname":"张三"}
     * </pre>
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterDTO dto) {
        return Result.success("注册成功", userService.register(dto));
    }

    /**
     * 登录。成功后把用户信息写入 Session，前端后续请求带 Cookie 即可维持登录态。
     *
     * <pre>
     * POST /user/login
     * Content-Type: application/json
     * {"username":"zhangsan","password":"123456"}
     * </pre>
     */
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginDTO dto, HttpSession session) {
        UserVO user = userService.login(dto);
        session.setAttribute(LoginInterceptor.SESSION_USER, user);
        return Result.success("登录成功", user);
    }

    /**
     * 当前登录用户信息。
     *
     * <p>直接从 Session 取，不回查数据库；若需要拿到最新昵称/头像，改成
     * {@code userService.getById(vo.getId())} 即可。</p>
     */
    @GetMapping("/info")
    public Result<UserVO> info(HttpSession session) {
        Object current = session.getAttribute(LoginInterceptor.SESSION_USER);
        if (!(current instanceof UserVO)) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        return Result.success((UserVO) current);
    }

    /**
     * 注销：销毁整个 Session，登录态一并清除。
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success("已退出登录", null);
    }
    /**
     * 个人资料页的数据（从库里取最新值，而不是 Session 快照）。
     */
    @GetMapping("/profile")
    public Result<UserVO> profile(HttpSession session) {
        Long userId = com.campus.headlines.util.SessionUtil.requireUserId(session);
        UserVO user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 修改个人资料（昵称、头像）。
     *
     * <p>改完顺手刷新 Session 里的副本，否则顶栏还显示旧昵称。</p>
     */
    @PostMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody ProfileDTO dto, HttpSession session) {
        Long userId = com.campus.headlines.util.SessionUtil.requireUserId(session);
        UserVO updated = userService.updateProfile(userId, dto.getNickname(), dto.getAvatar());
        session.setAttribute(LoginInterceptor.SESSION_USER, updated);
        return Result.success("资料已更新", updated);
    }

    /**
     * 修改自己的密码：需要提供原密码。
     */
    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordDTO dto, HttpSession session) {
        Long userId = com.campus.headlines.util.SessionUtil.requireUserId(session);
        userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码已修改，下次登录请使用新密码", null);
    }
}