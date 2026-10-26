package com.campus.headlines.dto;

import java.util.List;

/**
 * 资讯保存入参（草稿与提交审核共用）。
 *
 * <p>{@code materialIds} 是本次资讯使用的素材 id 列表，保存时写入 article_material；
 * 每次保存采用“先删后插”，因此前端每次都要带上完整列表，而不是增量。</p>
 */
public class ArticleSaveDTO {

    /** 资讯ID，null 表示新建 */
    private Long id;

    /** 栏目ID */
    private Long categoryId;

    /** 标题 */
    private String title;

    /** 封面图路径（取自某个素材的 filePath），可为空 */
    private String coverImage;

    /** 正文 HTML */
    private String content;

    /** 使用的素材ID列表，可为空 */
    private List<Long> materialIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<Long> getMaterialIds() {
        return materialIds;
    }

    public void setMaterialIds(List<Long> materialIds) {
        this.materialIds = materialIds;
    }
}
