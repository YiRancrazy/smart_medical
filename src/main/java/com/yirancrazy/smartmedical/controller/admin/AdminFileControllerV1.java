package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.FileManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.simple.AdminFileSimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员端 - 文件上传下载接口
 * @Datetime: 2026-03-18 16:50
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/file")
@Tag(name = "文件管理接口")
public class AdminFileControllerV1 {
    private final FileManager fileManager;

    /**
     * 获取挂号排班模板
     * @return 文件对象
     */
    @GetMapping("/registration/template")
    @Operation(summary = "获取挂号排班模板")
    public Result<AdminFileSimpleResponse> getRegistrationTemplate() {
        return fileManager.getRegistrationTemplate();
    }
}