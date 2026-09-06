package com.yirancrazy.smartmedical.manager;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminRegistrationScheduleTemplateDetail;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班模板管理器
 * @Datetime: 2026-03-20 18:52
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class RegistrationScheduleTemplateManager {

    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;
    private final RegistrationScheduleService registrationScheduleService;
    private final DoctorService doctorService;
    private final DepartmentService departmentService;

    /**
     * 根据ID获取挂号排班模板信息
     * @param id 挂号排班模板ID
     * @return 挂号排班模板信息
     */
    public Result<RegistrationScheduleTemplate> getRegistrationScheduleTemplateById(Long id) {
        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService.getRegistrationScheduleTemplateById(id);
        return Result.success(registrationScheduleTemplate);
    }

    /**
     * 更新挂号排班模板信息
     * @param registrationScheduleTemplate 挂号排班模板信息
     * @return 更新结果
     */
    public Result<Integer> updateRegistrationScheduleTemplateById(RegistrationScheduleTemplate registrationScheduleTemplate) {
        int result = registrationScheduleTemplateService.updateRegistrationScheduleTemplateById(registrationScheduleTemplate);
        return Result.success(result);
    }

    /**
     * 根据ID删除挂号排班模板
     * @param id 挂号排班模板ID
     * @return 删除结果
     */
    public Result<Integer> deleteRegistrationScheduleTemplateById(Long id) {
        int result = registrationScheduleTemplateService.deleteRegistrationScheduleTemplateById(id);
        return Result.success(result);
    }

    /**
     * 获取所有挂号排班模板列表
     * @return 挂号排班模板列表
     */
    public Result<List<RegistrationScheduleTemplate>> listAllRegistrationScheduleTemplates() {
        List<RegistrationScheduleTemplate> registrationScheduleTemplates = registrationScheduleTemplateService.listAllRegistrationScheduleTemplates();
        return Result.success(registrationScheduleTemplates);
    }

    /**
     * 分页查询挂号排班模板
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Result<PageInfo<AdminRegistrationScheduleTemplateDetail>> listRegistrationScheduleTemplatesByPage(Integer pageNum, Integer pageSize) {

        List<AdminRegistrationScheduleTemplateDetail> registrationScheduleTemplateDetails = new ArrayList<>();

        PageHelper.startPage(pageNum, pageSize);
        List<RegistrationScheduleTemplate> registrationScheduleTemplates = registrationScheduleTemplateService
                .listAllRegistrationScheduleTemplates();
        PageInfo<RegistrationScheduleTemplate> sourcePage = new PageInfo<>(registrationScheduleTemplates);

        List<Long> doctorIdList = registrationScheduleTemplates
                .stream()
                .map(RegistrationScheduleTemplate::getDoctorId)
                .toList();

        List<Long> registrationScheduleIdList = registrationScheduleTemplates
                .stream()
                .map(RegistrationScheduleTemplate::getId)
                .toList();

        List<Doctor> doctorList = doctorIdList.isEmpty() ? List.of() : doctorService.listDoctorsByIds(doctorIdList);
        List<Department> departmentList = departmentService.listAllDepartment();
        List<RegistrationSchedule> registrationScheduleList = registrationScheduleService
                .listRegistrationScheduleByRegistrationScheduleIdList(registrationScheduleIdList);

        for (RegistrationScheduleTemplate item : registrationScheduleTemplates) {
            Doctor doctor = doctorList.stream().filter(item1 -> item1.getId().equals(item.getDoctorId())).findFirst().orElse(null);
            if (doctor == null) {
                // G03: 不跳过，填充空展示，保证 total 与 list 大小一致
                log.warn("排班模板 {} 未关联有效医生，填充空展示", item.getId());
            }
            Department department = doctor == null ? null
                    : departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
            List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

            int remaining = 0;
            for (RegistrationSchedule r : registrationSchedules) {
                if (r.getRemainingQuota() != null) remaining += r.getRemainingQuota();
            }

            registrationScheduleTemplateDetails.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
        }

        PageInfo<AdminRegistrationScheduleTemplateDetail> pageInfo = new PageInfo<>();
        pageInfo.setTotal(sourcePage.getTotal());
        pageInfo.setPageNum(sourcePage.getPageNum());
        pageInfo.setPageSize(sourcePage.getPageSize());
        pageInfo.setPages(sourcePage.getPages());
        pageInfo.setList(registrationScheduleTemplateDetails);

        return Result.success(pageInfo);
    }

    /**
     * 组装管理员端排班模板详情 DTO（医生 / 科室为空时兜底为空串，避免前端展示 null）
     * @param registrationScheduleTemplate 排班模板实体
     * @param doctor 医生实体（可空）
     * @param department 科室实体（可空）
     * @param remaining 剩余号源数
     * @return 排班模板详情 DTO
     */
    public AdminRegistrationScheduleTemplateDetail createAdminRegistrationScheduleTemplateDetail(RegistrationScheduleTemplate registrationScheduleTemplate, Doctor doctor, Department department, Integer remaining) {
        return new AdminRegistrationScheduleTemplateDetail(
                String.valueOf(registrationScheduleTemplate.getId()),
                doctor == null ? "" : String.valueOf(doctor.getId()),
                doctor == null ? "" : doctor.getName(),
                department == null ? "" : String.valueOf(department.getId()),
                department == null ? "" : department.getName(),
                String.valueOf(registrationScheduleTemplate.getRegistrationDate()),
                String.valueOf(registrationScheduleTemplate.getRegistrationType()),
                String.valueOf(registrationScheduleTemplate.getStartTime()),
                String.valueOf(registrationScheduleTemplate.getEndTime()),
                String.valueOf(remaining),
                String.valueOf(registrationScheduleTemplate.getTotalQuota()),
                String.valueOf(registrationScheduleTemplate.getConsultationRoomId()),
                String.valueOf(registrationScheduleTemplate.getEnabled()),
                String.valueOf(registrationScheduleTemplate.getPrice()),
                registrationScheduleTemplate.getRemark()
        );
    }

    /**
     * 根据医生id、科室id、开始日期、结束日期分页查询挂号排班模板
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param doctorId 医生id
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 科室id
     * @return 分页结果
     */
    public Result<PageInfo<AdminRegistrationScheduleTemplateDetail>> listRegistrationScheduleTemplatesByDoctorIdAndDepartmentIdAndDateAndPage(Integer pageNum, Integer pageSize, Long doctorId, String startDate, String endDate, Long departmentId) {
        // 日期解析防御：格式非法直接 fail，不抛 500
        LocalDate localStartDate;
        LocalDate localEndDate;
        try {
            localStartDate = startDate == null ? null : LocalDate.parse(startDate);
            localEndDate = endDate == null ? null : LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            return Result.fail("日期格式应为 yyyy-MM-dd");
        }

        List<Department> departmentList = departmentService.listAllDepartment();

        // 医生+科室组合：校验医生归属科室（含子科室），不匹配直接 fail，不静默丢弃条件
        if (doctorId != null && departmentId != null) {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            if (doctor == null || !expandDepartmentIds(List.of(departmentId), departmentList).contains(doctor.getDepartmentId())) {
                return Result.fail("医生与科室不匹配");
            }
        }

        // 统一收口医生 ID 集合：指定医生 / 科室（级联子科室）/ 全部
        List<Long> doctorIdList;
        if (doctorId != null) {
            doctorIdList = List.of(doctorId);
        } else if (departmentId != null) {
            Set<Long> departmentIds = expandDepartmentIds(List.of(departmentId), departmentList);
            List<Doctor> doctors = new ArrayList<>();
            for (Long deptId : departmentIds) {
                doctors.addAll(doctorService.listDoctorsByDepartmentId(deptId));
            }
            doctorIdList = doctors.stream().map(Doctor::getId).distinct().collect(Collectors.toList());
        } else {
            doctorIdList = doctorService.listAllDoctors().stream().map(Doctor::getId).toList();
        }

        // 空集合短路：避免 IN () 无效 SQL
        if (doctorIdList.isEmpty()) {
            return emptyPage(pageNum, pageSize);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService
                .listRegistrationScheduleTemplatesByDoctorIdListAndDate(doctorIdList, localStartDate, localEndDate);
        PageInfo<RegistrationScheduleTemplate> sourcePage = new PageInfo<>(registrationScheduleTemplateList);
        List<RegistrationSchedule> registrationScheduleList = registrationScheduleService
                .listRegistrationScheduleByRegistrationScheduleIdList(registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList());
        List<Doctor> doctorList = doctorService.listDoctorsByIds(doctorIdList);

        List<AdminRegistrationScheduleTemplateDetail> result = new ArrayList<>();
        for (RegistrationScheduleTemplate item : registrationScheduleTemplateList) {
            Doctor doctor = doctorList.stream().filter(d -> d.getId().equals(item.getDoctorId())).findFirst().orElse(null);
            if (doctor == null) continue;
            Department department = departmentList.stream().filter(d -> d.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
            List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(r -> r.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

            int remaining = 0;
            for (RegistrationSchedule r : registrationSchedules) {
                if (r.getRemainingQuota() != null) remaining += r.getRemainingQuota();
            }

            result.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
        }

        PageInfo<AdminRegistrationScheduleTemplateDetail> pageInfo = new PageInfo<>();
        pageInfo.setTotal(sourcePage.getTotal());
        pageInfo.setPageNum(sourcePage.getPageNum());
        pageInfo.setPageSize(sourcePage.getPageSize());
        pageInfo.setPages(sourcePage.getPages());
        pageInfo.setList(result);
        return Result.success(pageInfo);
    }

    /**
     * 级联展开科室集合：含自身及所有后代科室 id（BFS）
     * @param departmentIds 待展开的科室 id
     * @param departmentList 全量科室列表
     * @return 展开后的科室 id 集合
     */
    private Set<Long> expandDepartmentIds(List<Long> departmentIds, List<Department> departmentList) {
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (Department d : departmentList) {
            if (d.getParentDepartmentId() != null) {
                childrenMap.computeIfAbsent(d.getParentDepartmentId(), k -> new ArrayList<>()).add(d.getId());
            }
        }
        Set<Long> result = new HashSet<>(departmentIds);
        Deque<Long> queue = new ArrayDeque<>(departmentIds);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            for (Long child : childrenMap.getOrDefault(current, List.of())) {
                if (result.add(child)) {
                    queue.add(child);
                }
            }
        }
        return result;
    }

    /**
     * 构造空分页结果（total=0）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 空分页
     */
    private Result<PageInfo<AdminRegistrationScheduleTemplateDetail>> emptyPage(Integer pageNum, Integer pageSize) {
        PageInfo<AdminRegistrationScheduleTemplateDetail> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPages(0);
        pageInfo.setTotal(0);
        pageInfo.setList(List.of());
        return Result.success(pageInfo);
    }

    /**
     * 停诊
     * @param id 挂号排班模板id
     * @return 停诊结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> stopReceiving(Long id) {
        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService.getRegistrationScheduleTemplateById(id);
        if (registrationScheduleTemplate == null) {
            throw new BizException(BizErrorCode.SCHEDULE_TEMPLATE_NOT_FOUND);
        }
        registrationScheduleTemplate.setEnabled(false);
        Integer result = registrationScheduleTemplateService.updateRegistrationScheduleTemplateById(registrationScheduleTemplate);

        // 联动停诊已生成的排班
        syncScheduleStatusByTemplate(id, 0);
        // ponytail: S11 已知限制 — 停诊时未自动取消已挂号患者+退款，需通知系统支持，留待后续
        return Result.success(result);
    }

    /**
     * 开诊
     * @param id 挂号排班模板id
     * @return 开诊结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> startReceiving(Long id) {
        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService.getRegistrationScheduleTemplateById(id);
        if (registrationScheduleTemplate == null) {
            throw new BizException(BizErrorCode.SCHEDULE_TEMPLATE_NOT_FOUND);
        }
        registrationScheduleTemplate.setEnabled(true);
        Integer result = registrationScheduleTemplateService.updateRegistrationScheduleTemplateById(registrationScheduleTemplate);

        // 联动启用已生成的排班
        syncScheduleStatusByTemplate(id, 1);
        return Result.success(result);
    }

    /**
     * 同步某模板下所有排班的状态（停诊=0 / 启用=1）
     * ponytail: 条件更新避免覆盖已满(2)状态 — 开诊只改 0→1，停诊只改 1/2→0
     */
    private void syncScheduleStatusByTemplate(Long templateId, int status) {
        List<RegistrationSchedule> schedules = registrationScheduleService
                .getRegistrationScheduleListByTemplateIdList(List.of(templateId));
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        for (RegistrationSchedule schedule : schedules) {
            Integer cur = schedule.getStatus();
            if (cur == null) continue;
            boolean needUpdate = (status == 1 && cur == 0) || (status == 0 && (cur == 1 || cur == 2));
            if (needUpdate) {
                schedule.setStatus(status);
                registrationScheduleService.updateRegistrationScheduleById(schedule);
            }
        }
    }
}
