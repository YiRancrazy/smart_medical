package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者Mapper
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}
