package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.PrescriptionQueryManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.PrescriptionQueryRequest;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionPageItemVO;
import com.yirancrazy.smartmedical.pojo.dto.common.PageResult;
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
 * 管理员端 - 处方历史
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员查看历史处方记录
 * @Datetime: 2026-07-25 20:20
 * @Version: 1.0
 */

@RestController
@RequestMapping("/api/admin/v1/prescription")
@Tag(name = "管理员端 - 处方历史")
@RequiredArgsConstructor
public class AdminPrescriptionControllerV1 {

    private final PrescriptionQueryManager prescriptionQueryManager;

    /**
     * 管理员端 - 处方历史分页列表
     * @param request 查询条件
     * @return 处方分页列表
     */
    @Operation(summary = "管理员端 - 处方历史分页列表")
    @PostMapping("/page")
    public Result<PageResult<PrescriptionPageItemVO>> page(@Valid @RequestBody PrescriptionQueryRequest request) {
        return Result.success(prescriptionQueryManager.pagePrescriptions(request, null));
    }

    /**
     * 管理员端 - 处方详情
     * @param id 处方ID
     * @return 处方详情
     */
    @Operation(summary = "管理员端 - 处方详情")
    @Parameter(name = "id", description = "处方ID", required = true)
    @GetMapping("/{id:\\d+}")
    public Result<PrescriptionDetailVO> detail(@PathVariable Long id) {
        return Result.success(prescriptionQueryManager.getPrescriptionDetailForAdmin(id));
    }
}
