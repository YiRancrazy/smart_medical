package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付记录实体类
 * @Datetime: 2026-02-26
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付记录实体")
public class PaymentRecord {
    @Schema(description = "支付记录ID")
    @TableId
    private Long id;

    @Schema(description = "流水号")
    private Long sn;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "支付金额")
    private Integer totalAmount;

    @Schema(description = "实际支付金额")
    private Integer realAmount;

    @Schema(description = "支付渠道: 1微信 2支付宝 3医保 4现金")
    private Integer paymentMethodId;

    @Schema(description = "支付状态: 0待支付 1支付中 2成功 3失败 4已退款")
    private Integer status;

    @Schema(description = "第三方交易流水号")
    private Long transactionSn;

    @Schema(description = "支付成功时间")
    private LocalDateTime paymentTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除: 0未删除 1已删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
