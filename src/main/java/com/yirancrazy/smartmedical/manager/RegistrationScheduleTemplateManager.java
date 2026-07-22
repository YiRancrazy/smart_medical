package com.yirancrazy.smartmedical.manager;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminRegistrationScheduleTemplateDetail;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
                log.warn("跳过排班模板 {}：未找到医生 {}", item.getId(), item.getDoctorId());
                continue;
            }
            Department department = departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
            List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

            Integer remaining = 0;
            for (RegistrationSchedule r : registrationSchedules) {
                remaining += r.getRemainingQuota();
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
        LocalDate localStartDate = null;
        LocalDate localEndDate = null;

        if (startDate != null)
            localStartDate = LocalDate.parse(startDate);
        if (endDate != null)
            localEndDate = LocalDate.parse(endDate);

        List<Department> departmentList = departmentService.listAllDepartment();
        List<Doctor> doctorList = null;
        List<RegistrationSchedule> registrationScheduleList;
        List<AdminRegistrationScheduleTemplateDetail> result = new ArrayList<>();
        PageInfo<RegistrationScheduleTemplate> sourcePage = null;

        if (departmentId != null && doctorId == null) {
            List<Doctor> doctors = doctorService.listDoctorsByDepartmentId(departmentId);
            List<Long> doctorIdList = doctors.stream().map(Doctor::getId).toList();
            PageHelper.startPage(pageNum, pageSize);
            List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService
                    .listRegistrationScheduleTemplatesByDoctorIdListAndDate(doctorIdList, localStartDate, localEndDate);
            sourcePage = new PageInfo<>(registrationScheduleTemplateList);
            registrationScheduleList = registrationScheduleService
                    .listRegistrationScheduleByRegistrationScheduleIdList(registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList());
            doctorList = doctorIdList.isEmpty() ? List.of() : doctorService.listDoctorsByIds(doctorIdList);

            for (RegistrationScheduleTemplate item : registrationScheduleTemplateList) {
                Doctor doctor = doctorList.stream().filter(item1 -> item1.getId().equals(item.getDoctorId())).findFirst().orElse(null);
                if (doctor == null) continue;
                Department department = departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
                List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

            Integer remaining = 0;
            for (RegistrationSchedule r : registrationSchedules) {
                remaining += r.getRemainingQuota();
            }

            result.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
        }
    } else if (doctorId != null) {
            PageHelper.startPage(pageNum, pageSize);
            List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService
                    .listRegistrationScheduleTemplatesByDoctorIdAndDate(doctorId, localStartDate, localEndDate);
            sourcePage = new PageInfo<>(registrationScheduleTemplateList);
            registrationScheduleList = registrationScheduleService
                    .listRegistrationScheduleByRegistrationScheduleIdList(registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList());
            Doctor doctor1 = doctorService.getDoctorById(doctorId);
            doctorList = doctor1 == null ? List.of() : List.of(doctor1);

            for (RegistrationScheduleTemplate item : registrationScheduleTemplateList) {
                Doctor doctor = doctorList.stream().filter(item1 -> item1.getId().equals(item.getDoctorId())).findFirst().orElse(null);
                if (doctor == null) continue;
                Department department = departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
                List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

                Integer remaining = 0;
                for (RegistrationSchedule r : registrationSchedules) {
                    remaining += r.getRemainingQuota();
                }

                result.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
            }
        } else {
            List<Doctor> doctors = doctorService.listAllDoctors();
            List<Long> doctorIdList = doctors.stream().map(Doctor::getId).toList();
            PageHelper.startPage(pageNum, pageSize);
            List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService
                    .listRegistrationScheduleTemplatesByDoctorIdListAndDate(doctorIdList, localStartDate, localEndDate);
            sourcePage = new PageInfo<>(registrationScheduleTemplateList);
            registrationScheduleList = registrationScheduleService
                    .listRegistrationScheduleByRegistrationScheduleIdList(registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList());
            doctorList = doctorIdList.isEmpty() ? List.of() : doctorService.listDoctorsByIds(doctorIdList);

            for (RegistrationScheduleTemplate item : registrationScheduleTemplateList) {
                Doctor doctor = doctorList.stream().filter(item1 -> item1.getId().equals(item.getDoctorId())).findFirst().orElse(null);
                if (doctor == null) continue;
                Department department = departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
                List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

                Integer remaining = 0;
                for (RegistrationSchedule r : registrationSchedules) {
                    remaining += r.getRemainingQuota();
                }

                result.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
            }
        }

        PageInfo<AdminRegistrationScheduleTemplateDetail> pageInfo = new PageInfo<>();
        if (sourcePage != null) {
            pageInfo.setTotal(sourcePage.getTotal());
            pageInfo.setPageNum(sourcePage.getPageNum());
            pageInfo.setPageSize(sourcePage.getPageSize());
            pageInfo.setPages(sourcePage.getPages());
        }
        pageInfo.setList(result);
        return Result.success(pageInfo);
    }

    /**
     * 停诊
     * @param id 挂号排班模板id
     * @return 停诊结果
     */
    public Result<Integer> stopReceiving(Long id) {
        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService.getRegistrationScheduleTemplateById(id);
        if (registrationScheduleTemplate == null) {
            throw new BizException(BizErrorCode.SCHEDULE_TEMPLATE_NOT_FOUND);
        }
        registrationScheduleTemplate.setEnabled(false);
        Integer result = registrationScheduleTemplateService.updateRegistrationScheduleTemplateById(registrationScheduleTemplate);

        // 联动停诊已生成的排班
        syncScheduleStatusByTemplate(id, 0);
        return Result.success(result);
    }

    /**
     * 开诊
     * @param id 挂号排班模板id
     * @return 开诊结果
     */
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
     */
    private void syncScheduleStatusByTemplate(Long templateId, int status) {
        List<RegistrationSchedule> schedules = registrationScheduleService
                .getRegistrationScheduleListByTemplateIdList(List.of(templateId));
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        for (RegistrationSchedule schedule : schedules) {
            if (schedule.getStatus() == null || schedule.getStatus() != status) {
                schedule.setStatus(status);
                registrationScheduleService.updateRegistrationScheduleById(schedule);
            }
        }
    }
}
