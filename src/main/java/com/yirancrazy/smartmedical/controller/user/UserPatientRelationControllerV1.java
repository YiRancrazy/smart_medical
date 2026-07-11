package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.UserPatientRelationManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.OutPatientCardBaseInfo;
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

    // todo：添加就诊人
    @Operation(summary = "添加就诊人", description = "添加就诊人")
    @PostMapping("/{currentUserId}/{name}/{idCard}/{phone}/{relation}/{remark}/{defaulted}")
    public Result<Integer> insertUserPatientRelation(@PathVariable String currentUserId,
                                                     @PathVariable String name,
                                                     @PathVariable String idCard,
                                                     @PathVariable String phone,
                                                     @PathVariable String relation,
                                                     @PathVariable String remark,
                                                     @PathVariable String defaulted) {
        return userPatientRelationManager.insertUserPatientRelation(currentUserId,name,idCard,phone,relation,remark,defaulted);
    }

    // todo：修改就诊人

    @Operation(summary = "修改就诊人", description = "修改就诊人")
    @PutMapping("/{currentUserId}/{id}/{relation}/{remark}/{defaulted}")
    public Result<Integer> updateUserPatientRelation(@PathVariable String currentUserId,
                                                     @PathVariable Long id,
                                                     @PathVariable String relation,
                                                     @PathVariable String remark,
                                                     @PathVariable String defaulted) {
        return userPatientRelationManager.updateUserPatientRelationById(currentUserId,id,relation,remark,defaulted);
    }
    // todo：查询就诊人


    // todo：设置默认就诊人
    @Operation(summary = "设置默认就诊人", description = "设置默认就诊人")
    @PutMapping("/default/{currentUserId}/{id}")
    public Result<Integer> setDefaultUserPatientRelation(@PathVariable String currentUserId,@PathVariable Long id) {
        return userPatientRelationManager.setDefaultUserPatientRelation(currentUserId,id);
    }
    // todo：删除就诊人
    @Operation(summary = "删除就诊人", description = "删除就诊人")
    @DeleteMapping("/{id}")
    public Result<Integer> deleteUserPatientRelation(@PathVariable Long id) {
        return userPatientRelationManager.deleteUserPatientRelationById(id);
    }
}
