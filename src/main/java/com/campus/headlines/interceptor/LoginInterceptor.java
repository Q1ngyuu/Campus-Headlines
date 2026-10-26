package com.campus.headlines.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器（初始化骨架）。
 *
 * <p>当前处于“占位”状态：preHandle 一律返回 true，不拦截任何请求，
 * 因此可以先把类建好、在 spring-mvc.xml 里保持注释，
 * 等登录功能（Session 写入 user）完成后再启用。</p>
 *
 * <p>启用步骤：</p>
 * <ol>
 *   <li>把 {@link #preHandle} 里注释掉的校验逻辑放开；</li>
 *   <li>打开 spring-mvc.xml 中 &lt;mvc:interceptors&gt; 的注释；</li>
 *   <li>确认排除清单里包含 /login、/register、/static/**、/upload/**。</li>
 * </ol>
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);

    /** Session 中存放登录用户的 key，登录成功后应为：session.setAttribute(SESSION_USER, user) */
    public static final String SESSION_USER = "loginUser";

    /** 登录页地址，未登录时跳转到这里 */
    private static final String LOGIN_URL = "/login";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // ---------------------------------------------------------------
        // TODO 登录功能完成后放开以下逻辑
        // ---------------------------------------------------------------
        // 1) 取 Session 中的登录用户
        // Object loginUser = request.getSession().getAttribute(SESSION_USER);
        // if (loginUser != null) {
        //     return true;
        // }
        //
        // 2) 未登录：AJAX 请求返回 401 JSON，页面请求重定向到登录页
        // String requestURI = request.getRequestURI();
        // if (requestURI.endsWith(".json") || "XMLHttpRequest".equals(
        //         request.getHeader("X-Requested-With"))) {
        //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //     response.setContentType("application/json;charset=UTF-8");
        //     response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
        //     return false;
        // }
        //
        // response.sendRedirect(request.getContextPath() + LOGIN_URL);
        // return false;

        log.debug("LoginInterceptor 占位放行：{}", request.getRequestURI());
        return true;
    }
}
