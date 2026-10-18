package com.yirancrazy.smartmedical.pojo.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 新增订单明细请求
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemAddRequest {

    private Long orderId;

    private Long productionId;

    private Long productionTypeId;

    private Integer quantity;

    private String productionName;
}
