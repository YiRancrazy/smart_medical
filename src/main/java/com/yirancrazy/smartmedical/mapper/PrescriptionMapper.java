package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.Prescription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方 Mapper
 * @Author: YiRanCrazy@gmail.com
 * @Description: 继承 MyBatis-Plus BaseMapper，提供处方表的 CRUD（含 @Version 乐观锁）
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
}