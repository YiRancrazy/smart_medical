package com.yirancrazy.smartmedical.pojo.vo.registration.confirm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号确认页面就诊人信息返回对象
 * @Datetime: 2026-02-20 11:25
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDateAndRemainQuotaVo {
    private String doctorId;                          // 医生id
    private LocalDate date;                       // 挂号日期
    private Integer remainQuota;                      // 剩余配额
    private Integer totalQuota;                       // 总配额
}