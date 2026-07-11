package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品库存Mapper
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Mapper
public interface DrugInventoryMapper extends BaseMapper<DrugInventory> {
}
