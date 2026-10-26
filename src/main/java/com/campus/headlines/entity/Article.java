package com.campus.headlines.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资讯实体，对应数据库表 {@code article}。
 *
 * <p>审核状态是本表的核心，流转过程：</p>
 * <pre>
 *   0 草稿 ──提交──> 1 待审核 ──通过──> 2 已通过（写 publish_time，前台可见）
 *                             └──驳回──> 3 已驳回（记录 reject_reason）
 *   3 已驳回 ──修改后重新提交──> 1 待审核
 * </pre>
 *
 * <p>状态值请统一使用本类的 {@code STATUS_*} 常量，不要在业务代码里写魔法数字。</p>
 */
@TableName("article")
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 草稿：作者可见可改，前台不展示 */
    public static final int STATUS_DRAFT = 0;

    /** 待审核：已提交，等待管理员处理 */
    public static final int STATUS_PENDING = 1;

    /** 已通过：前台可见，可按发布时间排序 */
    public static final int STATUS_APPROVED = 2;

    /** 已驳回：附驳回原因，作者可修改后重新提交 */
    public static final int STATUS_REJECTED = 3;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者ID，关联 user.id */
    private Long userId;

    /** 栏目ID，关联 category.id */
    private Long categoryId;

    /** 标题 */
    private String title;

    /** 封面图访问路径 */
    private String coverImage;

    /** 正文，HTML 富文本 */
    private String content;

    /** 审核状态：0-草稿，1-待审核，2-已通过，3-已驳回 */
    private Integer status;

    /** 驳回原因，status=3 时填写 */
    private String rejectReason;

    /** 浏览量 */
    private Integer viewCount;

    /** 是否置顶：0-否，1-是 */
    private Integer isTop;

    /** 发布时间，审核通过时写入 */
    private LocalDateTime publishTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

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

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        // 不含 content，避免日志被正文刷屏
        return "Article{id=" + id + ", userId=" + userId + ", categoryId=" + categoryId
                + ", title='" + title + "', status=" + status
                + ", viewCount=" + viewCount + ", isTop=" + isTop
                + ", publishTime=" + publishTime + '}';
    }
}
