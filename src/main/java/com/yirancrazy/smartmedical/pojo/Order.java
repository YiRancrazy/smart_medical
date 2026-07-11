package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单实体")
@TableName(value = "`order`")
public class Order {
    @Schema(description = "订单唯一标识")
    @TableId
    private Long id;

    @Schema(description = "下单用户id")
    private Long userId;

    @Schema(description = "订单类型id")
    private Long orderTypeId;

    @Schema(description = "订单号，用于对外展示")
    private Long sn;

    @Schema(description = "订单状态0完成，1、未支付、2、关闭")
    private Integer status;

    @Schema(description = "实际支付金额")
    private Integer totalAmount;

    @Schema(description = "订单创建时间")
    private LocalDateTime orderCreateTime;

    @Schema(description = "订单更新时间")
    private LocalDateTime orderUpdateTime;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
