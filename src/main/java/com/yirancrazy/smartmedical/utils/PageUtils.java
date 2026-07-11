package com.yirancrazy.smartmedical.utils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;

import java.util.List;
import java.util.function.Supplier;

/**
 * 分页工具
 * <p>
 * 替换项目内散落的 {@code PageHelper.startPage(...)} + {@code new PageInfo<>(rows)}
 * + {@code new PageResult<>(pageInfo, list)} 三行模板（38 处）。
 *
 * @Author: YiRanCrazy@gmail.com
 * @Description: 分页工具
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */
public final class PageUtils {

    private PageUtils() {
    }

    /**
     * 启动分页（无返回值，供链式调用前的独立 startPage 使用场景）
     */
    public static void startPage(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null) {
            return;
        }
        PageHelper.startPage(pageNum, pageSize);
    }

    /**
     * 启动分页并执行查询，返回原始 PageInfo
     */
    public static <T> PageInfo<T> query(Integer pageNum, Integer pageSize, Supplier<List<T>> query) {
        startPage(pageNum, pageSize);
        List<T> rows = query.get();
        return new PageInfo<>(rows);
    }

    /**
     * PageInfo → 项目内 PageResult（dto.user.result 版本，承载 pageNum/pageSize/total/totalPages/list）
     *
     * @param pageInfo   PageHelper 分页结果
     * @param targetList 已转换的目标 VO/DTO 列表
     */
    public static <T> PageResult<T> toResult(PageInfo<?> pageInfo, List<T> targetList) {
        return new PageResult<>(pageInfo, targetList);
    }
}