package com.yirancrazy.smartmedical.pojo.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 统计数据返回值
 * @Datetime: 2026-02-03 11:50
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticDataVo {
    private int totalReservationCount;          // 预约总数
    private int totalPatientCount;              // 患者总数
    private int totalDoctorCount;               // 医生人数  
    private BigDecimal todayIncome;             // 今日收入
    
}
