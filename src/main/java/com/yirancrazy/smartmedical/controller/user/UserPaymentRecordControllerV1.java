package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PaymentRecordSimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.yirancrazy.smartmedical.manager.PaymentRecordManager;

import java.util.List;

/**
 * 用户支付记录 + 支付回调 Controller
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户支付记录查询 + 模拟支付成功端点
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/v1/payment/record")
@Tag(name = "用户支付记录管理", description = "用户支付记录管理")
public class UserPaymentRecordControllerV1 {
    private final PaymentRecordManager PaymentRecordManager;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @GetMapping("/simple/list")
    @Operation(summary = "获取当前用户支付记录", description = "获取当前用户支付记录")
    public Result<List<PaymentRecordSimpleResponse>> listAllPaymentRecordsSimple(@RequestAttribute("currentUserId") Long userId) {
        return PaymentRecordManager.listAllPaymentRecordsSimple(userId);
    }

    /**
     * 用户端 - 支付订单(模拟支付成功,实际应接入第三方回调)
     * @param orderId 订单ID
     * @param paymentMethodId 支付方式ID(可选)
     * @param transactionSn 第三方流水号(可选)
     * @param realAmount 实际支付金额(可选,默认订单总额)
     */
    @PostMapping("/pay")
    @Operation(summary = "用户端 - 支付订单(模拟支付成功,实际应接入第三方回调)")
    public Result<Void> pay(@RequestParam Long orderId,
                            @RequestAttribute("currentUserId") Long userId,
                            @RequestParam(required = false) Integer paymentMethodId,
                            @RequestParam(required = false) Long transactionSn,
                            @RequestParam(required = false) Integer realAmount) {
        // 模拟支付仅 dev 环境可用，生产环境必须接入真实支付回调
        if (!"dev".equals(activeProfile)) {
            throw new BizException(BizErrorCode.OPERATION_NOT_SUPPORTED, "模拟支付仅开发环境可用");
        }
        return PaymentRecordManager.paySuccess(orderId, userId, paymentMethodId, transactionSn, realAmount);
    }
}