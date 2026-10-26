package com.campus.headlines.vo;

import com.campus.headlines.entity.Material;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 素材视图对象。
 *
 * <p>比实体多了两个展示用字段：{@code sizeText}（人类可读大小）和 {@code image}（是否图片），
 * 前端据此决定用 {@code <img>} 还是视频占位图标。</p>
 */
public class MaterialVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 对外访问路径，如 /upload/202610/xxx.jpg */
    private String filePath;

    /** image / video */
    private String fileType;

    /** 字节数 */
    private Long fileSize;

    /** 人类可读大小，如 1.2 MB */
    private String sizeText;

    /** 上传时间 */
    private LocalDateTime createTime;

    public static MaterialVO from(Material m) {
        if (m == null) {
            return null;
        }
        MaterialVO vo = new MaterialVO();
        vo.setId(m.getId());
        vo.setFileName(m.getFileName());
        vo.setFilePath(m.getFilePath());
        vo.setFileType(m.getFileType());
        vo.setFileSize(m.getFileSize());
        vo.setSizeText(humanSize(m.getFileSize()));
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }

    /** 是否图片，前端据此选择缩略图渲染方式 */
    public boolean isImage() {
        return Material.TYPE_IMAGE.equals(fileType);
    }

    private static String humanSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0 B";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        return String.format("%.1f MB", kb / 1024.0);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSizeText() {
        return sizeText;
    }

    public void setSizeText(String sizeText) {
        this.sizeText = sizeText;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "MaterialVO{id=" + id + ", fileName='" + fileName + "', fileType='" + fileType
                + "', sizeText='" + sizeText + "'}";
    }
}
