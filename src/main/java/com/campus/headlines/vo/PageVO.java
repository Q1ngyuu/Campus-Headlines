package com.campus.headlines.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页返回体。
 *
 * <p>直接把 MyBatis-Plus 的 {@code Page} 序列化给前端会带上很多内部字段
 * （searchCount、optimizeCountSql、orders 等），既冗余又不稳定，
 * 因此统一转成这几个字段。</p>
 *
 * @param <T> 列表元素类型
 */
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码，从 1 开始 */
    private long page;

    /** 每页条数 */
    private long size;

    /** 总页数 */
    private long pages;

    public PageVO() {
    }

    public PageVO(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size <= 0 ? 0 : (total + size - 1) / size;
    }

    /** 由 MyBatis-Plus 分页结果直接构造（元素类型相同） */
    public static <T> PageVO<T> of(IPage<T> page) {
        return new PageVO<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 由 MyBatis-Plus 分页结果转换元素类型（实体 → VO） */
    public static <E, T> PageVO<T> of(IPage<E> page, Function<E, T> mapper) {
        List<T> list = page.getRecords() == null
                ? Collections.emptyList()
                : page.getRecords().stream().map(mapper).collect(Collectors.toList());
        return new PageVO<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }
}
