package com.yirancrazy.smartmedical.manager.loader.impl;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.DepartmentConstant;
import com.yirancrazy.smartmedical.service.DepartmentService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 部门数据加载器
 * @Datetime: 2026-03-06 10:30
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class DepartmentLoaderManager {
    private final DepartmentService departmentService;

    /**
     * 加载所有部门
     */
    @PostConstruct
    public void loadAllDepartments(){
        DepartmentConstant.DEPARTMENT_LIST.addAll(departmentService.listAllDepartment());
    }
}