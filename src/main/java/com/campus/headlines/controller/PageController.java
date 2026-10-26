package com.campus.headlines.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由：把 URL 映射到 {@code /WEB-INF/views/} 下的 JSP。
 *
 * <p>JSP 放在 WEB-INF 下，浏览器无法直接访问，必须经过 Controller 转发，
 * 这样后续要加“已登录则跳首页”之类的判断也很自然。</p>
 */
@Controller
public class PageController {

    /** 登录页 */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /** 注册页 */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}
