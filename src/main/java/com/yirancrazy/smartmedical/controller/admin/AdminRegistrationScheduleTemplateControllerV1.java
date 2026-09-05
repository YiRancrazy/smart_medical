package com.yirancrazy.smartmedical.controller.admin;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.manager.RegistrationScheduleManager;
import com.yirancrazy.smartmedical.manager.RegistrationScheduleTemplateManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminRegistrationScheduleTemplateDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员挂号排班模板控制器
 * @Datetime: 2026-03-20 18:47
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "管理员-挂号排班模板管理")
@RequestMapping("/api/admin/v1/registration/schedule/template")
public class AdminRegistrationScheduleTemplateControllerV1 {
    private final RegistrationScheduleManager registrationScheduleManager;
    private final RegistrationScheduleTemplateManager registrationScheduleTemplateManager;

    @Operation(summary = "管理员端 - 获取所有挂号排班模板列表")
    @GetMapping("/list/page/{pageNum}/{pageSize}")
    public Result<PageInfo<AdminRegistrationScheduleTemplateDetail>> listRegistrationScheduleTemplatesByPage(@PathVariable Integer pageNum, @PathVariable Integer pageSize) {
        return registrationScheduleTemplateManager.listRegistrationScheduleTemplatesByPage(pageNum, pageSize);
    }

    @Operation(summary = "管理员端 - 查询挂号排班模板（分页）")
    @GetMapping("/list/page")
    public Result<PageInfo<AdminRegistrationScheduleTemplateDetail>> listRegistrationScheduleTemplatesByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false, defaultValue = "1000-01-01") String startDate,
            @RequestParam(required = false, defaultValue = "9999-12-12") String endDate,
            @RequestParam(required = false) Long departmentId) {
        return registrationScheduleTemplateManager
                .listRegistrationScheduleTemplatesByDoctorIdAndDepartmentIdAndDateAndPage(
                        pageNum,
                        pageSize,
                        doctorId,
                        startDate,
                        endDate,
                        departmentId);
    }

    @Operation(summary = "管理员端 - 停诊")
    @PutMapping("/stop/{id}")
    public Result<Integer> stopRegistrationScheduleTemplateById(@PathVariable Long id) {
        return registrationScheduleTemplateManager.stopReceiving(id);
    }

    @Operation(summary = "管理员端 - 启用")
    @PutMapping("/start/{id}")
    public Result<Integer> startRegistrationScheduleTemplateById(@PathVariable Long id) {
        return registrationScheduleTemplateManager.startReceiving(id);
    }

    @Operation(summary = "管理员端 - 删除挂号模板")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> deleteRegistrationScheduleTemplateById(@PathVariable Long id) {
        return registrationScheduleTemplateManager.deleteRegistrationScheduleTemplateById(id);
    }
}
