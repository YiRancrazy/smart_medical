package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yirancrazy.smartmedical.mapper.PayMethodMapper;
import com.yirancrazy.smartmedical.pojo.PaymentMethod;
import com.yirancrazy.smartmedical.service.PayMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-20 15:18
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class PayMethodServiceImpl implements PayMethodService {

    private final PayMethodMapper PayMethodMapper;

    /**
     * 根据支付类型id获取支付类型名称
     * @param payMethodId 支付类型id
     * @return 支付类型名称
     */
    @Override
    public PaymentMethod getPayMethodNameById(Long payMethodId) {
        return PayMethodMapper.selectById(payMethodId);
    }

    /**
     * 获取所有支付类型
     * @return 所有支付类型
     */
    @Override
    public List<PaymentMethod> listAllPayMethods() {
        return PayMethodMapper.selectList(null);
    }

    /**
     * 获取所有一级支付类型
     * @return 一级支付类型列表
     */
    @Override
    public List<PaymentMethod> getParentPayMethod() {
        return PayMethodMapper.selectList(new QueryWrapper<PaymentMethod>().isNull( "parent_id"));
    }

    /**
     * 获取所有二级支付类型
     * @param parentId 一级支付类型id
     * @return 二级支付类型列表
     */
    @Override
    public List<PaymentMethod> listChildPayMethodByParentId(Long parentId) {
        return null;
    }
}