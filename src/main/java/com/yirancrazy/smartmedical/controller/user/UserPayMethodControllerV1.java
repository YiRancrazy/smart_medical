package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.pojo.PaymentMethod;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.booking.confirm.PayMethodVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yirancrazy.smartmedical.manager.PayMethodManager;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-20 15:25
 * @Version: 1.0
 */


@RestController
@RequiredArgsConstructor
@Tag(name = "支付类型管理", description = "支付类型管理")
@RequestMapping("api/user/v1/payment/method")
public class UserPayMethodControllerV1 {

    private final PayMethodManager PayMethodManager;

    /**
     * 根据ID获取支付类型
     * @param id 支付类型ID
     * @return 支付类型
     */
    @GetMapping("/{id:\\d+}")
    public Result<PaymentMethod> getPayMethodById(@PathVariable("id") Long id) {
        return PayMethodManager.getPayMethodById(id);
    }

    /**
     * 获取默认支付类型
     * @return 默认支付类型
     */
    @GetMapping("/default")
    @Tag(name = "获取默认支付类型", description = "获取默认支付类型接口")
    public Result<PayMethodVo> getDefaultPayMethod() {
        return PayMethodManager.getDefaultPayMethod();
    }

    /**
     * 获取所有支付类型
     * @return 支付类型列表
     */
    @GetMapping("/all")
    @Tag(name = "获取所有支付类型", description = "获取所有支付类型接口")
    public Result<List<PaymentMethod>> getAllPayMethod() {
        return PayMethodManager.getAllPayMethod();
    }

    /**
     * 获取所有一级支付类型
     * @return 一级支付类型列表
     */
    @GetMapping("/list/all/parent")
    @Tag(name = "获取所有一级支付类型", description = "获取所有一级支付类型接口")
    public Result<List<PayMethodVo>> listParentPayMethods() {
        return PayMethodManager.listParentPayMethods();
    }


}