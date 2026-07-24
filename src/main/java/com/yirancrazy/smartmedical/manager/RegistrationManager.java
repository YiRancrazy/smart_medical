package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.mapper.RegistrationScheduleMapper;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AppointmentResponseSimple;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
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
    private final RegistrationStatusLogService registrationStatusLogService;


    public Result<AppointmentResponseSimple> getRegistrationById(Long id, Long currentUserId) {
        Registration reg = registrationService.getRegistrationById(id);
        if (reg == null) {
            return Result.fail("挂号记录不存在");
        }
        // 校验当前用户对该挂号记录的就诊人有访问权限
        List<Long> accessibleUserIds = patientManager.getAccessiblePatientUserIds(currentUserId, null);
        if (!accessibleUserIds.contains(reg.getUserId())) {
            return Result.fail("无权查看该挂号记录");
        }
        AppointmentResponseSimple vo = convertToAppointmentResponseSimple(reg);
        return vo == null ? Result.fail("挂号关联信息不完整") : Result.success(vo);
    }

    /**
     * 添加挂号
     * @param registrationScheduleId 排班id
     * @param uid 用户id
     * @param patientCardId 患者卡id
     * @return 挂号结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addRegistration(Long registrationScheduleId, Long uid, Long patientCardId){
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

        // B22: 重复挂号校验以 patient.userId(就诊人维度)为粒度，允许同一账号代多名家属挂同一排班。
        // 业务规则若改为"每账号每排班限一个号"，将 patient.getUserId() 改为 uid 即可。
        Registration existRegistration = registrationService
                .getRegistrationByRegistrationScheduleIdAndUserId(registrationSchedule
                        .getId(), patient.getUserId());
        log.warn("existRegistration: " + existRegistration);
        if(existRegistration != null){
            return Result.fail("该就诊人已挂号此排班");
        }



        // 原子扣减号源：WHERE remaining_quota > 0 防止并发超卖；扣减后为 0 则置为已满(2)
        int deducted = registrationScheduleMapper.update(null,
                new UpdateWrapper<RegistrationSchedule>()
                        .eq("id", registrationScheduleId)
                        .gt("remaining_quota", 0)
                        .setSql("remaining_quota = remaining_quota - 1")
                        .setSql("status = IF(remaining_quota - 1 = 0, 2, status)"));
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
        // S03: 写挂号创建日志（from=null, to=WAITING_FOR_PAYMENT）
        registrationStatusLogService.writeLog(
                registration.getId(),
                null,
                RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(),
                userId,
                "user",
                "挂号创建");
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
     * 获取用户预约列表（支持可选分页，U27）
     * ponytail: G13 已知限制 — 逐条查 user/schedule/template/doctor/department/position（N+1），
     *           用户挂号记录通常 < 100，当前可接受；数据量增大时改为批量 listByIds + Map 缓存
     * @param currentUserId 当前登录用户id
     * @param patientCardId 就诊卡id（为 null 时返回全部关联就诊人）
     * @param pageNum 页码（可选，传则分页）
     * @param pageSize 每页条数（可选，传则分页）
     * @return 用户预约信息（PageResult 包含 list + total；未传分页参数时 total = list.size）
     */
    public Result<PageResult<AppointmentResponseSimple>> getRegistrationByUid(
            Long currentUserId, Long patientCardId, Integer pageNum, Integer pageSize) {
        List<Long> patientUserIds = patientManager.getAccessiblePatientUserIds(currentUserId, patientCardId);
        if (patientUserIds == null || patientUserIds.isEmpty()) {
            return Result.success(new PageResult<>(1, pageSize == null ? 0 : pageSize, 0L, 0, new ArrayList<>()));
        }
        // U27: 可选分页，PageHelper 拦截下一条 MyBatis 查询
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Registration> registrationList = registrationService.listRegistrationsByUserIds(patientUserIds);
        PageInfo<Registration> pageInfo = new PageInfo<>(registrationList);

        List<AppointmentResponseSimple> result = new ArrayList<>();
        if (registrationList == null || registrationList.isEmpty()) {
            return Result.success(new PageResult<>(pageInfo, result));
        }

        for (Registration registration : registrationList) {
            AppointmentResponseSimple item = convertToAppointmentResponseSimple(registration);
            if (item != null) {
                result.add(item);
            }
        }
        return Result.success(new PageResult<>(pageInfo, result));
    }

    /**
     * 将 Registration 实体转为前端展示 VO（U14 详情/报到共用）
     * @param registration 挂号实体
     * @return 展示 VO，关联信息不完整时返回 null
     */
    private AppointmentResponseSimple convertToAppointmentResponseSimple(Registration registration) {
        AppointmentResponseSimple item = new AppointmentResponseSimple();
        item.setId(String.valueOf(registration.getId()));
        item.setOrderId(registration.getOrderId() == null ? "" : String.valueOf(registration.getOrderId()));
        item.setStatus(registration.getStatus());
        item.setRegistrationPrice(0.0);

        User patientUser = userService.getUserById(registration.getUserId());
        item.setPatientName(patientUser == null ? "" : patientUser.getNickname());

        if (registration.getRegistrationScheduleId() == null) {
            log.warn("跳过挂号 {}：未关联排班", registration.getId());
            return null;
        }
        RegistrationSchedule schedule = registrationScheduleService
                .getRegistrationScheduleById(registration.getRegistrationScheduleId());
        if (schedule == null || schedule.getRegistrationScheduleTemplateId() == null) {
            log.warn("跳过挂号 {}：未找到排班 {}", registration.getId(),
                    registration.getRegistrationScheduleId());
            return null;
        }
        RegistrationScheduleTemplate template = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(schedule.getRegistrationScheduleTemplateId());
        if (template == null || template.getDoctorId() == null) {
            log.warn("跳过挂号 {}：未找到排班模板 {}", registration.getId(),
                    schedule.getRegistrationScheduleTemplateId());
            return null;
        }
        Doctor doctor = doctorService.getDoctorById(template.getDoctorId());
        if (doctor == null) {
            log.warn("跳过挂号 {}：未找到医生 {}", registration.getId(), template.getDoctorId());
            return null;
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
        return item;
    }
}
