package com.yirancrazy.smartmedical.pojo.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 新增订单状态日志请求
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLogAddRequest {

    private Long orderId;

    private Integer fromStatus;

    private Integer toStatus;

    private Long operatorId;

    private String operatorRole;

    private String remark;
}
