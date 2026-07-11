package com.yirancrazy.smartmedical.pojo.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通用分页响应对象
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 当前页码
     */
    private Long pageNum;
    
    /**
     * 每页显示条数
     */
    private Long pageSize;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Long totalPages;
    
    /**
     * 分页数据列表
     */
    private List<T> records;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    public PageResult() {
    }
    
    public PageResult(Long pageNum, Long pageSize, Long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
        this.totalPages = calculateTotalPages(total, pageSize);
        this.hasPrevious = pageNum > 1;
        this.hasNext = pageNum < this.totalPages;
    }
    
    /**
     * 计算总页数
     */
    private Long calculateTotalPages(Long total, Long pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 0L;
        }
        return (total + pageSize - 1) / pageSize;
    }
    
    /**
     * 静态工厂方法
     */
    public static <T> PageResult<T> of(Long pageNum, Long pageSize, Long total, List<T> records) {
        return new PageResult<>(pageNum, pageSize, total, records);
    }
}