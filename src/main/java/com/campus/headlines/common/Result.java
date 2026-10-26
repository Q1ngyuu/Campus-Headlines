package com.campus.headlines.common;

import java.io.Serializable;

/**
 * 统一返回体：所有 Controller / REST 接口都返回该结构，前端只判断 {@code code}。
 *
 * <pre>
 * { "code": 200, "msg": "操作成功", "data": { ... } }
 * </pre>
 *
 * <p>约定：{@code code == 200} 表示成功，其余为失败；HTTP 状态码统一用 200，
 * 业务错误靠 {@code code} 区分，避免前端还要处理 4xx/5xx 的两种分支。</p>
 *
 * @param <T> 业务数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功 */
    public static final int CODE_OK = 200;
    /** 参数错误 */
    public static final int CODE_BAD_REQUEST = 400;
    /** 未登录 */
    public static final int CODE_UNAUTHORIZED = 401;
    /** 无权限 */
    public static final int CODE_FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int CODE_NOT_FOUND = 404;
    /** 数据冲突，如用户名已存在 */
    public static final int CODE_CONFLICT = 409;
    /** 服务器内部错误 */
    public static final int CODE_ERROR = 500;

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 业务数据，失败时为 null */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ---------------- 静态工厂 ----------------

    /** 成功，带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_OK, "操作成功", data);
    }

    /** 成功，无数据 */
    public static <T> Result<T> success() {
        return new Result<>(CODE_OK, "操作成功", null);
    }

    /** 成功，自定义提示 */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(CODE_OK, msg, data);
    }

    /** 失败，默认 500 */
    public static <T> Result<T> error(String msg) {
        return new Result<>(CODE_ERROR, msg, null);
    }

    /** 失败，指定状态码 */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /** 未登录便捷方法 */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(CODE_UNAUTHORIZED, msg, null);
    }

    // ---------------- getter / setter ----------------

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{code=" + code + ", msg='" + msg + "', data=" + data + '}';
    }
}
