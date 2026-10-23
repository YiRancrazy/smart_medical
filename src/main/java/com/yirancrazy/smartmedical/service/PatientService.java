package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Patient;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface PatientService {

    /**
     * 添加患者信息
     * @param patient 患者对象
     * @return 添加结果
     */
    Integer insertPatient(Patient patient);

    /**
     * 根据ID查询患者信息
     * @param id 患者ID
     * @return 患者对象
     */
    Patient getPatientById(Long id);

    /**
     * 根据就诊卡ID查询患者信息
     * @param patientCardId 就诊卡ID
     * @return 患者对象
     */
    Patient getPatientByPatientCardId(Long patientCardId);

    /**
     * 根据规范化身份证号查询患者
     * @param idCard 规范化身份证号
     * @return 患者信息
     */
    Patient getPatientByIdCard(String idCard);

    /**
     * 根据规范化身份证号锁定查询患者
     * @param idCard 规范化身份证号
     * @return 患者信息
     */
    Patient getPatientByIdCardForUpdate(String idCard);

    /**
     * 根据本人绑定账号查询患者
     * @param userId 用户ID
     * @return 患者信息
     */
    Patient getPatientByOwnerUserId(Long userId);

    /**
     * 根据ID更新患者信息
     * @param patient 患者对象
     * @return 更新结果
     */
    Integer updatePatientById(Patient patient);

    /**
     * 根据ID删除患者信息
     * @param id 患者ID
     * @return 删除结果
     */
    Integer deletePatientById(Long id);

    /**
     * 查询所有患者列表
     * @return 患者列表
     */
    List<Patient> listAllPatients();

    /**
     * 根据ID列表批量查询患者信息
     * @param ids 患者ID列表
     * @return 患者列表
     */
    List<Patient> getPatientsByIds(List<Long> ids);

    /**
     * 分页查询患者列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Patient> listPatientsByPage(Integer pageNum, Integer pageSize);

}
