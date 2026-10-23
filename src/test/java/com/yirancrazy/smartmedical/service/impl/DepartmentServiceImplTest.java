package com.yirancrazy.smartmedical.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.yirancrazy.smartmedical.mapper.DepartmentMapper;
import com.yirancrazy.smartmedical.pojo.Department;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 科室服务查询语义单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 覆盖一级、二级科室对 NULL 与历史 0 值的兼容查询
 * @Datetime: 2026-10-23 09:21
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DepartmentServiceImplTest {

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    /**
     * 一级科室同时兼容 NULL 和历史 0 值
     */
    @Test
    void listAllParentDepartments_includesNullAndLegacyZero() {
        when(departmentMapper.selectList(any())).thenReturn(List.of());
        ArgumentCaptor<Wrapper<Department>> captor = ArgumentCaptor.forClass(Wrapper.class);

        departmentService.listAllParentDepartments();

        verify(departmentMapper).selectList(captor.capture());
        AbstractWrapper<Department, ?, ?> wrapper = (AbstractWrapper<Department, ?, ?>) captor.getValue();
        String sql = wrapper.getSqlSegment();
        assertTrue(sql.contains("IS NULL"));
        assertTrue(sql.contains(" OR "));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(0L));
    }

    /**
     * 二级科室排除 NULL 和历史 0 值
     */
    @Test
    void listAllNonParentDepartments_excludesNullAndLegacyZero() {
        when(departmentMapper.selectList(any())).thenReturn(List.of());
        ArgumentCaptor<Wrapper<Department>> captor = ArgumentCaptor.forClass(Wrapper.class);

        departmentService.listAllNonParentDepartments();

        verify(departmentMapper).selectList(captor.capture());
        AbstractWrapper<Department, ?, ?> wrapper = (AbstractWrapper<Department, ?, ?>) captor.getValue();
        String sql = wrapper.getSqlSegment();
        assertTrue(sql.contains("IS NOT NULL"));
        assertTrue(sql.contains("<>"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(0L));
    }
}
