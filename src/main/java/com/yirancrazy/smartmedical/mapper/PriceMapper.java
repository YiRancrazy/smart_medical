package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.Price;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号价格Mapper
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

@Mapper
public interface PriceMapper extends BaseMapper<Price> {
}
