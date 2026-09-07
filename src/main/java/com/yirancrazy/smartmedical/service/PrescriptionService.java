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

    /**
     * 根据关联订单ID反查处方（同一订单最多一张待支付处方）
     * @param orderId 药品订单ID
     * @return 处方，不存在返回 null
     */
    Prescription getByOrderId(Long orderId);

    /**
     * 释放处方锁定的库存并写解锁流水（FOR UPDATE 行锁串行化防并发双释放）
     * 医生作废 / 用户退款 / 系统超时统一走此原语
     * @param rx 处方
     * @param operatorId 操作人ID（系统传 0L）
     * @param operatorName 操作人角色（doctor/user/system）
     * @param remark 流水备注
     */
    void releaseLockedStock(Prescription rx, Long operatorId, String operatorName, String remark);

    /**
     * 超时作废待支付处方：先守卫置状态 0→3（仅待支付可取消），成功后释放其锁定库存并写解锁流水
     * 守卫失败（已被支付/取消）返回 false，不释放库存，由调用方跳过
     * @param prescriptionId 处方ID
     * @param remark 作废原因备注
     * @return true=已作废并释放库存；false=处方状态已变更，未生效
     */
    boolean cancelExpiredPendingPrescription(Long prescriptionId, String remark);
}