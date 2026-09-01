package com.yirancrazy.smartmedical.controller.admin;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.manager.MedicalRecordManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.MedicalRecordQueryRequest;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordPageItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 病历历史
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员查看历史病历记录
 * @Datetime: 2026-07-25 20:20
 * @Version: 1.0
 */

@RestController
@RequestMapping("/api/admin/v1/medical-record")
@Tag(name = "管理员端 - 病历历史")
@RequiredArgsConstructor
public class AdminMedicalRecordControllerV1 {

    private final MedicalRecordManager medicalRecordManager;

    /**
     * 管理员端 - 病历历史分页列表
     * @param request 查询条件
     * @return 病历分页列表
     */
    @Operation(summary = "管理员端 - 病历历史分页列表")
    @PostMapping("/page")
    public Result<PageInfo<MedicalRecordPageItemVO>> page(@Valid @RequestBody MedicalRecordQueryRequest request) {
        return Result.success(medicalRecordManager.pageMedicalRecords(request, null));
    }

    /**
     * 管理员端 - 病历详情
     * @param id 病历ID
     * @return 病历详情
     */
    @Operation(summary = "管理员端 - 病历详情")
    @Parameter(name = "id", description = "病历ID", required = true)
    @GetMapping("/{id:\\d+}")
    public Result<MedicalRecordDetailVO> detail(@PathVariable Long id) {
        return Result.success(medicalRecordManager.getMedicalRecordDetailForAdmin(id));
    }
}
