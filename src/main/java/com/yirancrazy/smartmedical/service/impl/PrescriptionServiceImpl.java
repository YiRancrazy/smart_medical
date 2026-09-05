package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.PrescriptionMapper;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处方 Service 实现
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方 Service 实现
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Service
public class PrescriptionServiceImpl
        extends ServiceImpl<PrescriptionMapper, Prescription>
        implements PrescriptionService {

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsPaid(Long orderId) {
        Prescription rx = getOne(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getOrderId, orderId)
                        .last("LIMIT 1"));
        if (rx == null) {
            log.warn("[prescription-paid] no prescription for orderId={}", orderId);
            return;
        }
        // 状态守卫：仅待支付可置为已支付，已支付幂等跳过，其他状态拒绝
        if (rx.getStatus() != null
                && rx.getStatus() == PrescriptionStatus.PAID.getCode()) {
            log.info("[prescription-paid] orderId={} already paid, skip", orderId);
            return;
        }
        if (rx.getStatus() == null
                || rx.getStatus() != PrescriptionStatus.PENDING_PAYMENT.getCode()) {
            throw new BizException(BizErrorCode.PRESCRIPTION_ALREADY_DISPENSED,
                    "处方状态非待支付，无法标记已支付");
        }
        rx.setStatus(PrescriptionStatus.PAID.getCode());
        updateById(rx);

        log.info("[prescription-paid] prescriptionId={}, orderId={}", rx.getId(), orderId);
    }
}
