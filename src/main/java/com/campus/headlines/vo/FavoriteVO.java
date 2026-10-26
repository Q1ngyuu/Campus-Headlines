package com.campus.headlines.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏项视图：收藏记录 + 被收藏文章的关键信息。
 *
 * <p>由 {@code FavoriteMapper.xml} 的联表查询直接映射（收藏表 join 文章表 join 栏目表），
 * 这样一次查询就能把“我的收藏”列表渲染出来，不用在 Java 里逐条查文章。</p>
 */
public class FavoriteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收藏记录ID */
    private Long favoriteId;

    /** 文章ID */
    private Long articleId;

    private String title;

    private String coverImage;

    /** 栏目名，联表补齐 */
    private String categoryName;

    private LocalDateTime publishTime;

    private Integer viewCount;

    /** 1 = 置顶 */
    private Integer isTop;

    /** 收藏时间 */
    private LocalDateTime favoriteTime;

    public Long getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(Long favoriteId) {
        this.favoriteId = favoriteId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
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

    public LocalDateTime getFavoriteTime() {
        return favoriteTime;
    }

    public void setFavoriteTime(LocalDateTime favoriteTime) {
        this.favoriteTime = favoriteTime;
    }

    @Override
    public String toString() {
        return "FavoriteVO{favoriteId=" + favoriteId + ", articleId=" + articleId
                + ", title='" + title + "'}";
    }
}
