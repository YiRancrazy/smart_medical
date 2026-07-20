package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.ExcelManager;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/admin/v1/excel")
@Tag(name = "管理员Excel接口")
public class AdminExcelControllerV1 {
    private final ExcelManager excelManager;

    @PostMapping("/upload/registration/template")
    @Operation(summary = "上传挂号模板")
    public Result<Integer> uploadRegistrationTemplate(@RequestParam("file") MultipartFile file) {
        return excelManager.uploadRegistrationTemplate(file);
    }

    @GetMapping("/download/registration/template")
    @Operation(summary = "下载挂号排班导入模板")
    public ResponseEntity<Resource> downloadRegistrationTemplate() {
        Resource resource = new ClassPathResource("ScheduleImportTemplate.csv");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"挂号排班导入模板.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(resource);
    }
}