package com.campus.headlines.dto;

/**
 * 个人资料修改入参。
 */
public class ProfileDTO {

    /** 昵称，展示用 */
    private String nickname;

    /** 头像路径（取自素材或手填） */
    private String avatar;

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
}
