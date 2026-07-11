package com.yirancrazy.smartmedical.pojo.dto.user.result;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     * 每页数量
     */
    private Integer pageSize;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 总页数
     */
    private Integer totalPages;
    /**
     * 当前页数据列表
     */
    private List<T> list;

    /**
     * 便捷的构造方法，从 PageInfo 转换
     */
    public PageResult(PageInfo<?> pageInfo, List<T> targetList) {
        this.pageNum = pageInfo.getPageNum();
        this.pageSize = pageInfo.getPageSize();
        this.total = pageInfo.getTotal();
        this.totalPages = pageInfo.getPages();
        this.list = targetList; // 这里传入的是已经处理过的VO/DTO列表
    }
}