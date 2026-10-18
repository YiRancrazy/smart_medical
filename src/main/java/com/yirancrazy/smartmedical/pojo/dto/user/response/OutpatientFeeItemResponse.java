package com.yirancrazy.smartmedical.pojo.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户端 - 门诊费用列表项
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 门诊费用列表项
 * @Datetime: 2026-07-25 14:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutpatientFeeItemResponse {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 订单类型ID
     */
    private String orderTypeId;

    /**
     * 订单类型名称
     */
    private String orderTypeName;

    /**
     * 订单状态码
     */
    private Integer status;

    /**
     * 订单状态名称
     */
    private String statusName;

    /**
     * 订单金额（分）
     */
    private Integer totalAmount;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;
}
