package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yirancrazy.smartmedical.mapper.PaymentRecordMapper;
import com.yirancrazy.smartmedical.pojo.PaymentRecord;
//import com.yirancrazy.smartmedical.pojo.dto.user.response.PaymentRecordResponseSimple;
import com.yirancrazy.smartmedical.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付记录服务实现类
 * @Datetime: 2026-02-26 07:30
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl implements PaymentRecordService {

    private final PaymentRecordMapper paymentRecordMapper;

    /**
     * 添加支付记录
     * @param paymentRecord 支付记录
     * @return 添加结果
     */
    @Override
    public int insertPaymentRecord(PaymentRecord paymentRecord) {
        return paymentRecordMapper.insert(paymentRecord);
    }

    /**
     * 根据id删除支付记录
     * @param id 支付记录id
     * @return 删除结果
     */
    @Override
    public int deletePaymentRecordById(Long id) {
        return paymentRecordMapper.deleteById(id);
    }

    /**
     * 根据id更新支付记录
     * @param paymentRecord 支付记录
     * @return 更新结果
     */
    @Override
    public int updatePaymentRecordById(PaymentRecord paymentRecord) {
        return paymentRecordMapper.updateById(paymentRecord);
    }

    /**
     * 根据id查询支付记录
     * @param id 支付记录id
     * @return 支付记录
     */
    @Override
    public PaymentRecord getPaymentRecordById(Long id) {
        return paymentRecordMapper.selectById(id);
    }

    /**
     * 查询所有支付记录
     * @return 支付记录列表
     */
    @Override
    public List<PaymentRecord> listAllPaymentRecords() {
        return paymentRecordMapper.selectList(null);
    }

    /**
     * 根据订单id查询所有支付记录
     * @param orderIds 订单id列表
     * @return 支付记录列表
     */
    @Override
    public List<PaymentRecord> listAllPaymentRecordsByOrderId(List<Long> orderIds) {
        return paymentRecordMapper.selectList(new QueryWrapper<PaymentRecord>().in("order_id", orderIds));
    }
}