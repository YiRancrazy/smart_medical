package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.PaymentMethod;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-20 15:15
 * @Version: 1.0
 */

@Mapper
public interface PayMethodMapper extends BaseMapper<PaymentMethod> {
}