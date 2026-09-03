package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.PaymentMethod;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.booking.confirm.PayMethodVo;
import com.yirancrazy.smartmedical.service.PayMethodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-20 15:22
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PayMethodManager {

    private final PayMethodService payMethodService;

    /**
     * 根据支付类型id获取支付类型名称
     * @param payMethodId 支付类型id
     * @return 支付类型名称
     */
    public Result<PaymentMethod> getPayMethodById(Long payMethodId) {
        return Result.success(payMethodService.getPayMethodNameById(payMethodId));
    }

    /**
     * 获取默认支付类型
     * @return 默认支付类型
     */
    public Result<PayMethodVo> getDefaultPayMethod() {
        List<PaymentMethod> paymentMethodList = payMethodService.listAllPayMethods();
        PaymentMethod defaultPayMethod = null;
        for(PaymentMethod paymentMethod : paymentMethodList){
            if(paymentMethod.getDefaulted()){
                defaultPayMethod = paymentMethod;
            }
        }

        if(defaultPayMethod ==null){
            return Result.fail("没有默认支付类型");
        }

        PayMethodVo bookingConfirmPayMethod = new PayMethodVo();
        bookingConfirmPayMethod.setId(String.valueOf(defaultPayMethod.getId()));
        bookingConfirmPayMethod.setName(defaultPayMethod.getName());
        bookingConfirmPayMethod.setIcon(defaultPayMethod.getIcon());
        bookingConfirmPayMethod.setDescription(defaultPayMethod.getDescription());
        bookingConfirmPayMethod.setDefaulted(defaultPayMethod.getDefaulted());
        bookingConfirmPayMethod.setEnabled(defaultPayMethod.getEnabled());
        bookingConfirmPayMethod.setParentId(String.valueOf(defaultPayMethod.getParentId()));
        bookingConfirmPayMethod.setSort(defaultPayMethod.getSort());

        return Result.success(bookingConfirmPayMethod);
    }

    /**
     * 获取所有支付类型
     * @return 默认支付类型
     */
    public Result<List<PaymentMethod>> getAllPayMethod() {
        return Result.success(payMethodService.listAllPayMethods());
    }

    /**
     * 获取所有一级支付类型
     * @return 一级支付类型列表
     */
    public Result<List<PayMethodVo>> listParentPayMethods() {
        List<PaymentMethod> parentPayMethodList = payMethodService.getParentPayMethod();
        List<PayMethodVo> result = new ArrayList<>();
        for(PaymentMethod paymentMethod : parentPayMethodList){
            PayMethodVo bookingConfirmPayMethod = new PayMethodVo();
            bookingConfirmPayMethod.setId(String.valueOf(paymentMethod.getId()));
            bookingConfirmPayMethod.setName(paymentMethod.getName());
            bookingConfirmPayMethod.setIcon(paymentMethod.getIcon());
            bookingConfirmPayMethod.setDescription(paymentMethod.getDescription());
            bookingConfirmPayMethod.setDefaulted(paymentMethod.getDefaulted());
            bookingConfirmPayMethod.setEnabled(paymentMethod.getEnabled());
            bookingConfirmPayMethod.setParentId(String.valueOf(paymentMethod.getParentId()));
            bookingConfirmPayMethod.setSort(paymentMethod.getSort());
            bookingConfirmPayMethod.setDescription(paymentMethod.getDescription());
            result.add(bookingConfirmPayMethod);
        }

        return Result.success(result);
    }

    /**
     * 根据一级支付类型id获取所有二级支付类型
     * @return 二级支付类型列表
     */
    public Result<List<PayMethodVo>> listAllChildPayMethods(String parentId) {
        List<PayMethodVo> bookingConfirmPayMethods = new ArrayList<>();
        List<PaymentMethod> paymentMethods = payMethodService.listChildPayMethodByParentId(Long.valueOf(parentId));
        if(paymentMethods == null){
            return Result.fail("没有二级支付类型");
        }


        // 将获取的类型转换成Vo类型
        for(PaymentMethod paymentMethod : paymentMethods){
            PayMethodVo bookingConfirmPayMethod = new PayMethodVo();
            bookingConfirmPayMethod.setId(String.valueOf(paymentMethod.getId()));
            bookingConfirmPayMethod.setName(paymentMethod.getName());
            bookingConfirmPayMethod.setIcon(paymentMethod.getIcon());
            bookingConfirmPayMethods.add(bookingConfirmPayMethod);
            bookingConfirmPayMethod.setDescription(paymentMethod.getDescription());
            bookingConfirmPayMethod.setDefaulted(paymentMethod.getDefaulted());
            bookingConfirmPayMethod.setEnabled(paymentMethod.getEnabled());
            bookingConfirmPayMethod.setParentId(String.valueOf(paymentMethod.getParentId()));
            bookingConfirmPayMethod.setSort(paymentMethod.getSort());

        }


        return Result.success(bookingConfirmPayMethods);
    }

}