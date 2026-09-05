package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.PatientManager;
import com.yirancrazy.smartmedical.manager.PrescriptionManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PrescriptionListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 处方查询
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@Tag(name = "用户端 - 处方", description = "我的处方查询")
@RestController
@RequestMapping("api/user/v1/prescription")
@RequiredArgsConstructor
public class UserPrescriptionControllerV1 {

    private final PrescriptionManager prescriptionManager;
    private final PatientManager patientManager;

    @Operation(summary = "用户端 - 我的处方列表")
    @GetMapping("/list")
    @Parameter(name = "patientCardId", description = "就诊卡ID", required = false)
    public Result<List<PrescriptionListVO>> list(@RequestAttribute("currentUserId") Long userId,
                                                 @RequestParam(required = false) Long patientCardId) {
        List<Long> patientUserIds = patientManager.getAccessiblePatientUserIds(userId, patientCardId);
        if (patientUserIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(prescriptionManager.listUserPrescriptions(patientUserIds));
    }

    @Operation(summary = "用户端 - 处方详情")
    @Parameter(name = "id", description = "处方ID", required = true)
    @GetMapping("/{id:\\d+}")
    public Result<PrescriptionDetailVO> detail(@PathVariable Long id,
                                               @RequestAttribute("currentUserId") Long userId) {
        return Result.success(prescriptionManager.getPrescriptionDetail(id, userId));
    }

    @Operation(summary = "用户端 - 退款已支付处方", description = "仅已支付未发药处方可退，退款后库存与款项回退")
    @Parameter(name = "id", description = "处方ID", required = true)
    @PostMapping("/{id:\\d+}/refund")
    public Result<String> refund(@PathVariable Long id,
                                 @RequestAttribute("currentUserId") Long userId) {
        prescriptionManager.refund(id, userId);
        return Result.success("退款成功");
    }
}