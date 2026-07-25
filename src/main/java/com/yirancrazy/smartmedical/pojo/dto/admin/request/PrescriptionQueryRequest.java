package com.yirancrazy.smartmedical.pojo.dto.admin.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 管理端 - 处方历史查询请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支持按患者姓名、处方状态、创建日期范围分页查询处方
 * @Datetime: 2026-07-25 20:10
 * @Version: 1.0
 */

@Data
public class PrescriptionQueryRequest {

    /** 患者姓名（模糊） */
    private String patientName;

    /** 处方状态：0-待支付 1-已支付 2-已发药 3-已取消 */
    private Integer status;

    /** 创建日期-开始 */
    private LocalDate startDate;

    /** 创建日期-结束 */
    private LocalDate endDate;

    /** 页码，默认 1 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 */
    private Integer pageSize = 10;
}
