package com.campus.headlines.dto;

/**
 * 审核入参。
 */
public class AuditDTO {

    /** 待审核的资讯ID */
    private Long articleId;

    /** 1-通过，0-驳回 */
    private Integer pass;

    /** 驳回原因，pass=0 时必填 */
    private String rejectReason;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Integer getPass() {
        return pass;
    }

    public void setPass(Integer pass) {
        this.pass = pass;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
