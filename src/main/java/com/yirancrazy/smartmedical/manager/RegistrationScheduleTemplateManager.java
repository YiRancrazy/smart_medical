package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminRegistrationScheduleTemplateDetail;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班模板管理器
 * @Datetime: 2026-03-20 18:52
 * @Version: 1.0
 */

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

        List<Long> doctorIdList = registrationScheduleTemplates
                .stream()
                .map(RegistrationScheduleTemplate::getDoctorId)
                .toList();

        List<Long> registrationScheduleTemplateIdList = registrationScheduleTemplates
                .stream()
                .map(RegistrationScheduleTemplate::getId)
                .toList();

        List<Doctor> doctorList = doctorService.listDoctorsByIds(doctorIdList);
        List<Department> departmentList = departmentService.listAllDepartment();
        List<RegistrationSchedule> registrationScheduleList = registrationScheduleService
                .listRegistrationScheduleByRegistrationScheduleTemplateIdList(registrationScheduleTemplateIdList);

        for (RegistrationScheduleTemplate item : registrationScheduleTemplates) {
            Doctor doctor = doctorList.stream().filter(item1 -> item1.getId().equals(item.getDoctorId())).findFirst().orElse(null);
            Department department = departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
            List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

            Integer remaining = 0;
            for (RegistrationSchedule r : registrationSchedules) {
                remaining += r.getRemainingQuota();
            }

            registrationScheduleTemplateDetails.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
        }

        PageInfo<AdminRegistrationScheduleTemplateDetail> pageInfo = new PageInfo<>(registrationScheduleTemplateDetails);

        return Result.success(pageInfo);
    }

    public AdminRegistrationScheduleTemplateDetail createAdminRegistrationScheduleTemplateDetail(RegistrationScheduleTemplate registrationScheduleTemplate, Doctor doctor, Department department, Integer remaining) {
        return new AdminRegistrationScheduleTemplateDetail(
                String.valueOf(registrationScheduleTemplate.getId()),
                String.valueOf(doctor.getId()),
                doctor.getName(),
                String.valueOf(department.getId()),
                department.getName(),
                String.valueOf(registrationScheduleTemplate.getRegistrationDate()),
                String.valueOf(registrationScheduleTemplate.getRegistrationType()),
                String.valueOf(registrationScheduleTemplate.getStartTime()),
                String.valueOf(registrationScheduleTemplate.getEndTime()),
                String.valueOf(remaining),
                String.valueOf(registrationScheduleTemplate.getTotalQuota()),
                registrationScheduleTemplate.getConsultationRoomId(),
                String.valueOf(registrationScheduleTemplate.getEnabled()),
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

        if (departmentId != null && doctorId == null) {
            List<Doctor> doctors = doctorService.listDoctorsByDepartmentId(departmentId);
            List<Long> doctorIdList = doctors.stream().map(Doctor::getId).toList();
            PageHelper.startPage(pageNum, pageSize);
            List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService
                    .listRegistrationScheduleTemplatesByDoctorIdListAndDate(doctorIdList, localStartDate, localEndDate);
            registrationScheduleList = registrationScheduleService
                    .listRegistrationScheduleByRegistrationScheduleTemplateIdList(registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList());
            doctorList = doctorService.listDoctorsByIds(doctorIdList);

            for (RegistrationScheduleTemplate item : registrationScheduleTemplateList) {
                Doctor doctor = doctorList.stream().filter(item1 -> item1.getId().equals(item.getDoctorId())).findFirst().orElse(null);
                Department department = departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
                List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

                Integer remaining = 0;
                for (RegistrationSchedule r : registrationSchedules) {
                    remaining += r.getRemainingQuota();
                }

                assert doctor != null;
                assert department != null;
                result.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
            }
        } else if (doctorId != null) {
            PageHelper.startPage(pageNum, pageSize);
            List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService
                    .listRegistrationScheduleTemplatesByDoctorIdAndDate(doctorId, localStartDate, localEndDate);
            registrationScheduleList = registrationScheduleService
                    .listRegistrationScheduleByRegistrationScheduleTemplateIdList(registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList());
            Doctor doctor1 = doctorService.getDoctorById(doctorId);
            doctorList = new ArrayList<>();
            doctorList.add(doctor1);

            for (RegistrationScheduleTemplate item : registrationScheduleTemplateList) {
                Doctor doctor = doctorList.stream().filter(item1 -> item1.getId().equals(item.getDoctorId())).findFirst().orElse(null);
                Department department = departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
                List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

                Integer remaining = 0;
                for (RegistrationSchedule r : registrationSchedules) {
                    remaining += r.getRemainingQuota();
                }

                assert doctor != null;
                assert department != null;
                result.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
            }
        } else {
            List<Doctor> doctors = doctorService.listAllDoctors();
            List<Long> doctorIdList = doctors.stream().map(Doctor::getId).toList();
            PageHelper.startPage(pageNum, pageSize);
            List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService
                    .listRegistrationScheduleTemplatesByDoctorIdListAndDate(doctorIdList, localStartDate, localEndDate);
            registrationScheduleList = registrationScheduleService
                    .listRegistrationScheduleByRegistrationScheduleTemplateIdList(registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList());
            doctorList = doctorService.listDoctorsByIds(doctorIdList);

            for (RegistrationScheduleTemplate item : registrationScheduleTemplateList) {
                Doctor doctor = doctorList.stream().filter(item1 -> item1.getId().equals(item.getDoctorId())).findFirst().orElse(null);
                Department department = departmentList.stream().filter(item1 -> item1.getId().equals(doctor.getDepartmentId())).findFirst().orElse(null);
                List<RegistrationSchedule> registrationSchedules = registrationScheduleList.stream().filter(item1 -> item1.getRegistrationScheduleTemplateId().equals(item.getId())).toList();

                Integer remaining = 0;
                for (RegistrationSchedule r : registrationSchedules) {
                    remaining += r.getRemainingQuota();
                }

                assert doctor != null;
                assert department != null;
                result.add(createAdminRegistrationScheduleTemplateDetail(item, doctor, department, remaining));
            }
        }

        PageInfo<AdminRegistrationScheduleTemplateDetail> pageInfo = new PageInfo<>(result);
        return Result.success(pageInfo);
    }

    /**
     * 停诊
     * @param id 挂号排班模板id
     * @return 停诊结果
     */
    public Result<Integer> stopReceiving(Long id) {
        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService.getRegistrationScheduleTemplateById(id);
        registrationScheduleTemplate.setEnabled(false);

        Integer result = registrationScheduleTemplateService.updateRegistrationScheduleTemplateById(registrationScheduleTemplate);
        return Result.success(result);
    }

    /**
     * 开诊
     * @param id 挂号排班模板id
     * @return 开诊结果
     */
    public Result<Integer> startReceiving(Long id) {
        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService.getRegistrationScheduleTemplateById(id);
        registrationScheduleTemplate.setEnabled(true);

        Integer result = registrationScheduleTemplateService.updateRegistrationScheduleTemplateById(registrationScheduleTemplate);
        return Result.success(result);
    }
}
