package com.campus.headlines.controller;

import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.dto.LoginDTO;
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
}
