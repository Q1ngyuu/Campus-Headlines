package com.campus.headlines.dto;

/**
 * 修改密码入参。
 */
public class PasswordDTO {

    /** 原密码，用于校验身份 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
