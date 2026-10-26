package com.campus.headlines.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由：把 URL 映射到 {@code /WEB-INF/views/} 下的 JSP。
 *
 * <p>JSP 放在 WEB-INF 下，浏览器无法直接访问，必须经过 Controller 转发，
 * 这样后续要加“已登录则跳首页”之类的判断也很自然。</p>
 *
 * <p>注意：这里只负责“打开页面”，页面的数据一律通过 AJAX 调 REST 接口拿，
 * 避免页面方法里堆一堆 model 拼装逻辑（发布页的栏目是例外，图省一次请求）。</p>
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

    /** 素材管理页（需要登录） */
    @GetMapping("/material")
    public String materialPage() {
        return "material/list";
    }
}
