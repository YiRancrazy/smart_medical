package com.yirancrazy.smartmedical.pojo.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 新增班次请求
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAddRequest {

    private String name;

    private LocalTime startTime;

    private LocalTime endTime;

    private Double workHours;

    private String description;
}
