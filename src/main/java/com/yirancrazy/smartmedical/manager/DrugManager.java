package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.service.DrugService;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 药品业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端药品搜索
 * @Datetime: 2026-07-22 15:30
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class DrugManager {

    private final DrugService drugService;

    /**
     * 医生端 - 按名称搜索药品
     * ponytail: 单表模糊查询
     * @param keyword 药品通用名/商品名关键词
     * @return 药品列表
     */
    public List<Drug> searchDrugs(String keyword) {
        return drugService.listDrugsByKeyword(keyword);
    }
}