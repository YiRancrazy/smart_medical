package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.RegistrationManager;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.InsertRegistrationRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AppointmentResponseSimple;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/registration")
@RequiredArgsConstructor
@Tag(name = "挂号管理", description = "挂号记录相关接口")
public class UserRegistrationControllerV1 {

    private final RegistrationManager registrationManager;

    @PostMapping("/add")
    @Operation(summary = "添加挂号记录", description = "添加新挂号记录")
    public Result<Integer> addRegistration(@RequestBody Registration registration) {
        return registrationManager.addRegistration(registration);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取挂号记录", description = "根据挂号记录ID获取挂号信息")
    @Parameter(name = "id", description = "挂号记录ID", required = true)
    public Result<Registration> getRegistrationById(@PathVariable String id) {
        return registrationManager.getRegistrationById(Long.parseLong(id));
    }

    /**
     * 挂号
     * @param request 挂号信息
     * @return 订单编号
     */
    @PostMapping("/")
    @Operation(summary = "添加挂号记录", description = "添加新挂号记录")
    public Result<String> insertRegistration(@RequestBody InsertRegistrationRequest request) {
        return registrationManager.addRegistration(Long.valueOf(request.getPaymentMethodId()),
                Long.valueOf(request.getRegistrationScheduleId()),
                Long.valueOf(request.getUserId()),
                Long.valueOf(request.getPatientCardId()));
    }

    /**
     * 根据用户ID获取挂号记录
     * @param patientId 用户ID
     * @return 挂号记录
     */
    @GetMapping("/simple/list/{patientId}")
    @Operation(summary = "根据患者ID获取挂号记录", description = "根据患者ID获取挂号记录")
    @Parameter(name = "patientId", description = "患者id", required = true)
    public Result<List<AppointmentResponseSimple>> getRegistrationByUid(@PathVariable String patientId) {
        return registrationManager.getRegistrationByUid(Long.valueOf(patientId));
    }



}
