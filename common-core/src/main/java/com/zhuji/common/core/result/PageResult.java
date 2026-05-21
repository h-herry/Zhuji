package com.zhuji.common.core.result;

import java.util.ArrayList;
import java.util.List;

public class PageResult<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;

    public PageResult() {
        this.records = new ArrayList<>();
    }

    public PageResult(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

    public static <T> PageResult<T> of(List<T> records, long total, long size, long current) {
        return new PageResult<>(records, total, size, current);
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }
}
