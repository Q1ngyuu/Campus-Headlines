package com.campus.headlines.dto;

/**
 * 栏目新增 / 修改入参。
 */
public class CategoryDTO {

    /** 栏目ID，null 表示新增 */
    private Long id;

    /** 栏目名称，唯一 */
    private String name;

    /** 排序值，越小越靠前 */
    private Integer sortOrder;

    /** 状态：0-停用，1-启用 */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
