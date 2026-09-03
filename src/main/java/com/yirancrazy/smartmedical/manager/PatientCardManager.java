package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.DesensitizedUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PatientCardSimpleResponse;
import com.yirancrazy.smartmedical.pojo.vo.OutPatientCardBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationConfirmPatientCardVo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-13 19:16
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PatientCardManager {
    private final PatientCardService patientCardService;
    private final AccountService accountService;
    private final RedisUtil redisUtil;
    private final UserService userService;
    private final UserPatientRelationService userPatientRelationService;
    private final PatientService patientService;

    /**
     * 获取就诊卡信息
     * @param patientId 就诊人ID
     * @return 就诊卡信息
     */
    public Result<PatientCard> getPatientCardById(Long patientId) {
        return Result.success(patientCardService.getPatientCardById(patientId));
    }

    /**
     * 获取默认就诊人基本信息
     * @param userId 用户公开id
     * @return 默认就诊人基本信息
     */
    public Result<OutPatientCardBaseInfo> getDefaultPatientBaseInfoByUid(Long userId) {

        // 获取用户下默认患者id

        List<UserPatientRelation> userPatientRelations =
                userPatientRelationService.getUserPatientRelationsByUserId(userId);

        UserPatientRelation defaultUserPatientRelation = userPatientRelations
                        .stream()
                        .filter(UserPatientRelation::getDefaulted)
                        .findFirst()
                        .orElse(null);

        if (defaultUserPatientRelation == null) {
            return Result.fail("未绑定任何就诊人");
        }


        // 获取患者信息
        Patient defaultPatient = patientService.getPatientByUserId(defaultUserPatientRelation.getPatientUserId());

        // 获取患者用户信息
        User defaultPatientUser = userService.getUserById(defaultPatient.getUserId());

        // 获取默认就诊卡信息
        PatientCard patientCard = patientCardService.getPatientCardById(defaultPatient.getPatientCardId());

        OutPatientCardBaseInfo result = new OutPatientCardBaseInfo();
        result.setPatientId(String.valueOf(defaultPatient.getId())); // 患者id
        result.setPatientCardId(String.valueOf(patientCard.getId()));
        result.setPatientCardSn(String.valueOf(patientCard.getSn()));
        result.setPatientName(defaultPatientUser.getNickname()); // G08: 统一用 nickname（用户可编辑的展示名）
        result.setPatientAvatar(defaultPatientUser.getAvatar());
        result.setPatientIdCard(defaultPatientUser.getIdCard());

        // 门诊余额以分为单位存储，直接返回分
        result.setOutPatientBalance(String.valueOf(patientCard.getOutpatientBalance()));

        return Result.success(result);
    }


    /**
     * 获取用户绑定的所有就诊人基本信息
     * @param userId 用户公开id
     * @return 用户所有就诊人基本信息
     */
    public Result<List<RegistrationConfirmPatientCardVo>> getAllPatientBaseInfoByUserId(Long userId) {
        // 获取用户下所有绑定的患者
        List<UserPatientRelation> userPatientRelations = userPatientRelationService.getUserPatientRelationsByUserId(userId);

        // 提取出所有的患者id
        List<Long> patientIds = userPatientRelations
                .stream()
                .map(UserPatientRelation::getPatientUserId)
                .collect(Collectors.toList());

        // 通过用户idList获取患者信息
        List<Patient> patients = patientService.getPatientsByUserIds(patientIds);

        // 提取所有用户id
        List<Long> userIds = patients
                .stream()
                .map(Patient::getUserId)
                .collect(Collectors.toList());

        // 通过用户idList获取用户信息
        List<User> userList = userService.listUsersByUserIds(userIds);

        // 获取用户账户信息
        Account patientAccount = accountService.getAccountByUserId(userId);

        // 获取所有患者卡信息
        List<PatientCard> patientCards = patientCardService.getPatientCardsByIds(
                patients.stream().map(Patient::getPatientCardId).collect(Collectors.toList()));

        // 获取用户所有患者卡信息
        List<RegistrationConfirmPatientCardVo> result = new ArrayList<>();

        for (Patient patient : patients) {
            User patientUser = userList.stream().filter(user -> user.getId().equals(patient.getUserId())).findFirst().orElse(null);
            UserPatientRelation userPatientRelation = userPatientRelations
                    .stream()
                    .filter(item -> item.getPatientUserId().equals(patient.getUserId()))
                    .findFirst()
                    .orElse(null);
            PatientCard patientCard = patientCards.stream()
                    .filter(card -> card.getId().equals(patient.getPatientCardId()))
                    .findFirst()
                    .orElse(null);

            if(patientUser == null || userPatientRelation == null){
                return Result.fail("未绑定任何就诊人");
            }

            RegistrationConfirmPatientCardVo registrationConfirmPatientCardVo = new RegistrationConfirmPatientCardVo();
            registrationConfirmPatientCardVo.setUserId(String.valueOf(userPatientRelation.getPatientUserId()));
            registrationConfirmPatientCardVo.setPatientUserId(String.valueOf(patientUser.getId()));
            registrationConfirmPatientCardVo.setPatientName(patientUser.getNickname());
            registrationConfirmPatientCardVo.setPatientAvatar(patientUser.getAvatar());
            registrationConfirmPatientCardVo.setPatientIdCard(patientUser.getIdCard());
            registrationConfirmPatientCardVo.setPatientCardNo(patientCard == null ? "" : String.valueOf(patientCard.getId()));
            registrationConfirmPatientCardVo.setRelation(userPatientRelation.getRelation());
            registrationConfirmPatientCardVo.setDefaultPatientCard(userPatientRelation.getDefaulted());
            registrationConfirmPatientCardVo.setPatientPhone(DesensitizedUtil.mobilePhone(patientAccount.getPhone()));
            result.add(registrationConfirmPatientCardVo);
        }

        return Result.success(result);
    }

    /**
     * 获取用户所有患者卡信息简单响应
     * @param userId 用户id
     * @return 患者卡信息简单响应
     */
    public Result<List<PatientCardSimpleResponse>> listPatientCardSimpleResponseByUserId(Long userId) {
        // 获取用户下所有绑定的患者
        List<UserPatientRelation> userPatientRelations = userPatientRelationService.getUserPatientRelationsByUserId(userId);

        // 获取所有用户信息
        List<User> users = userService.listUsersByUserIds(userPatientRelations
                .stream()
                .map(UserPatientRelation::getUserId)
                .collect(Collectors.toList()));

        // 获取用户账户信息
        List<Account> accounts = accountService.listAccountsByUserIds(users
                .stream()
                .map(User::getId)
                .collect(Collectors.toList()));

        List<Long> patientIds = userPatientRelations
                .stream()
                .map(UserPatientRelation::getPatientUserId)
                .toList();

        // 获取所有患者信息
        List<Patient> patients = patientService.getPatientsByUserIds(patientIds);

        // 获取所有患者账户信息
        List<Account> patientAccounts = accountService.listAccountsByUserIds(patientIds);

        // 获取所有患者用户信息
        List<User> patientUsers = userService.listUsersByUserIds(patients.stream().map(Patient::getUserId).toList());

        // 获取所有患者卡信息
        List<PatientCard> patientCards = patientCardService.getPatientCardsByIds(patients
                .stream()
                .map(Patient::getPatientCardId)
                .collect(Collectors
                        .toList()));


        List<PatientCardSimpleResponse> result = new ArrayList<>();
        for (Patient patient : patients) {
            PatientCardSimpleResponse item = new PatientCardSimpleResponse();
//            User currentUser = users
//                    .stream()
//                    .filter(user -> user.getId().equals(patient.getUserId()))
//                    .findFirst()
//                    .orElse(null);
            User patientUser = patientUsers
                    .stream()
                    .filter(user -> user.getId().equals(patient.getUserId()))
                    .findFirst()
                    .orElse(null);
            Account currentAccount = patientAccounts
                    .stream()
                    .filter(account -> account.getUserId().equals(patient.getUserId()))
                    .findFirst()
                    .orElse(null);
            PatientCard currentPatientCard = patientCards
                    .stream()
                    .filter(patientCard -> patientCard.getId().equals(patient.getPatientCardId()))
                    .findFirst()
                    .orElse(null);
            UserPatientRelation currentUserPatientRelation = userPatientRelations
                    .stream()
                    .filter(userPatientRelation -> userPatientRelation.getPatientUserId().equals(patient.getUserId()))
                    .findFirst()
                    .orElse(null);
            // L05: assert 生产环境失效，改为显式抛 BizException
            if (patient == null || patientUser == null || currentAccount == null
                    || currentPatientCard == null || currentUserPatientRelation == null) {
                throw new BizException(BizErrorCode.PATIENT_DATA_INVALID);
            }

            item.setUserId(String.valueOf(currentAccount.getUserId())); // 获取当前用户id
            item.setUserPatientRelationId(String.valueOf(currentUserPatientRelation.getId())); // 配置用户患者关系、

            item.setPatientId(String.valueOf(patient.getId()));

            item.setPatientName(patientUser.getNickname());  // G08: 统一用 nickname
            item.setPatientIdCard(patientUser.getIdCard());  // 身份证
            item.setPatientPhone(DesensitizedUtil.mobilePhone(currentAccount.getPhone()));
            item.setPatientCardSn(String.valueOf(currentPatientCard.getSn())); // 患者卡号
            item.setPatientCardId(String.valueOf(currentPatientCard.getId()));
            item.setRelation(currentUserPatientRelation.getRelation());
            item.setDefaultPatient(currentUserPatientRelation.getDefaulted());
            item.setRemark(currentUserPatientRelation.getRemark());
            result.add(item);
        }

        return Result.success(result);
    }

    /**
     * 按就诊人关系ID查询单条详情（含 remark）
     * <p>F24: 替代前端拉全列表再 find by id 的 N+1 路径；同时回填 remark（U06）</p>
     * @param currentUserId 当前登录用户ID（归属校验）
     * @param relationId 用户患者关系ID
     * @return 单条就诊人详情
     * @throws BizException PATIENT_DATA_INVALID 关系不存在/不属于当前用户
     */
    public Result<PatientCardSimpleResponse> getPatientCardDetailByRelationId(Long currentUserId, Long relationId) {
        UserPatientRelation relation = userPatientRelationService.getUserPatientRelationById(relationId);
        if (relation == null || !currentUserId.equals(relation.getUserId())) {
            throw new BizException(BizErrorCode.PATIENT_DATA_INVALID);
        }
        Patient patient = patientService.getPatientByUserId(relation.getPatientUserId());
        User patientUser = userService.getUserById(relation.getPatientUserId());
        Account patientAccount = accountService.getAccountByUserId(relation.getPatientUserId());
        PatientCard patientCard = patient != null
                ? patientCardService.getPatientCardById(patient.getPatientCardId())
                : null;
        if (patientUser == null || patientAccount == null || patientCard == null) {
            throw new BizException(BizErrorCode.PATIENT_DATA_INVALID);
        }

        PatientCardSimpleResponse result = new PatientCardSimpleResponse();
        result.setUserId(String.valueOf(patientAccount.getUserId()));
        result.setUserPatientRelationId(String.valueOf(relation.getId()));
        result.setPatientId(String.valueOf(patient.getId()));
        result.setPatientName(patientUser.getNickname());
        result.setPatientIdCard(patientUser.getIdCard());
        result.setPatientPhone(DesensitizedUtil.mobilePhone(patientAccount.getPhone()));
        result.setPatientCardSn(String.valueOf(patientCard.getSn()));
        result.setPatientCardId(String.valueOf(patientCard.getId()));
        result.setRelation(relation.getRelation());
        result.setDefaultPatient(relation.getDefaulted());
        result.setRemark(relation.getRemark());
        return Result.success(result);
    }
}
