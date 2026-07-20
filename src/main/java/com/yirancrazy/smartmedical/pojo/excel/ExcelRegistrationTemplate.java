package com.yirancrazy.smartmedical.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 挂号排班导入模板
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班 Excel 导入行对象，按列索引读取
 * @Datetime: 2026-03-18 17:42
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelRegistrationTemplate {
    @ExcelProperty(index = 0)
    private String doctorId;

    @ExcelProperty(index = 1)
    private String doctorName;

    @ExcelProperty(index = 2)
    private String departmentName;

    @ExcelProperty(index = 3)
    private String registrationDate;

    @ExcelProperty(index = 4)
    private String registrationType;

    @ExcelProperty(index = 5)
    private String startTime;

    @ExcelProperty(index = 6)
    private String endTime;

    @ExcelProperty(index = 7)
    private Integer total;

    @ExcelProperty(index = 8)
    private String price;

    @ExcelProperty(index = 9)
    private String sn;

    @ExcelProperty(index = 10)
    private String remark;
}
