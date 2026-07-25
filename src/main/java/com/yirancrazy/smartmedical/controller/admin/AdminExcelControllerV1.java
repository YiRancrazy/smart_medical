package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.ExcelManager;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
    public void downloadRegistrationTemplate(HttpServletResponse response) throws IOException {
        String filename = "挂号排班导入模板.csv";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

        // 写入 UTF-8 BOM，使 Excel 能正确识别中文
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (InputStream is = new ClassPathResource("ScheduleImportTemplate.csv").getInputStream()) {
            is.transferTo(response.getOutputStream());
        }
        response.getOutputStream().flush();
    }
}