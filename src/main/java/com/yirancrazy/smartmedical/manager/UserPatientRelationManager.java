package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.vo.OutPatientCardBaseInfo;
import com.yirancrazy.smartmedical.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private static final Long USER_ROLE = 4L;


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
    public Result<Integer> insertUserPatientRelation(Long currentUserId,String name, String idCard, String phone, String relation, String remark, String defaulted) {

        Long id = IdUtil.getSnowflakeNextId();
        User patientUser = userService.getUserByIdCard(idCard);
        if (patientUser == null) {
            List<Account> accounts = accountService.getAccountByPhone(phone);
            if (accounts != null && !accounts.isEmpty()) {
                patientUser = userService.getUserById(accounts.get(0).getUserId());
            } else {
                patientUser = createPatientUser(name, idCard, phone);
            }
        }
        Long patientUserId = patientUser.getId();
        Integer isAuthorized = 0;

        // 查询用户关系是否存在
        List<UserPatientRelation> userPatientRelationList = userPatientRelationService.getUserPatientRelationsByUserId(currentUserId);
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
            List<UserPatientRelation> list = userPatientRelationService.getUserPatientRelationsByUserId(currentUserId);
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
        userPatientRelation.setUserId(currentUserId);
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
     * @param name 姓名
     * @param phone 手机号
     * @param idCard 身份证号
     * @param relation 用户与就诊人关系
     * @param remark 备注
     * @param defaulted 是否默认就诊人
     * @return 修改结果
     */
    @Transactional
    public Result<Integer> updateUserPatientRelationById(Long currentUserId, Long id,
                                                         String name, String phone, String idCard,
                                                         String relation, String remark, String defaulted) {
        UserPatientRelation userPatientRelationById = userPatientRelationService.getUserPatientRelationById(id);
        if (userPatientRelationById == null) {
            return Result.fail("就诊人不存在");
        }
        // 校验当前用户是否有权修改
        if (!currentUserId.equals(userPatientRelationById.getUserId())) {
            return Result.fail("无权修改该就诊人");
        }

        List<UserPatientRelation> list = userPatientRelationService.getUserPatientRelationsByUserId(currentUserId);
        UserPatientRelation defaultRelation = list.stream().filter(UserPatientRelation::getDefaulted).findFirst().orElse(null);

        if ("true".equals(defaulted) && defaultRelation != null && !defaultRelation.getId().equals(id)) {
            defaultRelation.setDefaulted(false);
            userPatientRelationService.updateUserPatientRelationById(defaultRelation);
        }
        // S18: 取消当前默认就诊人时，自动切换到另一个就诊人为默认；无其他就诊人才拒绝
        if ("false".equals(defaulted) && defaultRelation != null && defaultRelation.getId().equals(id)) {
            UserPatientRelation fallback = list.stream()
                    .filter(r -> !r.getId().equals(id))
                    .findFirst().orElse(null);
            if (fallback == null) {
                return Result.fail("当前账户不能没有默认就诊人");
            }
            fallback.setDefaulted(true);
            userPatientRelationService.updateUserPatientRelationById(fallback);
        }

        // 更新用户信息（姓名、手机号、身份证号）
        User patientUser = userService.getUserById(userPatientRelationById.getPatientUserId());
        if (patientUser != null) {
            if (name != null && !name.isEmpty()) {
                patientUser.setNickname(name);
                patientUser.setUsername(name);
            }
            if (idCard != null && !idCard.isEmpty()) {
                patientUser.setIdCard(idCard);
            }
            userService.updateUserById(patientUser);
        }
        if (phone != null && !phone.isEmpty()) {
            Account account = accountService.getAccountByUserId(userPatientRelationById.getPatientUserId());
            if (account != null) {
                // B16: 改手机号前先查重，避免命中 account.phone 唯一索引抛 SQL 异常被全局吞为 500
                if (!phone.equals(account.getPhone())) {
                    List<Account> existing = accountService.getAccountByPhone(phone);
                    boolean conflict = existing.stream()
                            .anyMatch(a -> !a.getId().equals(account.getId()));
                    if (conflict) {
                        return Result.fail("该手机号已被其他账号占用");
                    }
                }
                account.setPhone(phone);
                accountService.updateAccountById(account);
            }
        }

        userPatientRelationById.setRelation(relation);
        userPatientRelationById.setRemark(remark);
        userPatientRelationById.setDefaulted(Boolean.valueOf(defaulted));
        return Result.success(userPatientRelationService.updateUserPatientRelationById(userPatientRelationById));
    }

    /**
     * 设置默认就诊人
     * @param currentUserId 当前用户id
     * @param id 就诊人关系id
     * @return 设置结果
     */
    @Transactional
    public Result<Integer> setDefaultUserPatientRelation(Long currentUserId, Long id) {
        UserPatientRelation userPatientRelation = null;
        List<UserPatientRelation> list = userPatientRelationService.getUserPatientRelationsByUserId(currentUserId);
        // 获取当前用户下所有就诊人关系，目的为了获取默认就诊人
        userPatientRelation = list.stream().filter(UserPatientRelation::getDefaulted).findFirst().orElse(null);
        if (userPatientRelation != null) {
            userPatientRelation.setDefaulted(false);
            userPatientRelationService.updateUserPatientRelationById(userPatientRelation);
        }
        // 校验目标就诊人关系归属当前用户
        UserPatientRelation target = userPatientRelationService.getUserPatientRelationById(id);
        if (target == null || !currentUserId.equals(target.getUserId())) {
            return Result.fail("无权操作该就诊人");
        }
        target.setDefaulted(true);
        return Result.success(userPatientRelationService.updateUserPatientRelationById(target));
    }

    /**
     * 删除就诊人
     * @param currentUserId 当前用户id
     * @param id 就诊人关系id
     * @return 删除结果
     */
    public Result<Integer> deleteUserPatientRelationById(Long currentUserId, Long id) {
        UserPatientRelation relation = userPatientRelationService.getUserPatientRelationById(id);
        if (relation == null || !currentUserId.equals(relation.getUserId())) {
            return Result.fail("无权删除该就诊人");
        }
        // 检查该就诊人是否存在在途挂号（未完成就诊流程）：状态 0/1/5/6/7
        List<Integer> inTransitStatuses = Arrays.asList(0, 1, 5, 6, 7);
        List<Registration> registrations = registrationService.listRegistrationsByUserId(relation.getPatientUserId());
        boolean hasInTransit = registrations.stream()
                .anyMatch(r -> r.getStatus() != null && inTransitStatuses.contains(r.getStatus()));
        if (hasInTransit) {
            return Result.fail("该就诊人存在在途挂号，无法删除");
        }
        return Result.success(userPatientRelationService.deleteUserPatientRelationById(id));
    }

    /**
     * 为未注册的就诊人创建用户、账户、就诊卡及患者档案
     * @param name 就诊人姓名
     * @param idCard 身份证号
     * @param phone 手机号
     * @return 新建的用户
     */
    private User createPatientUser(String name, String idCard, String phone) {
        User user = new User();
        Long userId = IdUtil.getSnowflakeNextId();
        user.setId(userId);
        user.setNickname(name);
        user.setUsername(name);
        user.setIdCard(idCard);
        user.setAvatar("");
        userService.insertUser(user);

        Account account = new Account();
        account.setId(IdUtil.getSnowflakeNextId());
        account.setUserId(userId);
        account.setRoleId(USER_ROLE);
        account.setPhone(phone);
        // 随机 6 位数字初始密码，避免手机号后 6 位可预测导致账号被冒用
        String initPwd = RandomUtil.randomNumbers(6);
        account.setPassword(BCrypt.hashpw(initPwd, BCrypt.gensalt()));
        account.setEnabled(true);
        // 初始密码仅通过日志通知操作者，由操作者线下转交就诊人；后续应接短信通知
        log.warn("[createPatientUser] 新建就诊人账号 phone={} 初始密码={}", phone, initPwd);
        accountService.insertAccount(account);

        PatientCard patientCard = new PatientCard();
        patientCard.setId(IdUtil.getSnowflakeNextId());
        patientCard.setSn(IdUtil.getSnowflakeNextId());
        patientCard.setInpatientBalance(0);
        patientCard.setOutpatientBalance(0);
        patientCard.setPaymentPassword("");
        patientCard.setStatus(0);
        patientCard.setQrCode("");
        patientCardService.insertPatientCard(patientCard);

        Patient patient = new Patient();
        patient.setId(IdUtil.getSnowflakeNextId());
        patient.setUserId(userId);
        patient.setPatientCardId(patientCard.getId());
        patient.setEmergencyPhone(phone);
        patientService.insertPatient(patient);

        return user;
    }
}
