package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yirancrazy.smartmedical.mapper.UserPatientRelationMapper;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户患者关系服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class UserPatientRelationServiceImpl implements UserPatientRelationService {

    private final UserPatientRelationMapper userPatientRelationMapper;

    /**
     * 获取用户患者关系id列表
     * @param userId 用户id
     * @return 用户患者关系id列表
     */
    @Override
    public List<Long> getUserPatientRelationIdListByUserId(Long userId) {
        List<UserPatientRelation> userPatientRelationList = userPatientRelationMapper
                .selectList(new QueryWrapper<UserPatientRelation>()
                        .eq("user_id", userId));

        return userPatientRelationList
                .stream()
                .map(UserPatientRelation::getId)
                .toList();
    }

    /**
     * 获取用户患者关系列表
     * @param userId 用户id
     * @return 用户患者关系列表
     */
    @Override
    public List<UserPatientRelation> getUserPatientRelationsByUserId(Long userId) {
        return userPatientRelationMapper.selectList(new QueryWrapper<UserPatientRelation>().eq("user_id", userId));
    }

    /**
     * 根据id获取用户患者关系
     * @param id id
     * @return 用户患者关系
     */
    @Override
    public UserPatientRelation getUserPatientRelationById(Long id) {
        return userPatientRelationMapper.selectById(id);
    }

    /**
     * 更新用户患者关系
     * @param userPatientRelation 用户患者关系
     * @return 更新数量
     */
    @Override
    public Integer updateUserPatientRelationById(UserPatientRelation userPatientRelation) {
        return userPatientRelationMapper.updateById(userPatientRelation);
    }

    /**
     * 插入用户患者关系
     * @param userPatientRelation 用户患者关系
     * @return 插入数量
     */
    @Override
    public Integer insertUserPatientRelation(UserPatientRelation userPatientRelation) {
        return userPatientRelationMapper.insert(userPatientRelation);
    }

    /**
     * 删除用户患者关系
     * @param id id
     * @return 删除数量
     */
    @Override
    public Integer deleteUserPatientRelationById(Long id) {
        return userPatientRelationMapper.deleteById(id);
    }

    /**
     * 检查用户是否有代理权限
     * @param userId 用户ID
     * @param patientUserId 患者用户ID
     * @return 是否有代理权限
     */
    @Override
    public boolean hasAuthorization(Long userId, Long patientUserId) {
        return userPatientRelationMapper.selectCount(new QueryWrapper<UserPatientRelation>()
                .eq("user_id", userId)
                .eq("patient_user_id", patientUserId)
                .eq("is_authorized", 1)) > 0;
    }

//    @Override
//    public UserPatientRelation getDefaultUserPatientRelationByUserId(Long userId) {
//        return userPatientRelationMapper.selectOne(new QueryWrapper<UserPatientRelation>()
//                .eq("user_id", userId)
//                .eq("is_default", 1));
//    }
}
