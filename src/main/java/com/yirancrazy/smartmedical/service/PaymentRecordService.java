package com.yirancrazy.smartmedical.service;

import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.PaymentRecord;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付记录服务接口层
 * @Datetime: 2026-02-26 07:27
 * @Version: 1.0
 */

public interface PaymentRecordService {

    /**
     * 添加支付记录
     * @param paymentRecord 支付记录
     * @return 添加结果
     */
    int insertPaymentRecord(PaymentRecord paymentRecord);

    /**
     * 根据id删除支付记录
     * @param id 支付记录id
     * @return 删除结果
     */
    int deletePaymentRecordById(Long id);

    /**
     * 根据id更新支付记录
     * @param paymentRecord 支付记录
     * @return 更新结果
     */
    int updatePaymentRecordById(PaymentRecord paymentRecord);

    /**
     * 根据id查询支付记录
     * @param id 支付记录id
     * @return 支付记录
     */
    PaymentRecord getPaymentRecordById(Long id);

    /**
     * 查询所有支付记录
     * @return 支付记录列表
     */
    List<PaymentRecord> listAllPaymentRecords();

    /**
     * 根据订单id查询所有支付记录
     * @param orderIds 订单id列表
     * @return 支付记录列表
     */
    List<PaymentRecord> listAllPaymentRecordsByOrderId(List<Long> orderIds);
}
