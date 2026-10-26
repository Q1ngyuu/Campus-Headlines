package com.campus.headlines.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资讯列表项（首页卡片、我的文章、待审核列表共用）。
 *
 * <p>同一条 VO 在不同页面用不同字段：</p>
 * <ul>
 *   <li>首页：标题、封面、栏目、发布时间、浏览量、是否置顶；</li>
 *   <li>我的文章：额外用 {@code status} 和 {@code rejectReason} 显示状态标签与驳回原因；</li>
 *   <li>待审核列表：额外用 {@code authorName}。</li>
 * </ul>
 */
public class ArticleListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String coverImage;

    private Long categoryId;

    /** 栏目名，联表补齐 */
    private String categoryName;

    private Long authorId;

    /** 作者昵称，联表补齐 */
    private String authorName;

    /** 发布时间（仅已通过的有值） */
    private LocalDateTime publishTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    private Integer viewCount;

    /** 1 = 置顶 */
    private Integer isTop;

    /** 0草稿 1待审核 2已通过 3已驳回 */
    private Integer status;

    /** 驳回原因，status=3 时有值 */
    private String rejectReason;

    /** 正文摘要（去标签、截断），首页卡片用 */
    private String summary;

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

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
