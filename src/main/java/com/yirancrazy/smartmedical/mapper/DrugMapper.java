package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.Drug;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品信息Mapper
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Mapper
public interface DrugMapper extends BaseMapper<Drug> {
}
