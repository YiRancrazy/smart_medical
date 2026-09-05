package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.DoctorMapper;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生服务实现类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;

    /**
     * 添加医生
     * @param doctor 医生
     * @return 添加结果
     */
    @Override
    public Integer insertDoctor(Doctor doctor) {
        return doctorMapper.insert(doctor);
    }

    /**
     * 根据ID获取医生
     * @param id 医生ID
     * @return 医生
     */
    @Override
    public Doctor getDoctorById(Long id) {
        return doctorMapper.selectById(id);
    }

    /**
     * 根据ID列表获取医生列表
     * @param ids 医生ID列表
     * @return 医生列表
     */
    @Override
    public List<Doctor> listDoctorsByIds(List<Long> ids) {
        return doctorMapper.selectByIds(ids);
    }

    /**
     * 修改医生
     * @param doctor 医生
     * @return 修改结果
     */
    @Override
    public Integer updateDoctorById(Doctor doctor) {
        return doctorMapper.updateById(doctor);
    }

    /**
     * 根据ID删除医生
     * @param id 医生ID
     * @return 删除结果
     */
    @Override
    public Integer deleteDoctorById(Long id) {
        return doctorMapper.deleteById(id);
    }

    /**
     * 获取所有医生
     * @return 医生列表
     */
    @Override
    public List<Doctor> listAllDoctors() {
        return doctorMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页获取所有医生
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 医生列表
     */
    @Override
    public PageInfo<Doctor> listDoctorsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Doctor> doctors = doctorMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(doctors);
    }

    /**
     * 批量删除医生
     * @param ids 医生ID列表
     * @return 删除结果
     */
    @Override
    public Integer deleteBatch(List<Long> ids) {
        return doctorMapper.deleteByIds(ids);
    }

    /**
     * 根据科室ID获取医生列表
     * @param departmentId 科室ID
     * @return 医生列表
     */
    @Override
    public List<Doctor> getDoctorsByDepartmentId(Long departmentId) {
        return doctorMapper.selectList(new QueryWrapper<Doctor>().eq("department_id", departmentId));
    }

    /**
     * 获取医生数量
     * @return 医生数量
     */
    @Override
    public Long countDoctor() {
        return doctorMapper.selectCount(new LambdaQueryWrapper<>());
    }

    /**
     * 根据科室ID获取最近一周的医生列表
     * @param departmentId 科室ID
     * @return 医生列表
     */
    @Override
    public List<Long> getDoctorIdListByDepartmentIdLastWeek(Long departmentId) {
        return doctorMapper.getDoctorIdListByDepartmentIdLastWeek(departmentId);
    }

    /**
     * 根据科室ID获取最近一周的医生列表
     * @param departmentId 科室ID
     * @return 医生列表
     */
    @Override
    public List<Doctor> getDoctorListByDepartmentIdLastWeek(Long departmentId) {
        return doctorMapper.getDoctorListByDepartmentIdLastWeek(departmentId);
    }

    /**
     * 根据科室ID获取医生列表
     * @param departmentId 科室ID
     * @return 医生列表
     */
    @Override
    public List<Doctor> listDoctorsByDepartmentId(Long departmentId) {
        return doctorMapper.selectList(new QueryWrapper<Doctor>().eq("department_id", departmentId));
    }

    /**
     * 根据医生ID列表和状态和最大提前天数获取医生列表
     * @param doctorIds 医生ID列表
     * @param status 状态
     * @param maxAdvanceDays 最大提前天数
     * @return 医生列表
     */
    @Override
    public List<Doctor> listDoctorsByDoctorIdsAndStatusAndMaxAdvanceDays(List<Long> doctorIds, Integer status, Integer maxAdvanceDays) {
        return doctorMapper.listDoctorsByDoctorIdsAndStatusAndMaxAdvanceDays(doctorIds, status, maxAdvanceDays);
    }

    /**
     * 根据医生名称模糊查询医生信息
     * @param name 医生名称
     * @return 医生信息
     */
    @Override
    public List<Doctor> listDoctorsSimpleResponseByDoctorName(String name) {
        QueryWrapper<Doctor> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        return doctorMapper.selectList(wrapper);
    }

    /**
     * 根据医生名称和科室ID模糊查询医生信息
     * @param username 医生名称
     * @param departmentId 科室ID
     * @return 医生信息
     */
    @Override
    public List<Doctor> listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentId(String username, Long departmentId) {
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        if(username != null && !username.isEmpty()){
            queryWrapper.like(Doctor::getName, username);
        }

        if(departmentId != null){
            queryWrapper.eq(Doctor::getDepartmentId, departmentId);
        }

        return doctorMapper.selectList(queryWrapper);
    }
}
