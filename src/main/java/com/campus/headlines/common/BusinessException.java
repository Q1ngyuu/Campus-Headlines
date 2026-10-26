package com.campus.headlines.common;

/**
 * 业务异常：用于表达“可预期的业务失败”，如用户名已存在、密码错误、账号被禁用。
 *
 * <p>由 {@code GlobalExceptionHandler} 统一捕获并转成 {@link Result}，
 * 因此 Service / Controller 里直接 {@code throw new BusinessException("...")} 即可，
 * 不需要层层 return 错误码。</p>
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 跟随异常一起返回给前端的业务状态码 */
    private final int code;

    /** 默认按“参数错误(400)”处理 */
    public BusinessException(String message) {
        this(Result.CODE_BAD_REQUEST, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
