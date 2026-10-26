package com.campus.headlines.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资讯与素材的关联实体，对应数据库表 {@code article_material}。
 *
 * <p>多对多关系：一篇资讯可以内嵌多个素材，同一个素材也可被多篇资讯引用。
 * 唯一索引 (article_id, material_id) 保证不会重复关联。</p>
 */
@TableName("article_material")
public class ArticleMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 资讯ID，关联 article.id */
    private Long articleId;

    /** 素材ID，关联 material.id */
    private Long materialId;

    /** 关联建立时间 */
    private LocalDateTime createTime;

    public ArticleMaterial() {
    }

    public ArticleMaterial(Long articleId, Long materialId) {
        this.articleId = articleId;
        this.materialId = materialId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ArticleMaterial{id=" + id + ", articleId=" + articleId
                + ", materialId=" + materialId + '}';
    }
}
