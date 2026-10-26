package com.campus.headlines.dto;

/**
 * 置顶操作入参。
 */
public class TopDTO {

    /** 文章ID */
    private Long articleId;

    /** 1-置顶，0-取消置顶 */
    private Integer isTop;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }
}
