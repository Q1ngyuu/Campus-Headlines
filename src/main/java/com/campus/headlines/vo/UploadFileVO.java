package com.campus.headlines.vo;

import java.io.Serializable;

/**
 * 文件上传结果：磁盘路径 + 对外访问地址。
 *
 * <p>由 {@code FileUploadUtil} 返回，{@code MaterialService} 再据此写 material 表。
 * 拆成 VO 是为了让“文件怎么存”和“记录怎么写”两件事解耦。</p>
 */
public class UploadFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 原始文件名，如 运动会.jpg（仅用于展示与入库，不参与磁盘路径） */
    private String originalName;

    /** 落盘后的文件名，如 7f3c9a...e1.jpg */
    private String storedName;

    /** 扩展名，小写无点 */
    private String ext;

    /** 文件类型：image / video */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 对外相对路径，如 /upload/202610/7f3c9a...e1.jpg，写入 material.file_path */
    private String relativePath;

    /** 磁盘绝对路径，仅用于删除和排查，不入库 */
    private String absolutePath;

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
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

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    @Override
    public String toString() {
        return "UploadFileVO{originalName='" + originalName + "', fileType='" + fileType
                + "', fileSize=" + fileSize + ", relativePath='" + relativePath + "'}";
    }
}
