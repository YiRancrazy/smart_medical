package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.DesensitizedUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PatientCardSimpleResponse;
import com.yirancrazy.smartmedical.pojo.vo.OutPatientCardBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.SelfPatientCardStatus;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationConfirmPatientCardVo;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 就诊卡业务编排：就诊卡 / 就诊人关联
 * @Datetime: 2026-02-13 19:16
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PatientCardManager {

    private static final String SELF_RELATION = "本人";

    private final PatientCardService patientCardService;
    private final UserPatientRelationService userPatientRelationService;
    private final PatientService patientService;

    /**
     * 按患者ID获取就诊卡信息
     * @param patientId 患者ID
     * @return 就诊卡信息
     */
    public Result<PatientCard> getPatientCardById(Long patientId) {
        Patient patient = patientService.getPatientById(patientId);
        if (patient == null || patient.getPatientCardId() == null) {
            return Result.success(null);
        }
        return Result.success(patientCardService.getPatientCardById(patient.getPatientCardId()));
    }

    /**
     * 获取默认就诊人基本信息
     * @param userId 用户公开id
     * @return 默认就诊人基本信息
     */
    public Result<OutPatientCardBaseInfo> getDefaultPatientBaseInfoByUid(Long userId) {
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

        Patient defaultPatient = defaultUserPatientRelation.getPatientId() == null
                ? null : patientService.getPatientById(defaultUserPatientRelation.getPatientId());

        if (defaultPatient == null) {
            return Result.fail("默认就诊人不存在");
        }

        PatientCard patientCard = getPatientCard(defaultPatient);

        if (patientCard == null) {
            return Result.fail("默认就诊卡不存在");
        }

        OutPatientCardBaseInfo result = new OutPatientCardBaseInfo();
        result.setPatientId(String.valueOf(defaultPatient.getId()));
        result.setPatientCardId(String.valueOf(patientCard.getId()));
        result.setPatientCardSn(String.valueOf(patientCard.getSn()));
        result.setPatientName(defaultPatient.getName());
        result.setPatientAvatar(null);
        result.setPatientIdCard(defaultPatient.getIdCard());
        result.setPatientPhone(defaultPatient.getPhone());
        result.setPatientSex(defaultPatient.getSex());
        result.setOutPatientBalance(patientCard.getOutpatientBalance() == null
                ? null : String.valueOf(patientCard.getOutpatientBalance()));

        return Result.success(result);
    }


    /**
     * 获取用户绑定的所有就诊人基本信息
     * @param userId 用户公开id
     * @return 用户所有就诊人基本信息
     */
    public Result<List<RegistrationConfirmPatientCardVo>> getAllPatientBaseInfoByUserId(Long userId) {
        List<Long> patientIds = userPatientRelationService.listAccessiblePatientIds(userId);
        if (patientIds.isEmpty()) {
            return Result.success(List.of());
        }

        List<UserPatientRelation> userPatientRelations =
                userPatientRelationService.getUserPatientRelationsByUserId(userId);
        Map<Long, UserPatientRelation> relationMap = userPatientRelations.stream()
                .filter(relation -> relation.getPatientId() != null)
                .collect(Collectors.toMap(UserPatientRelation::getPatientId,
                        Function.identity(), (left, right) -> left));
        List<Patient> patients = patientService.getPatientsByIds(patientIds);
        Map<Long, Patient> patientMap = patients.stream()
                .filter(patient -> patient.getId() != null)
                .collect(Collectors.toMap(Patient::getId, Function.identity(), (left, right) -> left));
        Map<Long, PatientCard> patientCardMap = loadPatientCardMap(patients);

        List<RegistrationConfirmPatientCardVo> result = new ArrayList<>();
        for (Long patientId : patientIds) {
            Patient patient = patientMap.get(patientId);
            UserPatientRelation relation = relationMap.get(patientId);
            PatientCard patientCard = patient == null ? null : patientCardMap.get(patient.getPatientCardId());
            if (patient == null || relation == null || patientCard == null) {
                throw new BizException(BizErrorCode.PATIENT_DATA_INVALID);
            }

            RegistrationConfirmPatientCardVo registrationConfirmPatientCardVo = new RegistrationConfirmPatientCardVo();
            registrationConfirmPatientCardVo.setUserId(String.valueOf(relation.getUserId()));
            registrationConfirmPatientCardVo.setPatientUserId(toStringValue(patient.getOwnerUserId()));
            registrationConfirmPatientCardVo.setPatientId(String.valueOf(patient.getId()));
            registrationConfirmPatientCardVo.setPatientName(patient.getName());
            registrationConfirmPatientCardVo.setPatientAvatar(null);
            registrationConfirmPatientCardVo.setPatientIdCard(patient.getIdCard());
            registrationConfirmPatientCardVo.setPatientPhone(desensitizePhone(patient.getPhone()));
            registrationConfirmPatientCardVo.setPatientSex(patient.getSex());
            registrationConfirmPatientCardVo.setPatientCardNo(String.valueOf(patientCard.getId()));
            registrationConfirmPatientCardVo.setRelation(relation.getRelation());
            registrationConfirmPatientCardVo.setDefaultPatientCard(relation.getDefaulted());
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
        List<Long> patientIds = userPatientRelationService.listAccessiblePatientIds(userId);
        if (patientIds.isEmpty()) {
            return Result.success(List.of());
        }

        List<UserPatientRelation> userPatientRelations =
                userPatientRelationService.getUserPatientRelationsByUserId(userId);
        Map<Long, UserPatientRelation> relationMap = userPatientRelations.stream()
                .filter(relation -> relation.getPatientId() != null)
                .collect(Collectors.toMap(UserPatientRelation::getPatientId,
                        Function.identity(), (left, right) -> left));
        List<Patient> patients = patientService.getPatientsByIds(patientIds);
        Map<Long, Patient> patientMap = patients.stream()
                .filter(patient -> patient.getId() != null)
                .collect(Collectors.toMap(Patient::getId, Function.identity(), (left, right) -> left));
        Map<Long, PatientCard> patientCardMap = loadPatientCardMap(patients);
        List<PatientCardSimpleResponse> result = new ArrayList<>();

        for (Long patientId : patientIds) {
            Patient patient = patientMap.get(patientId);
            UserPatientRelation relation = relationMap.get(patientId);
            PatientCard patientCard = patient == null ? null : patientCardMap.get(patient.getPatientCardId());
            if (patient == null || relation == null || patientCard == null) {
                throw new BizException(BizErrorCode.PATIENT_DATA_INVALID);
            }

            PatientCardSimpleResponse item = new PatientCardSimpleResponse();
            item.setUserId(String.valueOf(relation.getUserId()));
            item.setUserPatientRelationId(String.valueOf(relation.getId()));
            item.setPatientId(String.valueOf(patient.getId()));
            item.setPatientName(patient.getName());
            item.setPatientIdCard(patient.getIdCard());
            item.setPatientPhone(desensitizePhone(patient.getPhone()));
            item.setPatientSex(patient.getSex());
            item.setPatientCardSn(String.valueOf(patientCard.getSn()));
            item.setPatientCardId(String.valueOf(patientCard.getId()));
            item.setRelation(relation.getRelation());
            item.setDefaultPatient(relation.getDefaulted());
            item.setRemark(relation.getRemark());
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
        if (relation == null
                || !currentUserId.equals(relation.getUserId())
                || relation.getPatientId() == null
                || !userPatientRelationService.hasPatientRelation(currentUserId, relation.getPatientId())) {
            throw new BizException(BizErrorCode.PATIENT_DATA_INVALID);
        }
        Patient patient = patientService.getPatientById(relation.getPatientId());
        PatientCard patientCard = getPatientCard(patient);
        if (patient == null || patientCard == null) {
            throw new BizException(BizErrorCode.PATIENT_DATA_INVALID);
        }

        PatientCardSimpleResponse result = new PatientCardSimpleResponse();
        result.setUserId(String.valueOf(relation.getUserId()));
        result.setUserPatientRelationId(String.valueOf(relation.getId()));
        result.setPatientId(String.valueOf(patient.getId()));
        result.setPatientName(patient.getName());
        result.setPatientIdCard(patient.getIdCard());
        result.setPatientPhone(patient.getPhone());
        result.setPatientSex(patient.getSex());
        result.setPatientCardSn(String.valueOf(patientCard.getSn()));
        result.setPatientCardId(String.valueOf(patientCard.getId()));
        result.setRelation(relation.getRelation());
        result.setDefaultPatient(relation.getDefaulted());
        result.setRemark(relation.getRemark());
        return Result.success(result);
    }

    /**
     * 获取当前用户本人就诊卡完善状态与回填信息
     * @param currentUserId 当前登录用户ID
     * @return 本人就诊卡状态
     */
    public Result<SelfPatientCardStatus> getSelfPatientCardStatus(Long currentUserId) {
        Patient patient = patientService.getPatientByOwnerUserId(currentUserId);
        UserPatientRelation selfRelation = patient == null
                ? null
                : userPatientRelationService.getByUserIdAndPatientId(currentUserId, patient.getId());
        if (selfRelation != null && !SELF_RELATION.equals(selfRelation.getRelation())) {
            selfRelation = null;
        }
        PatientCard patientCard = getPatientCard(patient);
        boolean completed = selfRelation != null && patient != null && patientCard != null;

        SelfPatientCardStatus result = new SelfPatientCardStatus();
        result.setCompleted(completed);
        result.setRelationId(selfRelation == null ? null : String.valueOf(selfRelation.getId()));
        result.setPatientId(patient == null ? null : String.valueOf(patient.getId()));
        result.setPatientName(patient == null ? null : patient.getName());
        result.setPatientIdCard(patient == null ? null : patient.getIdCard());
        result.setPatientPhone(patient == null ? null : patient.getPhone());
        result.setPatientSex(patient == null ? null : patient.getSex());
        result.setPatientCardSn(patientCard == null ? null : String.valueOf(patientCard.getSn()));
        result.setRemark(selfRelation == null ? null : selfRelation.getRemark());
        result.setDefaultPatient(selfRelation == null || Boolean.TRUE.equals(selfRelation.getDefaulted()));
        return Result.success(result);
    }

    /**
     * 批量加载患者对应的就诊卡
     */
    private Map<Long, PatientCard> loadPatientCardMap(List<Patient> patients) {
        List<Long> patientCardIds = patients.stream()
                .map(Patient::getPatientCardId)
                .filter(patientCardId -> patientCardId != null)
                .distinct()
                .toList();
        if (patientCardIds.isEmpty()) {
            return Map.of();
        }
        return patientCardService.getPatientCardsByIds(patientCardIds).stream()
                .filter(patientCard -> patientCard.getId() != null)
                .collect(Collectors.toMap(PatientCard::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 按患者当前就诊卡ID加载就诊卡
     */
    private PatientCard getPatientCard(Patient patient) {
        if (patient == null || patient.getPatientCardId() == null) {
            return null;
        }
        return patientCardService.getPatientCardById(patient.getPatientCardId());
    }

    /**
     * 脱敏患者手机号
     */
    private String desensitizePhone(String phone) {
        return phone == null || phone.isBlank() ? phone : DesensitizedUtil.mobilePhone(phone);
    }

    /**
     * Long 转字符串，保留 null
     */
    private String toStringValue(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
