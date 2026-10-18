package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.DrugManager;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 医生端 - 药品查询
 * @Author: YiRanCrazy@gmail.com
 * @Description: 提供病历开方时的药品搜索
 * @Datetime: 2026-07-22 10:30
 * @Version: 1.0
 */

@Tag(name = "医生端 - 药品")
@RestController
@RequestMapping("/api/doctor/v1/drug")
@RequiredArgsConstructor
public class DoctorDrugControllerV1 {

    private final DrugManager drugManager;

    /**
     * 医生端 - 按名称搜索药品
     * @param keyword 药品通用名/商品名关键词
     * @return 药品列表
     */
    @Operation(summary = "医生端 - 按名称搜索药品")
    @GetMapping("/search")
    public Result<List<Drug>> search(@RequestParam(required = false) String keyword) {
        return Result.success(drugManager.searchDrugs(keyword));
    }
}
