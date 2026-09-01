package com.yirancrazy.smartmedical.controller.pharmacy;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.manager.PharmacyManager;
import com.yirancrazy.smartmedical.manager.PrescriptionManager;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.PrescriptionQueryRequest;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionPageItemVO;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.DispenseVO;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.PendingPrescriptionVO;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药师端 - 处方发药
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "药师端 - 处方")
@RestController
@RequestMapping("/api/pharmacy/v1/prescription")
@RequiredArgsConstructor
public class PharmacyPrescriptionControllerV1 {

    private final PharmacyManager pharmacyManager;
    private final PrescriptionManager prescriptionManager;

    /** 药师端 - 历史处方分页列表 */
    @Operation(summary = "药师端 - 历史处方分页列表")
    @PostMapping("/page")
    public Result<PageInfo<PrescriptionPageItemVO>> page(@Valid @RequestBody PrescriptionQueryRequest request) {
        return Result.success(prescriptionManager.pagePrescriptions(request, null));
    }

    /** 药师端 - 历史处方详情 */
    @Operation(summary = "药师端 - 历史处方详情")
    @GetMapping("/history/{id:\\d+}")
    public Result<PrescriptionDetailVO> historyDetail(@PathVariable Long id) {
        return Result.success(prescriptionManager.getPrescriptionDetailForAdmin(id));
    }

    /** 药师端 - 待发药列表（F31支持可选分页） */
    @Operation(summary = "药师端 - 待发药列表", description = "F31: 可选分页 pageNum/pageSize")
    @GetMapping("/pending")
    public Result<PageResult<PendingPrescriptionVO>> pending(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        return pharmacyManager.listPending(pageNum, pageSize);
    }

    /** 药师端 - 处方详情 */
    @Operation(summary = "药师端 - 处方详情")
    @GetMapping("/{id}")
    public Result<Prescription> detail(@PathVariable Long id) {
        return Result.success(pharmacyManager.getPrescriptionById(id));
    }

    /** 药师端 - 扫码发药 */
    @Operation(summary = "药师端 - 扫码发药")
    @PostMapping("/{id}/dispense")
    public Result<DispenseVO> dispense(@PathVariable Long id,
                                       @RequestAttribute("currentPharmacistId") Long pharmacistId) {
        return Result.success(pharmacyManager.dispense(id, pharmacistId));
    }
}
