package com.campus.headlines.controller;

import com.campus.headlines.service.CategoryService;
import com.campus.headlines.util.SessionUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 页面路由：把 URL 映射到 {@code /WEB-INF/views/} 下的 JSP。
 *
 * <p>JSP 放在 WEB-INF 下，浏览器无法直接访问，必须经过 Controller 转发。</p>
 *
 * <p>这里只负责“打开页面”：首页的栏目在后端一次性带进模型（省一次请求），
 * 列表数据一律由页面 AJAX 调 {@code /article/list} 拿。</p>
 */
@Controller
public class PageController {

    @Resource
    private CategoryService categoryService;

    /** 首页：文章卡片列表 + 栏目筛选 + 搜索 */
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        model.addAttribute("categories", categoryService.listEnabled());
        model.addAttribute("loginUser", SessionUtil.getLoginUser(session));
        return "index";
    }

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
