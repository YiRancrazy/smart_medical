package com.yirancrazy.smartmedical.utils;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * PageUtils 单测
 */
class PageUtilsTest {

    @Test
    void startPage_doesNotThrowOnNullParams() {
        assertDoesNotThrow(() -> PageUtils.startPage(null, null));
    }

    @Test
    void startPage_doesNotThrowOnValidParams() {
        assertDoesNotThrow(() -> PageUtils.startPage(1, 10));
    }

    @Test
    void query_returnsPageInfo() {
        List<String> rows = Arrays.asList("a", "b", "c");
        PageInfo<String> info = PageUtils.query(1, 10, () -> rows);
        assertNotNull(info);
        assertEquals(3, info.getList().size());
    }

    @Test
    void query_emptySupplierReturnsEmptyPageInfo() {
        PageInfo<String> info = PageUtils.query(1, 10, Collections::emptyList);
        assertNotNull(info);
        assertEquals(0, info.getList().size());
    }

    @Test
    void toResult_mapsPageInfoToPageResult() {
        PageInfo<String> info = new PageInfo<>();
        info.setPageNum(2);
        info.setPageSize(10);
        info.setTotal(35L);
        info.setPages(4);
        info.setList(Arrays.asList("x", "y"));

        List<String> target = Arrays.asList("X", "Y");
        PageResult<String> result = PageUtils.toResult(info, target);

        assertNotNull(result);
        assertEquals(2, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(35L, result.getTotal());
        assertEquals(4, result.getTotalPages());
        assertEquals(target, result.getList());
    }

    @Test
    void toResult_emptyList() {
        PageInfo<String> info = new PageInfo<>();
        info.setPageNum(1);
        info.setPageSize(10);
        info.setTotal(0L);
        info.setPages(0);
        info.setList(Collections.emptyList());

        PageResult<String> result = PageUtils.toResult(info, Collections.emptyList());
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getList().size());
    }
}