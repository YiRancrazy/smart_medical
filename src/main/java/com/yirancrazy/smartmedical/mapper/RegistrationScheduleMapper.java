package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班Mapper接口
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Mapper
public interface RegistrationScheduleMapper extends BaseMapper<RegistrationSchedule> {

    List<RegistrationScheduleTemplate> getRecentRegistrationListByDoctorIdList(List<Long> doctorIdList);

    List<RegistrationSchedule> getRegistrationScheduleLastWeekByDoctorId(Long doctorId);

    @Select("SELECT * FROM registration_schedule WHERE doctor_id = #{doctorId} AND DATE(start_time) = #{date}")
    List<RegistrationSchedule> getRegistrationSchedulesByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    /**
     * 获取指定医生指定日期内的挂号排班信息
     * @param doctorId 医生ID
     * @param maxAdvanceDays 最大提前天数
     * @return 挂号排班信息
     */
    List<RegistrationSchedule> listRegistrationSchedulesByDoctorIdAndMaxAdvanceDays(Long doctorId, Integer maxAdvanceDays);
}
