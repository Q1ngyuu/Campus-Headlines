package com.campus.headlines.interceptor;

import com.campus.headlines.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器：未登录时拦截需要身份的请求。
 *
 * <p>拦截范围在 spring-mvc.xml 里配置，包含投稿、草稿、上传、后台管理；
 * 登录/注册、列表与详情、静态资源走 exclude 放行。</p>
 *
 * <p>响应策略分两种：</p>
 * <ul>
 *   <li>AJAX 请求：返回 JSON {@code {"code":401,"msg":"请先登录"}}，前端据此弹提示或跳转；</li>
 *   <li>页面请求：302 重定向到登录页 {@code /login}。</li>
 * </ul>
 *
 * <p>注意：JSON 响应的 HTTP 状态码仍为 200，业务状态放在 {@code code} 字段，
 * 与全局 {@link Result} 约定保持一致，前端不需要区分两套错误处理逻辑。</p>
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);

    /** Session 中存放登录用户的 key，登录成功时由 UserController 写入 */
    public static final String SESSION_USER = "loginUser";

    /** 登录页路径（相对 contextPath） */
    private static final String LOGIN_PAGE = "/login";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        Object loginUser = session == null ? null : session.getAttribute(SESSION_USER);

        if (loginUser != null) {
            return true;
        }

        log.info("拦截未登录请求: {} {}", request.getMethod(), request.getRequestURI());

        if (isAjaxRequest(request)) {
            writeJson(response, Result.unauthorized("请先登录"));
        } else {
            // 页面请求：跳登录页。带 redirect 参数便于登录后回跳（当前前端未使用，先留着）
            response.sendRedirect(request.getContextPath() + LOGIN_PAGE);
        }
        return false;
    }

    /**
     * 判断是否 AJAX / JSON 请求。
     *
     * <p>jQuery 的 $.ajax 默认会带 {@code X-Requested-With: XMLHttpRequest}；
     * 另外兼容显式声明 {@code Accept: application/json}、请求体为 JSON、
     * 或 URL 以 .json 结尾这几种情况。</p>
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        if ("XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))) {
            return true;
        }
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("application/json")) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri != null && uri.endsWith(".json");
    }

    /** 手动写出 JSON，保证中文不乱码 */
    private void writeJson(HttpServletResponse response, Result<?> result) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = response.getWriter()) {
            writer.write(MAPPER.writeValueAsString(result));
            writer.flush();
        }
    }
}
