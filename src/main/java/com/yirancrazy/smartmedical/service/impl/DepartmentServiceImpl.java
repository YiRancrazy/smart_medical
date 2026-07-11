package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.DepartmentMapper;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 科室服务实现类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    /**
     * 插入科室
     * @param department 科室信息
     * @return 影响的行数
     */
    @Override
    public Integer insertDepartment(Department department) {
        return departmentMapper.insert(department);
    }

    /**
     * 根据id查询科室
     * @param id 科室id
     * @return 科室信息
     */
    @Override
    public Department getDepartmentById(Long id) {
        return departmentMapper.selectById(id);
    }

    /**
     * 根据id列表查询科室
     * @param idList id 列表
     * @return 科室列表
     */
    @Override
    public List<Department> listDepartmentsByIds(List<Long> idList) {
        return departmentMapper.selectByIds(idList);
    }

    /**
     * 更新科室
     * @param department 科室信息
     * @return 影响的行数
     */
    @Override
    public Integer updateDepartmentById(Department department) {
        return departmentMapper.updateById(department);
    }

    /**
     * 删除科室
     * @param id 科室id
     * @return 影响的行数
     */
    @Override
    public Integer deleteDepartmentById(Long id) {
        return departmentMapper.deleteById(id);
    }

    /**
     * 查询所有科室
     * @return 科室列表
     */
    @Override
    public List<Department> listAllDepartment() {
        return departmentMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页查询所有科室
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 科室列表
     */
    @Override
    public PageInfo<Department> listDepartmentsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Department> departments = departmentMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(departments);
    }

    /**
     * 批量删除科室
     * @param idList id列表
     * @return 影响的行数
     */
    @Override
    public Integer deleteBatch(List<Long> idList) {
        return departmentMapper.deleteByIds(idList);
    }

    /**
     * 查询所有父级科室
     * @return 父级科室列表
     */
    @Override
    public List<Department> listAllParentDepartments() {
        return departmentMapper.selectList(new QueryWrapper<Department>().isNull("parent_department_id"));
    }

    /**
     * 查询所有非父级科室
     * @return 非父级科室列表
     */
    @Override
    public List<Department> listAllNonParentDepartments() {
        return departmentMapper.selectList(new QueryWrapper<Department>().isNotNull("parent_department_id"));
    }

    /**
     * 根据父级科室id查询所有子级科室
     * @param parentId 父级科室id
     * @return 子级科室列表
     */
    @Override
    public List<Department> listAllDepartmentsByParentDepartmentId(Long parentId) {
        return departmentMapper.selectList(new QueryWrapper<Department>().eq("parent_department_id", parentId));
    }

    /**
     * 分页查询所有父级科室
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 父级科室列表
     */
    @Override
    public PageInfo<Department> listAllParentDepartmentsByPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Department> departments = departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                .isNotNull(Department::getParentDepartmentId));
        return new PageInfo<>(departments);
    }

    /**
     * 根据科室编号查询科室
     * @param sn 科室编号
     * @return 科室
     */
    @Override
    public Department getDepartmentBySn(Long sn) {
        return departmentMapper.selectOne(new LambdaQueryWrapper<Department>().eq(Department::getSn, sn));
    }

    /**
     * 根据科室编号查询科室
     * @param parentId 父级科室编号
     * @param status 状态
     * @param type 类型
     * @param name 名称
     * @return 科室列表
     */
    @Override
    public List<Department> listDepartmentByParentIdAndStatusAndTypeAndLikeNameAndLikeSn(Long parentId, Integer status, Integer type, String name,Long sn) {
        LambdaQueryWrapper<Department> wrapper= new LambdaQueryWrapper<Department>();

        if(parentId!=null){
            wrapper.eq(Department::getParentDepartmentId, parentId);
        }
        if(type!=null){
            wrapper.eq(Department::getType, type);
        }
        if(status!=null){
            wrapper.eq(Department::getStatus, status);
        }
        if(name != null && !name.isEmpty()){
            wrapper.like(Department::getName, name);
        }
        if(sn!=null){
            wrapper.like(Department::getSn, sn);
        }
        return departmentMapper.selectList(wrapper);
    }
}
