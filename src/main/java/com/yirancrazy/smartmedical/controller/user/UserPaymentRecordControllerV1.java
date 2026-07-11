package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PaymentRecordSimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yirancrazy.smartmedical.manager.PaymentRecordManager;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-01 21:18
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/v1/payment/record")
@Tag(name = "用户支付记录管理", description = "用户支付记录管理")
public class UserPaymentRecordControllerV1 {
    private final PaymentRecordManager PaymentRecordManager;

    @GetMapping("/simple/list/{userId}")
    @Operation(summary = "根据用户ID获取支付记录", description = "根据用户ID获取支付记录")
    @Parameter(name = "userId", description = "用户id", required = true)
    public Result<List<PaymentRecordSimpleResponse>> listAllPaymentRecordsSimple(@PathVariable Long userId) {
        return PaymentRecordManager.listAllPaymentRecordsSimple(userId);
    }
}