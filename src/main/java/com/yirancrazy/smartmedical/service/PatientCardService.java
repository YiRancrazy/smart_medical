package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.PatientCard;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 钱包服务接口
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

public interface PatientCardService {

    /**
     * 添加钱包
     * @param patientCard 钱包信息
     * @return 添加结果
     */
    int insertPatientCard(PatientCard patientCard);

    /**
     * 根据ID获取钱包信息
     * @param id 钱包ID
     * @return 钱包信息
     */
    PatientCard getPatientCardById(Long id);

    /**
     * 更新钱包信息
     * @param patientCard 钱包信息
     * @return 更新结果
     */
    int updatePatientCardById(PatientCard patientCard);

    /**
     * 根据ID删除钱包
     * @param id 钱包ID
     * @return 删除结果
     */
    int deletePatientCardById(Long id);

    /**
     * 获取所有钱包列表
     * @return 钱包列表
     */
    List<PatientCard> listAllPatientCards();

    /**
     * 分页查询钱包
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<PatientCard> getPatientCardPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除钱包
     * @param ids 钱包ID列表
     * @return 删除结果
     */
    int deleteBatch(List<Long> ids);

    /**
     * 根据用户id获取用户下所有就诊卡信息
     * @param userIdList 用户ID列表
     * @return 就诊卡信息列表
     */
    List<PatientCard> listPatientCardByUserIdList(List<Long> userIdList);

    /**
     * 根据ID获取就诊卡信息
     * @param ids 就诊卡ID
     * @return 就诊卡信息
     */
    List<PatientCard> getPatientCardsByIds(List<Long> ids);

    /**
     * 根据卡号获取就诊卡信息
     * @param patientCardSn 就诊卡卡号
     * @return 就诊卡信息
     */
    PatientCard getPatientCardBySn(Long patientCardSn);
}
