package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 库存异动流水Mapper
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {
}
