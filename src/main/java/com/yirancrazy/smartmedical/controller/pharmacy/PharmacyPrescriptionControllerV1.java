package com.yirancrazy.smartmedical.controller.pharmacy;

import com.yirancrazy.smartmedical.manager.PharmacyManager;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.DispenseVO;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.PendingPrescriptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /** 药师端 - 待发药列表 */
    @Operation(summary = "药师端 - 待发药列表")
    @GetMapping("/pending")
    public Result<List<PendingPrescriptionVO>> pending() {
        return Result.success(pharmacyManager.listPending());
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
