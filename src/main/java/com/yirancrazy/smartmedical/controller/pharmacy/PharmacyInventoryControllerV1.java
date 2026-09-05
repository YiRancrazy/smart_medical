package com.yirancrazy.smartmedical.controller.pharmacy;

import com.yirancrazy.smartmedical.manager.PharmacyManager;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药师端 - 库存管理
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "药师端 - 库存")
@RestController
@RequestMapping("/api/pharmacy/v1/inventory")
@RequiredArgsConstructor
public class PharmacyInventoryControllerV1 {

    private final PharmacyManager pharmacyManager;

    /** 药师端 - 库存预警列表（F31支持可选分页） */
    @Operation(summary = "药师端 - 库存预警列表", description = "F31: 可选分页 pageNum/pageSize")
    @GetMapping("/low-stock")
    public Result<PageResult<DrugInventory>> lowStock(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        return pharmacyManager.listLowStock(pageNum, pageSize);
    }

    /** 药师端 - 库存入库 */
    @Operation(summary = "药师端 - 库存入库")
    @PostMapping("/stock-in")
    public Result<DrugInventory> stockIn(@RequestParam Long drugId,
                                          @RequestParam Integer quantity,
                                          @RequestParam Long warehouseId,
                                          @RequestAttribute("currentPharmacistId") Long operatorId) {
        return Result.success(pharmacyManager.stockIn(drugId, quantity, warehouseId, operatorId));
    }

    /** 药师端 - 盘点调整 */
    @Operation(summary = "药师端 - 盘点调整")
    @PostMapping("/adjust")
    public Result<DrugInventory> adjust(@RequestParam Long drugId,
                                         @RequestParam Integer actualQuantity,
                                         @RequestParam Long warehouseId,
                                         @RequestParam(required = false) String remark,
                                         @RequestAttribute("currentPharmacistId") Long operatorId) {
        return Result.success(pharmacyManager.stockAdjust(drugId, actualQuantity, warehouseId, operatorId, remark));
    }
}
