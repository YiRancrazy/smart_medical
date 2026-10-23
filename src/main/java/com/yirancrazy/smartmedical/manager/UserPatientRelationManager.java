package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.PatientIdentityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户患者关系Manager
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class UserPatientRelationManager {

    private final UserPatientRelationService userPatientRelationService;
    private final AccountService accountService;
    private final UserService userService;
    private final PatientCardService patientCardService;
    private final PatientService patientService;
    private final RegistrationService registrationService;

    /** 本人关系文案，前端 picker 直接提交中文，与库中存量数据保持一致 */
    private static final String SELF_RELATION = "本人";


    /**
     * 添加就诊人
     * @param name 就诊人姓名
     * @param idCard 就诊人身份证号
     * @param phone 就诊人手机号
     * @param relation 用户与就诊人关系
     * @param defaulted 是否默认就诊人
     * @return 添加结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> insertUserPatientRelation(Long currentUserId,String name, String idCard, String phone, String relation, String remark, String defaulted) {
        String normalizedIdCard = PatientIdentityUtil.normalizeIdCard(idCard);
        if (!PatientIdentityUtil.isValidIdCard(normalizedIdCard)) {
            return Result.fail("身份证号格式不正确");
        }
        String normalizedPhone = phone == null ? null : phone.trim();
        if (normalizedPhone == null || normalizedPhone.isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        if (!normalizedPhone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }
        String normalizedName = name == null ? null : name.trim();
        if (normalizedName == null || normalizedName.isEmpty()) {
            return Result.fail("姓名不能为空");
        }

        boolean selfBinding = SELF_RELATION.equals(relation);
        User currentUser = null;
        if (selfBinding) {
            List<Account> patientAccounts = accountService.listPatientAccountsByUserId(currentUserId);
            patientAccounts = patientAccounts == null ? List.of() : patientAccounts;
            List<Account> enabledPatientAccounts = patientAccounts.stream()
                    .filter(account -> !Boolean.FALSE.equals(account.getEnabled()))
                    .toList();
            if (enabledPatientAccounts.isEmpty()) {
                return Result.fail("当前账号缺少可用的患者角色账号");
            }
            if (enabledPatientAccounts.size() > 1) {
                return Result.fail("当前用户存在多个有效患者角色账号");
            }
            Account patientAccount = enabledPatientAccounts.get(0);
            if (!normalizedPhone.equals(patientAccount.getPhone())) {
                return Result.fail("本人关系手机号需与当前账号一致");
            }
            Patient ownedPatient = patientService.getPatientByOwnerUserId(currentUserId);
            if (ownedPatient != null && !normalizedIdCard.equals(ownedPatient.getIdCard())) {
                return Result.fail("当前账号已绑定其他本人患者");
            }
            currentUser = userService.getUserById(currentUserId);
            if (currentUser == null) {
                return Result.fail("当前账号不存在");
            }
        }

        Patient patient = patientService.getPatientByIdCard(normalizedIdCard);
        if (patient == null) {
            patient = createPatientOrReuse(currentUserId, normalizedName, normalizedIdCard, normalizedPhone);
        }

        UserPatientRelation existingRelation =
                userPatientRelationService.getByUserIdAndPatientId(currentUserId, patient.getId());
        if (existingRelation != null) {
            return Result.fail("该用户已存在该就诊人");
        }

        if (selfBinding) {
            if (patient.getOwnerUserId() != null && !currentUserId.equals(patient.getOwnerUserId())) {
                return Result.fail("该身份证已被其他账号绑定");
            }
            if (!normalizedPhone.equals(patient.getPhone())) {
                return Result.fail("本人关系手机号需与患者档案一致");
            }
            if (patient.getOwnerUserId() == null) {
                patient.setOwnerUserId(currentUserId);
                patientService.updatePatientById(patient);
            }
            currentUser.setIdCard(patient.getIdCard());
            currentUser.setSex(patient.getSex());
            userService.updateUserById(currentUser);
        }

        if (Boolean.parseBoolean(defaulted)) {
            List<UserPatientRelation> relations =
                    userPatientRelationService.getUserPatientRelationsByUserId(currentUserId);
            UserPatientRelation defaultRelation = relations.stream()
                    .filter(UserPatientRelation::getDefaulted)
                    .findFirst()
                    .orElse(null);
            if (defaultRelation != null) {
                defaultRelation.setDefaulted(false);
                userPatientRelationService.updateUserPatientRelationById(defaultRelation);
            }
        }

        UserPatientRelation userPatientRelation = new UserPatientRelation();
        userPatientRelation.setId(IdUtil.getSnowflakeNextId());
        userPatientRelation.setUserId(currentUserId);
        userPatientRelation.setPatientId(patient.getId());
        userPatientRelation.setDefaulted(Boolean.parseBoolean(defaulted));
        userPatientRelation.setRelation(relation);
        userPatientRelation.setRemark(remark);

        try {
            return Result.success(userPatientRelationService.insertUserPatientRelation(userPatientRelation));
        } catch (DuplicateKeyException e) {
            throw new BizException(BizErrorCode.PATIENT_RELATION_ALREADY_EXISTS);
        }
    }

    /**
     * 修改就诊人信息
     * @param id 就诊人id
     * @param name 姓名
     * @param phone 手机号
     * @param idCard 身份证号
     * @param relation 用户与就诊人关系
     * @param remark 备注
     * @param defaulted 是否默认就诊人
     * @return 修改结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> updateUserPatientRelationById(Long currentUserId, Long id,
                                                         String name, String phone, String idCard,
                                                         String relation, String remark, String defaulted) {
        UserPatientRelation userPatientRelation = userPatientRelationService.getUserPatientRelationById(id);
        if (userPatientRelation == null) {
            return Result.fail("就诊人不存在");
        }
        if (!currentUserId.equals(userPatientRelation.getUserId())) {
            return Result.fail("无权修改该就诊人");
        }

        Patient patient = patientService.getPatientById(userPatientRelation.getPatientId());
        if (patient == null) {
            return Result.fail("患者档案不存在");
        }
        boolean selfRelation = SELF_RELATION.equals(userPatientRelation.getRelation())
                || currentUserId.equals(patient.getOwnerUserId());
        if (selfRelation && !SELF_RELATION.equals(relation)) {
            return Result.fail("本人关系不可修改");
        }
        if (!selfRelation && SELF_RELATION.equals(relation)) {
            return Result.fail("本人关系只能通过本人绑定创建");
        }

        String normalizedIdCard = PatientIdentityUtil.normalizeIdCard(idCard);
        if (!PatientIdentityUtil.isValidIdCard(normalizedIdCard)) {
            return Result.fail("身份证号格式不正确");
        }
        if (!normalizedIdCard.equals(patient.getIdCard())) {
            if (patient.getOwnerUserId() != null) {
                return Result.fail("已绑定本人患者的身份证不可修改");
            }
            Patient idCardPatient = patientService.getPatientByIdCard(normalizedIdCard);
            if (idCardPatient != null && !idCardPatient.getId().equals(patient.getId())) {
                return Result.fail("该身份证已绑定其他患者");
            }
        }
        String normalizedName = name == null || name.isBlank() ? null : name.trim();
        String normalizedPhone = phone == null || phone.isBlank() ? null : phone.trim();
        if (normalizedPhone != null && !normalizedPhone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }
        boolean canEditPatient = currentUserId.equals(patient.getOwnerUserId())
                || (patient.getOwnerUserId() == null
                    && currentUserId.equals(patient.getCreatedByUserId()));
        boolean patientChanged = (normalizedName != null && !normalizedName.equals(patient.getName()))
                || (normalizedPhone != null && !normalizedPhone.equals(patient.getPhone()))
                || !normalizedIdCard.equals(patient.getIdCard());
        if (!canEditPatient && patientChanged) {
            return Result.fail("无权修改患者身份资料");
        }
        if (selfRelation && normalizedPhone != null) {
            List<Account> patientAccounts = accountService.listPatientAccountsByUserId(currentUserId);
            patientAccounts = patientAccounts == null ? List.of() : patientAccounts;
            List<Account> enabledPatientAccounts = patientAccounts.stream()
                    .filter(account -> !Boolean.FALSE.equals(account.getEnabled()))
                    .toList();
            if (enabledPatientAccounts.size() != 1
                    || !normalizedPhone.equals(enabledPatientAccounts.get(0).getPhone())) {
                return Result.fail("本人关系手机号需与当前账号一致");
            }
        }
        User currentUser = selfRelation ? userService.getUserById(currentUserId) : null;
        if (selfRelation && currentUser == null) {
            return Result.fail("当前账号不存在");
        }

        List<UserPatientRelation> list = userPatientRelationService.getUserPatientRelationsByUserId(currentUserId);
        UserPatientRelation defaultRelation = list.stream()
                .filter(UserPatientRelation::getDefaulted)
                .findFirst()
                .orElse(null);
        UserPatientRelation fallback = null;
        if ("false".equals(defaulted) && defaultRelation != null && defaultRelation.getId().equals(id)) {
            fallback = list.stream()
                    .filter(r -> !r.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (fallback == null) {
                return Result.fail("当前账户不能没有默认就诊人");
            }
        }

        if (canEditPatient && !normalizedIdCard.equals(patient.getIdCard())) {
            patient.setIdCard(normalizedIdCard);
        }
        if (canEditPatient && normalizedName != null) {
            patient.setName(normalizedName);
        }
        if (canEditPatient && normalizedPhone != null) {
            patient.setPhone(normalizedPhone);
        }
        if (canEditPatient) {
            patientService.updatePatientById(patient);
        }
        if (selfRelation) {
            currentUser.setIdCard(patient.getIdCard());
            currentUser.setSex(patient.getSex());
            userService.updateUserById(currentUser);
        }

        if ("true".equals(defaulted) && defaultRelation != null && !defaultRelation.getId().equals(id)) {
            defaultRelation.setDefaulted(false);
            userPatientRelationService.updateUserPatientRelationById(defaultRelation);
        }
        if (fallback != null) {
            fallback.setDefaulted(true);
            userPatientRelationService.updateUserPatientRelationById(fallback);
        }

        userPatientRelation.setRelation(relation);
        userPatientRelation.setRemark(remark);
        userPatientRelation.setDefaulted(Boolean.parseBoolean(defaulted));
        return Result.success(userPatientRelationService.updateUserPatientRelationById(userPatientRelation));
    }

    /**
     * 设置默认就诊人
     * @param currentUserId 当前用户id
     * @param id 就诊人关系id
     * @return 设置结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> setDefaultUserPatientRelation(Long currentUserId, Long id) {
        UserPatientRelation target = userPatientRelationService.getUserPatientRelationById(id);
        if (target == null || !currentUserId.equals(target.getUserId())) {
            return Result.fail("无权操作该就诊人");
        }
        List<UserPatientRelation> list = userPatientRelationService.getUserPatientRelationsByUserId(currentUserId);
        UserPatientRelation userPatientRelation = list.stream()
                .filter(UserPatientRelation::getDefaulted)
                .findFirst()
                .orElse(null);
        if (userPatientRelation != null && !userPatientRelation.getId().equals(id)) {
            userPatientRelation.setDefaulted(false);
            userPatientRelationService.updateUserPatientRelationById(userPatientRelation);
        }
        target.setDefaulted(true);
        return Result.success(userPatientRelationService.updateUserPatientRelationById(target));
    }

    /**
     * 删除就诊人
     * <p>仅逻辑删除当前账号的访问关系，不修改账号、患者档案或就诊卡。</p>
     * @param currentUserId 当前用户id
     * @param id 就诊人关系id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> deleteUserPatientRelationById(Long currentUserId, Long id) {
        UserPatientRelation relation = userPatientRelationService.getUserPatientRelationById(id);
        if (relation == null || !currentUserId.equals(relation.getUserId())) {
            return Result.fail("无权删除该就诊人");
        }
        Patient patient = patientService.getPatientById(relation.getPatientId());
        if (SELF_RELATION.equals(relation.getRelation())
                || (patient != null && currentUserId.equals(patient.getOwnerUserId()))) {
            return Result.fail("本人关系不能删除");
        }
        // 状态 0/1/5/6/7 表示挂号尚未完成
        List<Integer> inTransitStatuses = Arrays.asList(0, 1, 5, 6, 7);
        List<Registration> registrations = registrationService.listRegistrationsByPatientId(relation.getPatientId());
        boolean hasInTransit = registrations.stream()
                .anyMatch(r -> r.getStatus() != null
                        && inTransitStatuses.contains(r.getStatus()));
        if (hasInTransit) {
            return Result.fail("该就诊人存在在途挂号，无法删除");
        }
        return Result.success(userPatientRelationService.deleteUserPatientRelationById(id));
    }

    /**
     * 为未建档患者创建就诊卡及患者档案
     * @param currentUserId 创建账号ID
     * @param name 就诊人姓名
     * @param idCard 规范化身份证号
     * @param phone 手机号
     * @return 新建的患者档案
     */
    private Patient createPatientOrReuse(Long currentUserId, String name, String idCard, String phone) {
        PatientCard patientCard = new PatientCard();
        patientCard.setId(IdUtil.getSnowflakeNextId());
        patientCard.setSn(IdUtil.getSnowflakeNextId());
        patientCard.setInpatientBalance(0);
        patientCard.setOutpatientBalance(0);
        patientCard.setPaymentPassword("");
        patientCard.setStatus(0);
        patientCard.setQrCode("");

        Patient patient = new Patient();
        patient.setId(IdUtil.getSnowflakeNextId());
        patient.setName(name);
        patient.setIdCard(idCard);
        patient.setPhone(phone);
        patient.setCreatedByUserId(currentUserId);
        patient.setPatientCardId(patientCard.getId());
        patient.setEmergencyPhone(phone);
        try {
            // 先插患者身份证唯一索引，冲突时不会遗留孤立就诊卡
            patientService.insertPatient(patient);
        } catch (DuplicateKeyException e) {
            Patient existingPatient = patientService.getPatientByIdCardForUpdate(idCard);
            if (existingPatient == null) {
                throw e;
            }
            return existingPatient;
        }
        patientCardService.insertPatientCard(patientCard);
        return patient;
    }
}
