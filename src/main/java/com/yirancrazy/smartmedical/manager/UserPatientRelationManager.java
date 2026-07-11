package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.vo.OutPatientCardBaseInfo;
import com.yirancrazy.smartmedical.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户患者关系Manager
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class UserPatientRelationManager {

    private final UserPatientRelationService userPatientRelationService;
    private final AccountService accountService;
    private final UserService userService;
    private final PatientCardService patientCardService;
    private final PatientService patientService;


    /**
     * 添加就诊人
     * @param name 就诊人姓名
     * @param idCard 就诊人身份证号
     * @param phone 就诊人手机号
     * @param relation 用户与就诊人关系
     * @param defaulted 是否默认就诊人
     * @return 添加结果
     */
    @Transactional
    public Result<Integer> insertUserPatientRelation(String currentUserId,String name, String idCard, String phone, String relation, String remark, String defaulted) {

        Long id = IdUtil.getSnowflakeNextId();
        User patientUser = userService.getUserByIdCard(idCard);
        Long patientUserId = patientUser.getId();
        Patient patient = patientService.getPatientByUserId(patientUserId);
        Integer isAuthorized = 0;

        // 查询用户关系是否存在
        List<UserPatientRelation> userPatientRelationList = userPatientRelationService.getUserPatientRelationsByUserId(Long.valueOf(currentUserId));
        UserPatientRelation isExistUserPatientRelation = userPatientRelationList
                .stream()
                .filter(userPatientRelation -> userPatientRelation.getPatientUserId().equals(patientUserId))
                .findFirst()
                .orElse(null);

        if (isExistUserPatientRelation != null) {
            return Result.fail("该用户已存在该就诊人");
        }
        // 如果设置了默认就诊人，则将原来的默认就诊人设置为非默认
        if (Boolean.parseBoolean(defaulted)) {
            // 获取所有用户的就诊人关系
            List<UserPatientRelation> list = userPatientRelationService.getUserPatientRelationsByUserId(Long.valueOf(currentUserId));
            // 找到默认就诊人
            UserPatientRelation userPatientRelation = list.stream().filter(UserPatientRelation::getDefaulted).findFirst().orElse(null);

            //如果默认就诊人不为空，则将其设置为非默认
            if(userPatientRelation!=null){
                userPatientRelation.setDefaulted(false);
                userPatientRelationService.updateUserPatientRelationById(userPatientRelation);
            }
        }

        // 此时没有默认就诊人，将当前插入对象设置为默认就诊人
        UserPatientRelation userPatientRelation = new UserPatientRelation();
        userPatientRelation.setId(id);
        userPatientRelation.setUserId(Long.valueOf(currentUserId));
        userPatientRelation.setPatientUserId(patientUserId);
        userPatientRelation.setIsAuthorized(isAuthorized);
        userPatientRelation.setDefaulted(Boolean.valueOf(defaulted));
        userPatientRelation.setRelation(relation);
        userPatientRelation.setRemark(remark);

        Integer integer = userPatientRelationService.insertUserPatientRelation(userPatientRelation);

        return Result.success(integer);
    }

    /**
     * 修改就诊人信息
     * @param id 就诊人id
     * @param relation 用户与就诊人关系
     * @param remark 备注
     * @param defaulted 是否默认就诊人
     * @return 修改结果
     */
    @Transactional
    public Result<Integer> updateUserPatientRelationById(String currentUserId, Long id, String relation, String remark, String defaulted) {
        UserPatientRelation userPatientRelation = null;
        List<UserPatientRelation> list = userPatientRelationService.getUserPatientRelationsByUserId(Long.valueOf(currentUserId));
        // 获取当前用户下所欲就诊人关系，目的为了获取默认就诊人
        userPatientRelation = list.stream().filter(UserPatientRelation::getDefaulted).findFirst().orElse(null);

        switch (defaulted) {
            case "true" -> {
                assert userPatientRelation != null;
                userPatientRelation.setDefaulted(false);
                userPatientRelationService.updateUserPatientRelationById(userPatientRelation);
                System.out.println("--------------------------------122222222");
            }
            case "false" -> {
                if (userPatientRelation == null) {
                    return Result.fail("当前账户不能没有默认就诊人");
                }
            }
        }


        UserPatientRelation userPatientRelationById = userPatientRelationService.getUserPatientRelationById(id);
        userPatientRelationById.setRelation(relation);
        userPatientRelationById.setRemark(remark);
        userPatientRelationById.setDefaulted(Boolean.valueOf(defaulted));
        return Result.success(userPatientRelationService.updateUserPatientRelationById(userPatientRelationById));
    }

    /**
     * 设置默认就诊人
     * @param currentUserId 当前用户id
     * @param id 就诊人id
     * @return 设置结果
     */
    @Transactional
    public Result<Integer> setDefaultUserPatientRelation(String currentUserId, Long id) {
        UserPatientRelation userPatientRelation = null;
        List<UserPatientRelation> list = userPatientRelationService.getUserPatientRelationsByUserId(Long.valueOf(currentUserId));
        // 获取当前用户下所欲就诊人关系，目的为了获取默认就诊人
        userPatientRelation = list.stream().filter(UserPatientRelation::getDefaulted).findFirst().orElse(null);
        if (userPatientRelation != null) {
            userPatientRelation.setDefaulted(false);
            userPatientRelationService.updateUserPatientRelationById(userPatientRelation);
        }
        userPatientRelation = userPatientRelationService.getUserPatientRelationById(id);
        userPatientRelation.setDefaulted(true);
        return Result.success(userPatientRelationService.updateUserPatientRelationById(userPatientRelation));
    }

    /**
     * 删除就诊人
     * @param id 就诊人id
     * @return 删除结果
     */
    public Result<Integer> deleteUserPatientRelationById(Long id) {
        return Result.success(userPatientRelationService.deleteUserPatientRelationById(id));
    }
}
