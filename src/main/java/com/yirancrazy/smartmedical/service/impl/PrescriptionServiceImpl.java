package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.PrescriptionMapper;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 处方 Service 实现
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方 Service 实现
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl
        extends ServiceImpl<PrescriptionMapper, Prescription>
        implements PrescriptionService {

    /**
     * {@inheritDoc}
     */
    @Override
    public Long countByStatus(Integer status) {
        return baseMapper.selectCount(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getStatus, status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Prescription getByMedicalRecordId(Long medicalRecordId) {
        if (medicalRecordId == null) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getMedicalRecordId, medicalRecordId)
                .last("LIMIT 1"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Prescription> listByMedicalRecordIds(Collection<Long> medicalRecordIds) {
        if (medicalRecordIds == null || medicalRecordIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<Prescription>()
                .in(Prescription::getMedicalRecordId, medicalRecordIds)
                .orderByDesc(Prescription::getCreateTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<Prescription> listPrescriptionsByMedicalRecordIdsAndStatusAndCreateTimePage(
            Collection<Long> medicalRecordIds, Integer status, LocalDateTime createTimeStart,
            LocalDateTime createTimeEnd, Integer pageNum, Integer pageSize) {
        if (medicalRecordIds != null && medicalRecordIds.isEmpty()) {
            PageHelper.clearPage();
            return new PageInfo<>(List.of());
        }

        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getDeleted, false)
                .orderByDesc(Prescription::getCreateTime);
        if (medicalRecordIds != null) {
            wrapper.in(Prescription::getMedicalRecordId, medicalRecordIds);
        }
        if (status != null) {
            wrapper.eq(Prescription::getStatus, status);
        }
        if (createTimeStart != null) {
            wrapper.ge(Prescription::getCreateTime, createTimeStart);
        }
        if (createTimeEnd != null) {
            wrapper.le(Prescription::getCreateTime, createTimeEnd);
        }

        int normalizedPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage(normalizedPageNum, normalizedPageSize);
        return new PageInfo<>(list(wrapper));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Prescription> listByStatus(Integer status) {
        return list(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getStatus, status)
                .orderByAsc(Prescription::getCreateTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<Prescription> listDispenseHistoryPage(
            Collection<Long> pharmacistUserIds, Long prescriptionId, Long orderId,
            LocalDateTime dispensedAtStart, LocalDateTime dispensedAtEnd,
            Collection<Long> medicalRecordIds, Integer pageNum, Integer pageSize) {
        if ((pharmacistUserIds != null && pharmacistUserIds.isEmpty())
                || (medicalRecordIds != null && medicalRecordIds.isEmpty())) {
            PageHelper.clearPage();
            return new PageInfo<>(List.of());
        }

        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getStatus, PrescriptionStatus.DISPENSED.getCode())
                .orderByDesc(Prescription::getDispensedAt);
        if (pharmacistUserIds != null) {
            wrapper.in(Prescription::getPharmacistId, pharmacistUserIds);
        }
        if (prescriptionId != null) {
            wrapper.eq(Prescription::getId, prescriptionId);
        }
        if (orderId != null) {
            wrapper.eq(Prescription::getOrderId, orderId);
        }
        if (dispensedAtStart != null) {
            wrapper.ge(Prescription::getDispensedAt, dispensedAtStart);
        }
        if (dispensedAtEnd != null) {
            wrapper.le(Prescription::getDispensedAt, dispensedAtEnd);
        }
        if (medicalRecordIds != null) {
            wrapper.in(Prescription::getMedicalRecordId, medicalRecordIds);
        }

        int normalizedPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage(normalizedPageNum, normalizedPageSize);
        return new PageInfo<>(list(wrapper));
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Prescription getByOrderId(Long orderId) {
        return getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getOrderId, orderId)
                .last("LIMIT 1"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cancelPendingIfCurrent(Long prescriptionId) {
        if (prescriptionId == null) {
            return false;
        }
        return update(new UpdateWrapper<Prescription>()
                .eq("id", prescriptionId)
                .eq("status", PrescriptionStatus.PENDING_PAYMENT.getCode())
                .set("status", PrescriptionStatus.CANCELLED.getCode()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applyRefundIfCurrent(Long prescriptionId) {
        if (prescriptionId == null) {
            return false;
        }
        return update(new UpdateWrapper<Prescription>()
                .eq("id", prescriptionId)
                .eq("status", PrescriptionStatus.PAID.getCode())
                .set("status", PrescriptionStatus.CANCELLED.getCode()));
    }

}
