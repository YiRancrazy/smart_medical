package com.yirancrazy.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yirancrazy.smartmedical.pojo.Prescription;

/**
 * 处方 Service
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方读写骨架，业务方法后续按需添加
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

public interface PrescriptionService extends IService<Prescription> {

    /**
     * 支付成功回调:标记处方为已支付（状态守卫：仅待支付可置为已支付，已支付幂等跳过）
     * @param orderId 订单ID
     */
    void markAsPaid(Long orderId);
}