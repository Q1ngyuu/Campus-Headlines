package com.campus.headlines.interceptor;

import com.campus.headlines.common.Result;
import com.campus.headlines.vo.UserVO;
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
 * 管理员拦截器：拦住 role != 1 的用户访问 /admin/**。
 *
 * <p>必须注册在 {@code LoginInterceptor} <b>之后</b>：登录拦截器负责“有没有登录”，
 * 这里负责“是不是管理员”，顺序反了会拿到 null 用户。</p>
 *
 * <p>真正的权限判定还在 Service 层做一次（查库校验 role），
 * 因为拦截器只信任 Session 里的快照——用户被降权后 Session 不会立刻失效。</p>
 */
public class AdminInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminInterceptor.class);

    /** 管理员角色值 */
    private static final int ROLE_ADMIN = 1;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        Object current = session == null ? null : session.getAttribute(LoginInterceptor.SESSION_USER);

        if (current instanceof UserVO && ((UserVO) current).getRole() != null
                && ((UserVO) current).getRole() == ROLE_ADMIN) {
            return true;
        }

        log.warn("非管理员访问被拦截: {} {}", request.getMethod(), request.getRequestURI());

        if (isAjaxRequest(request)) {
            writeJson(response, Result.error(Result.CODE_FORBIDDEN, "需要管理员权限"));
        } else {
            // 页面请求直接给 Tomcat 的 403 页面，比静默跳首页更容易发现问题
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
        }
        return false;
    }

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
