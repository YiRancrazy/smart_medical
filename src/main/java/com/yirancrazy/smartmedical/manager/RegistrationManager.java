package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
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
    private final OrderService orderService;
    private final OrderTypeService orderTypeService;
    private final OrderItemService orderItemService;
    private final RegistrationScheduleService registrationScheduleService;
    private final DoctorService doctorService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final DoctorPositionService doctorPositionService;


    public int addRegistration(Registration registration) {
        registration.setId(IdUtil.getSnowflakeNextId());
        return registrationService.insertRegistration(registration);
    }

    public Registration getRegistrationById(Long id) {
        return registrationService.getRegistrationById(id);
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
        Long userId = Long.parseLong(redisUtil.get("uid_" + uid));


        PatientCard patientCard = patientCardService.getPatientCardById(patientCardId);  // 获取患者卡信息
        Patient patient = patientService.getPatientByPatientCardId(patientCardId); // 获取患者信息
        RegistrationSchedule registrationSchedule = registrationScheduleService.getRegistrationScheduleById(registrationScheduleId); // 获取排班信息

        // 判断该用户是否已经预约过
        Registration existRegistration = registrationService
                .getRegistrationByRegistrationScheduleTemplateIdAndUserId(registrationSchedule
                        .getRegistrationScheduleTemplateId(), patient.getUserId());
        log.warn("existRegistration: " + existRegistration);
        if(existRegistration != null){
            return Result.fail("该用户已经挂号");
        }



        if (registrationSchedule.getRemainingQuota() > 0) {
            registrationSchedule.setRemainingQuota(registrationSchedule.getRemainingQuota() - 1);
            Integer updated = registrationScheduleService.updateRegistrationScheduleById(registrationSchedule);
            if (updated == null || updated <= 0) {
                return Result.fail("号源扣减失败");
            }
        }else {
            return Result.fail("该排班已无号源");
        }

        order.setId(IdUtil.getSnowflakeNextId());
        order.setSn(IdUtil.getSnowflakeNextId());
        order.setUserId(patient.getUserId());
        order.setOrderTypeId(1L);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setTotalAmount(0);
        order.setOrderCreateTime(LocalDateTime.now());
        order.setOrderUpdateTime(LocalDateTime.now());

        registration.setId(IdUtil.getSnowflakeNextId());
        registration.setUserId(patient.getUserId());
        registration.setRegistrationScheduleTemplateId(registrationSchedule.getRegistrationScheduleTemplateId());
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

        return Result.success(String.valueOf(order.getSn()));
    }

    /**
     * 获取用户预约列表
     * @param patientId 用户id
     * @return 用户预约信息简单响应列表
     */
    public Result<List<AppointmentResponseSimple>> getRegistrationByUid(Long patientId) {

//        // 根据患者id 获取用户信息
//        User user = userService.getUserById(patientId);
//
//        // 根据患者id 获取患者的预约信息
//        List<Registration> registrationList = registrationService.listRegistrationsByUserId(patientId);
//
//        // 根据预约信息中的医生id 获取医生信息
//        List<Doctor> doctorList = doctorService.listDoctorsByIds(registrationList
//                .stream()
//                .map(Registration::getDoctorId)
//                .toList());
//
//        // 根据医生id 获取科室信息
//        List<Department> departmentList = departmentService.listDepartmentsByIds(doctorList
//                .stream()
//                .map(Doctor::getDepartmentId)
//                .toList());
//
//        // 根据医生id 获取医生职位信息
//        List<DoctorPosition> doctorPositionList = doctorPositionService.listDoctorPositions();
//
//        // 构建结果
//        List<AppointmentResponseSimple> result = new ArrayList<>();
//        for (Registration registration : registrationList) {
//            AppointmentResponseSimple temp = new AppointmentResponseSimple();
//            temp.setRegistrationId(String.valueOf(registration.getId()));
//            temp.setRegistrationStatus(registration.getStatus());
//
//            Doctor doctor = doctorList
//                    .stream()
//                    .filter(item -> item.getId().equals(registration.getDoctorId()))
//                    .toList()
//                    .get(0);
//
//            DoctorPosition doctorPosition = doctorPositionList
//                    .stream()
//                    .filter(item -> item.getId().equals(doctor.getDoctorPositionId()))
//                    .toList()
//                    .get(0);
//
//            Department department = departmentList
//                    .stream()
//                    .filter(item -> item.getId().equals(doctor.getDepartmentId()))
//                    .toList()
//                    .get(0);
//
//            temp.setAppointmentData("");
//            temp.setAppointmentStartTime("");
//            temp.setAppointmentEndTime("");
//            temp.setDoctorId(String.valueOf(doctor.getId()));
//            temp.setDoctorName(doctor.getName());
//            temp.setDoctorAvatar(doctor.getAvatar());
//            temp.setDoctorPosition(doctorPosition.getName());
//            temp.setDepartmentId(String.valueOf(department.getId()));
//            temp.setDepartmentName(department.getName());
//            temp.setPatientName(user.getNickname());
//            temp.setRegistrationPrice(0.0);
//            result.add(temp);
//        }

//        System.out.println(result);
//
//        return Result.success(result);
        return null;
    }
}
