package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.ExcelManager;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-18 17:59
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/excel")
@Tag(name = "管理员Excel接口")
public class AdminExcelControllerV1 {
    private final ExcelManager excelManager;

    @PostMapping("/upload/registration/template")
    @Operation(summary = "上传挂号模板")
    public Result<Integer> uploadRegistrationTemplate(@RequestParam("file") MultipartFile file) {
        return excelManager.uploadRegistrationTemplate(file);
    }
}