package com.campus.headlines.dto;

/**
 * 注册入参。
 *
 * <p>与 {@code User} 实体分开：注册时不允许前端指定 {@code role} / {@code status}，
 * 否则任何人都能把自己注册成管理员。</p>
 */
public class RegisterDTO {

    /** 登录账号，3-50 位，字母/数字/下划线 */
    private String username;

    /** 明文密码，6-32 位，落库前由 BCrypt 加密 */
    private String password;

    /** 昵称，不填则默认用用户名 */
    private String nickname;

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
