package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.UserPatientRelationManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UserPatientRelationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户患者关系控制器
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/v1/patient/relation")
@Tag(name = "就诊人管理", description = "就诊人管理接口")
public class UserPatientRelationControllerV1 {

    private final UserPatientRelationManager userPatientRelationManager;

    @Operation(summary = "添加就诊人", description = "添加就诊人")
    @PostMapping
    public Result<Integer> insertUserPatientRelation(@RequestAttribute("currentUserId") Long currentUserId,
                                                     @RequestBody UserPatientRelationRequest request) {
        return userPatientRelationManager.insertUserPatientRelation(currentUserId, request.getName(), request.getIdCard(),
                request.getPhone(), request.getRelation(), request.getRemark(), request.getDefaulted());
    }

    @Operation(summary = "修改就诊人", description = "修改就诊人，支持修改姓名、手机号、身份证号、关系、备注及默认状态")
    @PutMapping("/{id}")
    public Result<Integer> updateUserPatientRelation(@RequestAttribute("currentUserId") Long currentUserId,
                                                     @PathVariable Long id,
                                                     @RequestBody UserPatientRelationRequest request) {
        return userPatientRelationManager.updateUserPatientRelationById(currentUserId, id,
                request.getName(), request.getPhone(), request.getIdCard(),
                request.getRelation(), request.getRemark(), request.getDefaulted());
    }

    @Operation(summary = "设置默认就诊人", description = "设置默认就诊人")
    @PutMapping("/default/{id}")
    public Result<Integer> setDefaultUserPatientRelation(@RequestAttribute("currentUserId") Long currentUserId,
                                                         @PathVariable Long id) {
        return userPatientRelationManager.setDefaultUserPatientRelation(currentUserId, id);
    }

    @Operation(summary = "删除就诊人", description = "删除就诊人")
    @DeleteMapping("/{id}")
    public Result<Integer> deleteUserPatientRelation(@RequestAttribute("currentUserId") Long currentUserId,
                                                     @PathVariable Long id) {
        return userPatientRelationManager.deleteUserPatientRelationById(currentUserId, id);
    }
}
