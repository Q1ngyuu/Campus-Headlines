package com.campus.headlines.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表实体，对应数据库表 {@code user}。
 *
 * <p>学生与管理员共用一张表，靠 {@link #role} 区分：
 * 学生负责投稿、收藏；管理员负责审核与栏目维护。</p>
 *
 * <p>字段与列名按 MyBatis-Plus 默认的驼峰-下划线规则映射
 * （GlobalConfig.DbConfig.tableUnderline = true）。</p>
 */
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号，全表唯一 */
    private String username;

    /** 密码，BCrypt 加密后的密文，禁止明文 */
    private String password;

    /** 昵称，展示用 */
    private String nickname;

    /** 头像访问路径 */
    private String avatar;

    /** 角色：0-学生，1-管理员 */
    private Integer role;

    /** 账号状态：0-禁用，1-正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        // 不含 password，避免日志泄漏密文
        return "User{id=" + id + ", username='" + username + "', nickname='" + nickname
                + "', role=" + role + ", status=" + status + '}';
    }
}
