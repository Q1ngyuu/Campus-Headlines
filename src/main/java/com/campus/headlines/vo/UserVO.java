package com.campus.headlines.vo;

import com.campus.headlines.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视图对象：对外返回的用户信息。
 *
 * <p>最关键的一点：**不含 password 字段**。实体 {@link User} 里有密文，
 * 直接返回实体等于把密文暴露给前端，因此统一转成 VO 再出网关。</p>
 *
 * <p>实现 {@code Serializable} 是因为该对象会放进 HttpSession。</p>
 */
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像路径 */
    private String avatar;

    /** 角色：0-学生，1-管理员 */
    private Integer role;

    /** 注册时间 */
    private LocalDateTime createTime;

    public UserVO() {
    }

    /** 实体转 VO；入参为 null 时返回 null */
    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    /**
     * 是否管理员，便于拦截器或业务代码判断。
     *
     * <p>加 {@code @JsonIgnore}：Jackson 会把 {@code isXxx()} 当成一个名为 {@code xxx} 的属性，
     * 不加注解就会在 JSON 里多出一个 {@code "admin": false}，容易和实体字段混淆。</p>
     */
    @JsonIgnore
    public boolean isAdmin() {
        return role != null && role == 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserVO{id=" + id + ", username='" + username + "', nickname='" + nickname
                + "', role=" + role + '}';
    }
}
