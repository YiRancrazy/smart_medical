package com.yirancrazy.smartmedical.service;

import com.yirancrazy.smartmedical.pojo.UserPatientRelation;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户患者关系服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface UserPatientRelationService {

    /**
     * 根据用户id查询用户患者关系id列表
     * @param userId 用户id
     * @return 用户患者关系id列表
     */
    List<Long> getUserPatientRelationIdListByUserId(Long userId);

    /**
     * 根据用户id查询用户患者关系
     * @param userId 用户id
     * @return 用户患者关系
     */
    List<UserPatientRelation> getUserPatientRelationsByUserId(Long userId);

    /**
     * 根据id查询用户患者关系
     * @param id id
     * @return 用户患者关系
     */
    UserPatientRelation getUserPatientRelationById(Long id);

    /**
     * 更新用户患者关系
     * @param userPatientRelation 用户患者关系
     * @return 更新数量条数
     */
    Integer updateUserPatientRelationById(UserPatientRelation userPatientRelation);

    /**
     * 插入用户患者关系
     * @param userPatientRelation 用户患者关系
     * @return 插入数量
     */
    Integer insertUserPatientRelation(UserPatientRelation userPatientRelation);

    /**
     * 删除用户患者关系
     * @param id id
     * @return 删除数量
     */
    Integer deleteUserPatientRelationById(Long id);

    /**
     * 检查当前用户是否已添加该就诊人（存在 user_id → patient_user_id 关系）
     * <p>已移除"非本人账号需要其他就诊人授权"的门禁：存在关系即为可访问</p>
     * @param userId 用户ID
     * @param patientUserId 患者用户ID
     * @return 是否存在就诊人关系
     */
    boolean hasAuthorization(Long userId, Long patientUserId);

    /**
     * 获取当前账号下可访问的患者 userId 列表（所有已添加就诊人，含本人）
     * @param currentUserId 当前登录用户id
     * @param patientCardId 就诊卡id（为 null 时返回全部关联患者）
     * @return 患者 userId 列表；patientCardId 不合法或无访问关系时返回空列表
     */
    List<Long> getAccessiblePatientUserIds(Long currentUserId, Long patientCardId);
}
