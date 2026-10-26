package com.campus.headlines.controller;

import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把异常统一转成 {@link Result}，避免前端拿到 Tomcat 的错误页。
 *
 * <p>用 {@code @RestControllerAdvice}（而非 {@code @ControllerAdvice}）是为了让返回值
 * 直接序列化成 JSON，省去每个方法加 {@code @ResponseBody}。</p>
 *
 * <p>注意：本类放在 controller 包下，才能被 spring-mvc.xml 的
 * {@code <context:component-scan base-package="com.campus.headlines.controller"/>} 扫到。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 可预期的业务失败：按异常携带的 code 返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 请求体不是合法 JSON，或字段类型不匹配 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error(Result.CODE_BAD_REQUEST, "请求参数格式不正确");
    }

    /** 兜底：任何未处理的异常都记日志，但不把堆栈细节返回给前端 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOthers(Exception e) {
        log.error("未处理的异常", e);
        return Result.error(Result.CODE_ERROR, "服务器开小差了，请稍后再试");
    }
}
