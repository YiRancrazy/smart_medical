package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 病历 Mapper
 * @Author: YiRanCrazy@gmail.com
 * @Description: 继承 MyBatis-Plus BaseMapper，提供电子病历表的 CRUD
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
}