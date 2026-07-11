package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Mapper
public interface PatientCardMapper extends BaseMapper<PatientCard> {

    /**
     * 根据用户id查询默认的就诊卡信息
     * @param userId 用户id
     * @return 就诊卡信息
     */
    PatientCard getDefaultPatientBaseInfoByUserId(Long userId);
}
