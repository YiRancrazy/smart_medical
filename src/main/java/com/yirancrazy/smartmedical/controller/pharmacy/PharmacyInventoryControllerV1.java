package com.yirancrazy.smartmedical.controller.pharmacy;

import com.yirancrazy.smartmedical.manager.PharmacyManager;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药师端 - 库存预警
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "药师端 - 库存")
@RestController
@RequestMapping("/pharmacy/v1/inventory")
@RequiredArgsConstructor
public class PharmacyInventoryControllerV1 {

    private final PharmacyManager pharmacyManager;

    /** 药师端 - 库存预警列表 */
    @Operation(summary = "药师端 - 库存预警列表")
    @GetMapping("/low-stock")
    public Result<List<DrugInventory>> lowStock() {
        return Result.success(pharmacyManager.listLowStock());
    }
}
