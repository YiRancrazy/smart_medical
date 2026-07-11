package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.RegistrationCheckInManager;
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
 * @Description: 用户挂号管理 + 报到/取消
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/registration")
@RequiredArgsConstructor
@Tag(name = "挂号管理", description = "挂号记录相关接口")
public class UserRegistrationControllerV1 {

    private final RegistrationManager registrationManager;
    private final RegistrationCheckInManager registrationCheckInManager;

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

    /**
     * 用户端 - 挂号报到
     * @param id 挂号记录ID
     * @param userId 当前用户ID(Task 12 JWT 落地后可替换为 currentUserId)
     */
    @PostMapping("/{id}/check-in")
    @Operation(summary = "用户端 - 挂号报到")
    @Parameter(name = "id", description = "挂号记录ID", required = true)
    public Result<Void> checkIn(@PathVariable Long id, @RequestParam Long userId) {
        registrationCheckInManager.checkIn(id, userId);
        return Result.success(null);
    }

    /**
     * 用户端 - 取消预约(含退款联动)
     * @param id 挂号记录ID
     * @param userId 当前用户ID
     * @param reason 取消原因(可选)
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "用户端 - 取消预约")
    @Parameter(name = "id", description = "挂号记录ID", required = true)
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam Long userId,
                               @RequestParam(required = false) String reason) {
        registrationCheckInManager.cancel(id, userId, reason);
        return Result.success(null);
    }
}
