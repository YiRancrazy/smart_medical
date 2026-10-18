package com.yirancrazy.smartmedical.pojo.dto.admin.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

/**
 * 管理端 - 病历历史查询请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支持按患者姓名、创建日期范围分页查询病历
 * @Datetime: 2026-07-25 20:10
 * @Version: 1.0
 */

@Data
public class MedicalRecordQueryRequest {

    /** 患者姓名（模糊） */
    private String patientName;

    /** 创建日期-开始 */
    private LocalDate startDate;

    /** 创建日期-结束 */
    private LocalDate endDate;

    /** 页码，默认 1 */
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    /** 每页条数，默认 10 */
    @Min(value = 1, message = "每页条数不能小于 1")
    private Integer pageSize = 10;
}
