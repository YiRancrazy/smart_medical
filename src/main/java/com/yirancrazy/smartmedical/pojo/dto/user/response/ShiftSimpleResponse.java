package com.yirancrazy.smartmedical.pojo.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-06 12:11
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSimpleResponse {
    private String id; // ID
    private String name; // 名称
    private LocalTime start; // 开始时间
    private LocalTime end;   // 结束时间
}