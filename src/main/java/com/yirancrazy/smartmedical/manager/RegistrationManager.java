package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.mapper.RegistrationScheduleMapper;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AppointmentResponseSimple;
import com.yirancrazy.smartmedical.service.*;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class RegistrationManager {

    private final RegistrationService registrationService;
    private final RedisUtil redisUtil;
    private final PatientCardService patientCardService;
    private final PatientService patientService;
    private final PatientManager patientManager;
    private final OrderService orderService;
    private final OrderTypeService orderTypeService;
    private final OrderItemService orderItemService;
    private final RegistrationScheduleService registrationScheduleService;
    private final RegistrationScheduleMapper registrationScheduleMapper;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;
    private final DoctorService doctorService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final DoctorPositionService doctorPositionService;


    public Result<Integer> addRegistration(Registration registration) {
        registration.setId(IdUtil.getSnowflakeNextId());
        return Result.success(registrationService.insertRegistration(registration));
    }

    public Result<Registration> getRegistrationById(Long id) {
        return Result.success(registrationService.getRegistrationById(id));
    }

    /**
     * 添加挂号
     * @param paymentId 支付id
     * @param registrationScheduleId 排班id
     * @param uid 用户id
     * @param patientCardId 患者卡id
     * @return 挂号结果
     */
    @Transactional
    public Result<String> addRegistration(Long paymentId, Long registrationScheduleId, Long uid, Long patientCardId){
        Registration registration = new Registration();
        Order order = new Order();
        Long userId = uid;

        PatientCard patientCard = patientCardService.getPatientCardById(patientCardId);  // 获取患者卡信息
        if (patientCard == null) {
            return Result.fail("患者卡不存在");
        }
        Patient patient = patientService.getPatientByPatientCardId(patientCardId); // 获取患者信息
        if (patient == null) {
            return Result.fail("患者信息不存在");
        }
        RegistrationSchedule registrationSchedule = registrationScheduleService.getRegistrationScheduleById(registrationScheduleId); // 获取排班信息
        if (registrationSchedule == null) {
            return Result.fail("排班信息不存在");
        }

        // 判断该用户是否已经预约过
        Registration existRegistration = registrationService
                .getRegistrationByRegistrationScheduleIdAndUserId(registrationSchedule
                        .getId(), patient.getUserId());
        log.warn("existRegistration: " + existRegistration);
        if(existRegistration != null){
            return Result.fail("该用户已经挂号");
        }



        // 原子扣减号源：WHERE remaining_quota > 0 防止并发超卖
        int deducted = registrationScheduleMapper.update(null,
                new UpdateWrapper<RegistrationSchedule>()
                        .eq("id", registrationScheduleId)
                        .gt("remaining_quota", 0)
                        .setSql("remaining_quota = remaining_quota - 1"));
        if (deducted == 0) {
            return Result.fail("该排班已无号源");
        }

        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(registrationSchedule.getRegistrationScheduleTemplateId());
        int price = registrationScheduleTemplate == null || registrationScheduleTemplate.getPrice() == null
                ? 0 : registrationScheduleTemplate.getPrice();

        order.setId(IdUtil.getSnowflakeNextId());
        order.setSn(IdUtil.getSnowflakeNextId());
        order.setUserId(patient.getUserId());
        order.setOrderTypeId(1L);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setTotalAmount(price);
        order.setOrderCreateTime(LocalDateTime.now());
        order.setOrderUpdateTime(LocalDateTime.now());

        registration.setId(IdUtil.getSnowflakeNextId());
        registration.setUserId(patient.getUserId());
        registration.setRegistrationScheduleId(registrationSchedule.getId());
        registration.setOrderId(order.getId());
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode());

        registrationService.insertRegistration(registration);
        orderService.insertOrder(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(IdUtil.getSnowflakeNextId());
        orderItem.setOrderId(order.getId());
        orderItem.setProductionId(registration.getId());
        orderItem.setProductionTypeId(1L);
        orderItem.setQuantity(1);
        orderItemService.insertOrderItem(orderItem);

        return Result.success(String.valueOf(order.getId()));
    }

    /**
     * 获取用户预约列表
     * @param currentUserId 当前登录用户id
     * @param patientCardId 就诊卡id（为 null 时返回全部关联就诊人）
     * @return 用户预约信息简单响应列表
     */
    public Result<List<AppointmentResponseSimple>> getRegistrationByUid(Long currentUserId, Long patientCardId) {
        List<Long> patientUserIds = patientManager.getAccessiblePatientUserIds(currentUserId, patientCardId);
        List<Registration> registrationList = registrationService.listRegistrationsByUserIds(patientUserIds);

        List<AppointmentResponseSimple> result = new ArrayList<>();
        if (registrationList == null || registrationList.isEmpty()) {
            return Result.success(result);
        }

        for (Registration registration : registrationList) {
            AppointmentResponseSimple item = new AppointmentResponseSimple();
            item.setId(String.valueOf(registration.getId()));
            item.setOrderId(registration.getOrderId() == null ? "" : String.valueOf(registration.getOrderId()));
            item.setStatus(registration.getStatus());
            item.setRegistrationPrice(0.0);

            User patientUser = userService.getUserById(registration.getUserId());
            item.setPatientName(patientUser == null ? "" : patientUser.getNickname());

            if (registration.getRegistrationScheduleId() == null) {
                log.warn("跳过挂号 {}：未关联排班", registration.getId());
                continue;
            }
            RegistrationSchedule schedule = registrationScheduleService
                    .getRegistrationScheduleById(registration.getRegistrationScheduleId());
            if (schedule == null || schedule.getRegistrationScheduleTemplateId() == null) {
                log.warn("跳过挂号 {}：未找到排班 {}", registration.getId(),
                        registration.getRegistrationScheduleId());
                continue;
            }
            RegistrationScheduleTemplate template = registrationScheduleTemplateService
                    .getRegistrationScheduleTemplateById(schedule.getRegistrationScheduleTemplateId());
            if (template == null || template.getDoctorId() == null) {
                log.warn("跳过挂号 {}：未找到排班模板 {}", registration.getId(),
                        schedule.getRegistrationScheduleTemplateId());
                continue;
            }
            Doctor doctor = doctorService.getDoctorById(template.getDoctorId());
            if (doctor == null) {
                log.warn("跳过挂号 {}：未找到医生 {}", registration.getId(), template.getDoctorId());
                continue;
            }
            Department department = doctor.getDepartmentId() == null
                    ? null
                    : departmentService.getDepartmentById(doctor.getDepartmentId());
            DoctorPosition position = doctor.getDoctorPositionId() == null
                    ? null
                    : doctorPositionService.getPositionById(doctor.getDoctorPositionId());

            item.setScheduleDate(template.getRegistrationDate() == null ? "" : template.getRegistrationDate().toString());
            item.setScheduleTime(template.getStartTime() == null ? "" : template.getStartTime().toString());
            item.setRegistrationPrice(template.getPrice() == null ? 0.0 : template.getPrice() / 100.0);
            item.setDoctorId(String.valueOf(doctor.getId()));
            item.setDoctorName(doctor.getName());
            item.setDoctorAvatar(doctor.getAvatar());
            item.setDoctorPosition(position == null ? "" : position.getName());
            item.setDepartmentId(department == null ? "" : String.valueOf(department.getId()));
            item.setDepartmentName(department == null ? "" : department.getName());
            result.add(item);
        }
        return Result.success(result);
    }
}
