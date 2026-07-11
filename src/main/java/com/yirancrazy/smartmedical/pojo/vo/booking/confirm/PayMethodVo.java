package com.yirancrazy.smartmedical.pojo.vo.booking.confirm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预定确认页面的支付方式
 * @Datetime: 2026-02-24 11:02
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayMethodVo {
    private String id;      // 支付方式ID
    private String parentId; // 父级支付方式ID
    private String name;   // 支付方式名称
    private String icon; // 支付方式图标
    private String description; // 描述
    private Boolean defaulted; // 是否默认
    private Boolean enabled;  // 是否启用
    private Integer sort; // 排序

}