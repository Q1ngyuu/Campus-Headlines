package com.campus.headlines.util;

import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.interceptor.LoginInterceptor;
import com.campus.headlines.vo.UserVO;

import javax.servlet.http.HttpSession;

/**
 * Session 中登录用户的读取工具。
 *
 * <p>拦截器已经拦住了未登录的请求，但 Controller 里仍需拿到 userId；
 * 统一在这里取，避免每个方法都写一遍 {@code session.getAttribute(...)} 与空判断。</p>
 */
public final class SessionUtil {

    private SessionUtil() {
    }

    /**
     * 取当前登录用户，未登录直接抛 401。
     *
     * <p>兜底场景：拦截器配置漏了某个路径、或浏览器标签页登录态过期。</p>
     */
    public static UserVO requireLogin(HttpSession session) {
        Object current = session == null ? null : session.getAttribute(LoginInterceptor.SESSION_USER);
        if (!(current instanceof UserVO)) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        return (UserVO) current;
    }

    /** 取当前登录用户 id，未登录抛 401 */
    public static Long requireUserId(HttpSession session) {
        return requireLogin(session).getId();
    }

    /** 取当前登录用户，未登录返回 null（用于“可选登录”的接口） */
    public static UserVO getLoginUser(HttpSession session) {
        Object current = session == null ? null : session.getAttribute(LoginInterceptor.SESSION_USER);
        return current instanceof UserVO ? (UserVO) current : null;
    }
}
