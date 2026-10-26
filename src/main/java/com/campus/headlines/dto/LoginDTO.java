package com.campus.headlines.dto;

/**
 * 登录入参。
 */
public class LoginDTO {

    /** 登录账号 */
    private String username;

    /** 明文密码，与库中 BCrypt 密文比对 */
    private String password;

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
}
