package com.yirancrazy.smartmedical.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-18 17:42
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelRegistrationTemplate {
    @ExcelProperty("医生id")
    private String doctorId;
    @ExcelProperty("医生姓名")
    private String doctorName;             // 医生名称
    @ExcelProperty("科室")
    private String departmentName;         // 科室名称
    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("排班日期")
    private String registrationDate;    // 挂号日期
    @ExcelProperty("班次类型")
    private String registrationType;       // 挂号类型
    @DateTimeFormat("HH:mm:ss")
    @ExcelProperty("开始时间")
    private String startTime;           // 开始时间
    @DateTimeFormat("HH:mm:ss")
    @ExcelProperty("结束时间")
    private String endTime;             // 结束时间
    @ExcelProperty("号源数量")
    private Integer total;                 // 总数
    @ExcelProperty("挂号价格")
    private String price;                 // 价格
    @ExcelProperty("诊室编号")
    private String sn;                // 地址
    @ExcelProperty("备注")
    private String remark;                 // 备注
}