package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Department;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 科室服务接口
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

public interface DepartmentService {

    /**
     * 插入科室
     * @param department 科室信息
     * @return 插入数量
     */
    Integer insertDepartment(Department department);

    /**
     * 根据ID获取科室信息
     * @param id 科室ID
     * @return 科室信息
     */
    Department getDepartmentById(Long id);

    /**
     * 根据ID列表获取科室信息
     * @param idList ID列表
     * @return 科室信息列表
     */
    List<Department> listDepartmentsByIds(List<Long> idList);

    /**
     * 根据ID更新科室信息
     * @param department 科室信息
     * @return 更新数量
     */
    Integer updateDepartmentById(Department department);

    /**
     * 根据ID删除科室
     * @param id 科室ID
     * @return 删除数量
     */
    Integer deleteDepartmentById(Long id);

    /**
     * 获取所有科室列表
     * @return 科室列表
     */
    List<Department> listAllDepartment();

    /**
     * 分页获取科室列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 科室列表
     */
    PageInfo<Department> listDepartmentsByPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除科室
     * @param ids ID列表
     * @return 删除数量
     */
    Integer deleteBatch(List<Long> ids);

    /**
     * 获取所有父级科室列表
     * @return 父级科室列表
     */
    List<Department> listAllParentDepartments();

    /**
     * 获取所有非父级科室列表
     * @return 非父级科室列表
     */
    List<Department> listAllNonParentDepartments();

    /**
     * 根据父级科室ID获取子级科室列表
     * @param parentId 父级科室ID
     * @return 子级科室列表
     */
    List<Department> listAllDepartmentsByParentDepartmentId(Long parentId);

    /**
     * 分页获取所有父级科室列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 父级科室列表
     */
    PageInfo<Department> listAllParentDepartmentsByPage(int pageNum, int pageSize);

    /**
     * 根据编号获取科室信息
     * @param sn 科室编号
     * @return 科室信息
     */
    Department getDepartmentBySn(Long sn);

    /**
     * 根据上级科室ID、状态、类型、名称查询科室
     * @param parentId 上级科室ID
     * @param status 状态
     * @param type  类型
     * @param name 名称
     * @param sn  编号
     * @return 科室列表
     */
    List<Department> listDepartmentByParentIdAndStatusAndTypeAndLikeNameAndLikeSn(Long parentId, Integer status, Integer type, String name,Long sn);
}
