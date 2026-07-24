package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.PatientCardManager;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PatientCardSimpleResponse;
import com.yirancrazy.smartmedical.pojo.vo.OutPatientCardBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationConfirmPatientCardVo;
import com.yirancrazy.smartmedical.service.PatientCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-14 10:47
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "患者卡管理", description = "患者卡管理")
@RequestMapping("api/user/v1/patient/card")
public class UserPatientCardControllerV1 {
    private final PatientCardManager patientCardManager;

    /**
     * 获取默认就诊人基本信息
     * @param userId 账号id
     * @return 默认就诊人基本信息
     */
    @GetMapping("/baseinfo")
    @Operation(summary = "获取默认就诊人基本信息", description = "获取默认就诊人基本信息接口")
    public Result<OutPatientCardBaseInfo> getDefaultPatientBaseInfoByAccountId(@RequestAttribute("currentUserId") Long userId) {
        return patientCardManager.getDefaultPatientBaseInfoByUid(userId);
    }

    /**
     * 获取所有就诊人基本信息
     *
     *  @param userId 账号id
     * @return 所有就诊人基本信息
     */
    @GetMapping("/confirm/baseinfo")
    @Operation(summary = "获取所有就诊人基本信息", description = "获取所有就诊人基本信息接口")
    public Result<List<RegistrationConfirmPatientCardVo>> getAllPatientBaseInfoByUid(@RequestAttribute("currentUserId") Long userId){
        return patientCardManager.getAllPatientBaseInfoByUserId(userId);
    }

    /**
     * 获取所有患者卡信息简单响应
     * @param userId 账号id
     * @return 患者卡信息简单响应
     */
    @GetMapping("/list/simple/response")
    @Operation(summary = "获取所有患者卡信息简单响应", description = "获取所有患者卡信息简单响应接口")
    public Result<List<PatientCardSimpleResponse>> listPatientCardSimpleResponseByUserId(@RequestAttribute("currentUserId") Long userId){
        return patientCardManager.listPatientCardSimpleResponseByUserId(userId);
    }

    /**
     * 按关系ID查询单条就诊人详情（含 remark）
     * @param userId 账号id
     * @param relationId 用户患者关系ID
     * @return 单条就诊人详情
     */
    @GetMapping("/detail/{relationId}")
    @Operation(summary = "用户端 - 按关系ID查询单条就诊人详情", description = "F24: 替代拉全列表 find by id，含 remark 字段")
    public Result<PatientCardSimpleResponse> getPatientCardDetailByRelationId(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable Long relationId) {
        return patientCardManager.getPatientCardDetailByRelationId(userId, relationId);
    }
}
