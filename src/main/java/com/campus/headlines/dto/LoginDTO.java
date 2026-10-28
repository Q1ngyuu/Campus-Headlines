package com.campus.headlines.dto;

/**
 * 登录入参。
 */
public class LoginDTO {

    /** 登录账号 */
    private String username;

    /** 明文密码，与库中 BCrypt 密文比对 */
    private String password;

    /** 记住我：勾选后把 Session 有效期延长到 7 天，否则沿用默认的 30 分钟 */
    private Boolean rememberMe;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
