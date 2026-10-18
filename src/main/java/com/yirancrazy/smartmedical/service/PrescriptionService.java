package com.yirancrazy.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Prescription;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 处方 Service
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方读写骨架，业务方法后续按需添加
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */
public interface PrescriptionService extends IService<Prescription> {

    /**
     * 按处方状态统计数量
     * @param status 处方状态码
     * @return 数量
     */
    Long countByStatus(Integer status);

    /**
     * 按病历ID查询处方（不存在返回 null）
     * @param medicalRecordId 病历ID
     * @return 处方
     */
    Prescription getByMedicalRecordId(Long medicalRecordId);

    /**
     * 按病历ID集合批量查询处方（空集合返回空列表，按创建时间倒序）
     * @param medicalRecordIds 病历ID集合
     * @return 处方列表
     */
    List<Prescription> listByMedicalRecordIds(Collection<Long> medicalRecordIds);

    /**
     * 按病历ID集合、状态和创建时间范围分页查询处方
     * @param medicalRecordIds 病历ID集合；null=不按病历过滤，空集合=无匹配
     * @param status 处方状态；null=不按状态过滤
     * @param createTimeStart 创建时间起；null=不限制
     * @param createTimeEnd 创建时间止；null=不限制
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<Prescription> listPrescriptionsByMedicalRecordIdsAndStatusAndCreateTimePage(
            Collection<Long> medicalRecordIds, Integer status, LocalDateTime createTimeStart,
            LocalDateTime createTimeEnd, Integer pageNum, Integer pageSize);

    /**
     * 按处方状态查询列表（按创建时间升序）
     * @param status 处方状态码
     * @return 处方列表
     */
    List<Prescription> listByStatus(Integer status);

    /**
     * 分页查询发药历史
     * @param pharmacistUserIds 发药药师用户ID集合；null=不按药师过滤，空集合=无匹配
     * @param prescriptionId 处方ID；null=不按处方过滤
     * @param orderId 订单ID；null=不按订单过滤
     * @param dispensedAtStart 发药时间起；null=不限制
     * @param dispensedAtEnd 发药时间止；null=不限制
     * @param medicalRecordIds 病历ID集合；null=不按病历过滤，空集合=无匹配
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<Prescription> listDispenseHistoryPage(
            Collection<Long> pharmacistUserIds, Long prescriptionId, Long orderId,
            LocalDateTime dispensedAtStart, LocalDateTime dispensedAtEnd,
            Collection<Long> medicalRecordIds, Integer pageNum, Integer pageSize);

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
     * 待支付处方作废（状态守卫：仅 status=0 可置为 3）
     * @param prescriptionId 处方ID
     * @return true=更新成功；false=状态已变更
     */
    boolean cancelPendingIfCurrent(Long prescriptionId);

    /**
     * 已支付处方退款取消（状态守卫：仅 status=1 可置为 3）
     * @param prescriptionId 处方ID
     * @return true=更新成功；false=状态已变更
     */
    boolean applyRefundIfCurrent(Long prescriptionId);

}
