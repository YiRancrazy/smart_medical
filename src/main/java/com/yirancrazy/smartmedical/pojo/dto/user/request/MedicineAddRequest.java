package com.yirancrazy.smartmedical.pojo.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 新增药品请求
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineAddRequest {

    private String approvalNumber;

    private String medicineName;

    private String dosageForm;

    private String medicineSpecifications;

    private String listingPermitHolder;

    private String productionUnit;

    private String medicineCode;

    private String remarks;

    private Integer quantity;

    private BigDecimal price;
}
