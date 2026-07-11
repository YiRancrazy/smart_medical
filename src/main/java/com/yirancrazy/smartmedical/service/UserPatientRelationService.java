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

//    UserPatientRelation getDefaultUserPatientRelationByUserId(Long userId);
}
