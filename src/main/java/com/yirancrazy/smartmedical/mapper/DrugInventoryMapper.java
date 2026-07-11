package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品库存Mapper
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Mapper
public interface DrugInventoryMapper extends BaseMapper<DrugInventory> {

    /**
     * 行级悲观锁查询：SELECT ... FOR UPDATE，按 drug_id 加排他锁
     * 用于发药大事务中扣减库存前的并发守门
     * @param drugId 药品ID
     * @return 库存记录(已锁定)
     */
    @Select("SELECT * FROM drug_inventory WHERE drug_id = #{drugId} LIMIT 1 FOR UPDATE")
    DrugInventory selectForUpdate(@Param("drugId") Long drugId);
}