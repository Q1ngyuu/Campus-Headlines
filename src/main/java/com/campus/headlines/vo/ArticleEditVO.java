package com.campus.headlines.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑草稿时回填表单用的数据（发布页 ?id=xxx 时拉取）。
 */
public class ArticleEditVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private Long categoryId;

    private String coverImage;

    private String content;

    /** 当前状态：0草稿 / 3已驳回，编辑保存后会回到 0 */
    private Integer status;

    /** 已关联的素材 id，用于回填选择器的选中态 */
    private List<Long> materialIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Long> getMaterialIds() {
        return materialIds;
    }

    public void setMaterialIds(List<Long> materialIds) {
        this.materialIds = materialIds;
    }
}
