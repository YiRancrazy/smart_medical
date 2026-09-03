package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Medicine;
import com.yirancrazy.smartmedical.service.MedicineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class MedicineManager {

    private final MedicineService medicineService;

    public int addMedicine(Medicine medicine) {
        Long id = IdUtil.getSnowflakeNextId();
        medicine.setMedicineId(id);
        return medicineService.insertMedicine(medicine);
    }

    public Medicine getMedicineById(Long id) {
        return medicineService.getMedicineById(id);
    }
}
