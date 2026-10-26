package com.campus.headlines.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体，对应数据库表 {@code favorite}。
 *
 * <p>唯一索引 (user_id, article_id) 保证同一用户对同一篇资讯只能收藏一次，
 * 取消收藏即删除该行，因此不需要额外的状态字段。</p>
 */
@TableName("favorite")
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏人ID，关联 user.id */
    private Long userId;

    /** 被收藏资讯ID，关联 article.id */
    private Long articleId;

    /** 收藏时间 */
    private LocalDateTime createTime;

    public Favorite() {
    }

    public Favorite(Long userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }

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

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Favorite{id=" + id + ", userId=" + userId + ", articleId=" + articleId + '}';
    }
}
