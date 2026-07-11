package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Doctor;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生服务接口
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

public interface DoctorService {

    /**
     * 添加医生
     * @param doctor 医生
     * @return 添加结果
     */
    Integer insertDoctor(Doctor doctor);

    /**
     * 根据ID获取医生
     * @param id 医生ID
     * @return 医生
     */
    Doctor getDoctorById(Long id);

    /**
     * 根据ID列表获取医生列表
     * @param ids ID列表
     * @return 医生列表
     */
    List<Doctor> listDoctorsByIds(List<Long> ids);

    /**
     * 修改医生
     * @param doctor 医生
     * @return 修改结果
     */
    Integer updateDoctorById(Doctor doctor);

    /**
     * 删除医生
     * @param id 医生ID
     * @return 删除结果
     */
    Integer deleteDoctorById(Long id);

    /**
     * 获取所有医生
     * @return 医生列表
     */
    List<Doctor> listAllDoctors();

    /**
     * 分页获取医生列表
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 医生列表
     */
    PageInfo<Doctor> listDoctorsByPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除医生
     * @param ids ID列表
     * @return 删除结果
     */
    Integer deleteBatch(List<Long> ids);

    /**
     * 根据科室ID获取医生列表
     * @param departmentId 科室ID
     * @return 医生列表
     */
    List<Doctor> getDoctorsByDepartmentId(Long departmentId);

    /**
     * 获取医生数量
     * @return 医生数量
     */
    Long countDoctor();

    /**
     * 获取指定科室的医生ID列表
     * @param departmentId 科室ID
     * @return 医生ID列表
     */
    List<Long> getDoctorIdListByDepartmentIdLastWeek(Long departmentId);

    /**
     * 获取指定科室的医生列表
     * @param departmentId 科室ID
     * @return 医生列表
     */
    List<Doctor> getDoctorListByDepartmentIdLastWeek(Long departmentId);

    /**
     * 获取指定科室的医生列表
     * @param departmentId 科室ID
     * @return 医生列表
     */
    List<Doctor> listDoctorsByDepartmentId(Long departmentId);


    /**
     * 获取指定科室的指定状态的预约时间内的医生列表
     * @param doctorIds 医生ID列表
     * @param status 状态
     * @param maxAdvanceDays 最大预约提前天数
     * @return 医生列表
     */
    List<Doctor> listDoctorsByDoctorIdsAndStatusAndMaxAdvanceDays(List<Long> doctorIds, Integer status, Integer maxAdvanceDays);

    /**
     * 获取指定科室的指定状态的预约时间内的医生列表
     * @param name 医生名称
     * @return 医生列表
     */
    List<Doctor> listDoctorsSimpleResponseByDoctorName(String name);

    /**
     * 根据指定科室和医生名称模糊查询医生列表
     * @param username 医生名称
     * @param departmentId 科室ID
     * @return 医生列表
     */
    List<Doctor> listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentId(String username, Long departmentId);
}
