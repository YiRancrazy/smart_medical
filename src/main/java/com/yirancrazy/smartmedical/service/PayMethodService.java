package com.yirancrazy.smartmedical.service;

import com.yirancrazy.smartmedical.pojo.PaymentMethod;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-20 15:16
 * @Version: 1.0
 */

public interface PayMethodService {

    /**
     * 根据支付类型id获取支付类型名称
     * @param PayMethodId 支付类型id
     * @return 支付类型名称
     */
    PaymentMethod getPayMethodNameById(Long PayMethodId);

    /**
     * 获取所有支付类型
     * @return 所有支付类型
     */
    List<PaymentMethod> listAllPayMethods();

    /**
     * 获取所有一级支付类型
     * @return 一级支付类型列表
     */
    List<PaymentMethod> getParentPayMethod();

    /**
     * 获取所有二级支付类型
     * @return 二级支付类型列表
     */
    List<PaymentMethod> listChildPayMethodByParentId(Long parentId);


}