package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付记录持久层
 * @Datetime: 2026-02-26 07:26
 * @Version: 1.0
 */

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}