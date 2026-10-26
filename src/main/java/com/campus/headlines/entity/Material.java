package com.campus.headlines.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 素材实体，对应数据库表 {@code material}。
 *
 * <p>素材是用户上传的图片或视频，先独立落库，发布资讯时再通过
 * {@code article_material} 关联到具体资讯。</p>
 */
@TableName("material")
public class Material implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 素材类型常量：图片 */
    public static final String TYPE_IMAGE = "image";

    /** 素材类型常量：视频 */
    public static final String TYPE_VIDEO = "video";

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 上传者ID，关联 user.id */
    private Long userId;

    /** 原始文件名 */
    private String fileName;

    /** 访问路径，如 /upload/image/2026/10/xxxx.jpg */
    private String filePath;

    /** 素材类型：image-图片，video-视频 */
    private String fileType;

    /** 文件大小，单位字节 */
    private Long fileSize;

    /** 上传时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Material{id=" + id + ", userId=" + userId + ", fileName='" + fileName
                + "', fileType='" + fileType + "', fileSize=" + fileSize + '}';
    }
}
