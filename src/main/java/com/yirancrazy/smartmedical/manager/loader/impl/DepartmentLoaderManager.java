package com.yirancrazy.smartmedical.manager.loader.impl;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.DepartmentConstant;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.service.DepartmentService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 部门数据加载器
 * @Datetime: 2026-03-06 10:30
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class DepartmentLoaderManager {
    private final DepartmentService departmentService;

    /**
     * 加载所有部门
     */
    @PostConstruct
    public void loadAllDepartments(){
        List<Department> departments = departmentService.listAllDepartment();
        DepartmentConstant.DEPARTMENT_LIST.addAll(departments);
        log.info("[loader] 科室数据加载完成，共{}个", departments.size());
    }
}