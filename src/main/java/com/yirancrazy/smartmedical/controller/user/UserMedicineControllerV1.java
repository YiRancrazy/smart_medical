package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.MedicineManager;
import com.yirancrazy.smartmedical.pojo.Medicine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/medicine")
@RequiredArgsConstructor
@Tag(name = "药品管理", description = "药品相关接口")
public class UserMedicineControllerV1 {

    private final MedicineManager medicineManager;

    @PostMapping("/add")
    @Operation(summary = "添加药品", description = "添加新药品")
    public int addMedicine(@RequestBody Medicine medicine) {
        return medicineManager.addMedicine(medicine);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取药品", description = "根据药品ID获取药品信息")
    @Parameter(name = "id", description = "药品ID", required = true)
    public Medicine getMedicineById(@PathVariable String id) {
        return medicineManager.getMedicineById(Long.parseLong(id));
    }
}
