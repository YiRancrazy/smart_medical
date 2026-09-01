package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生Mapper
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    // 批量根据科室id 批量查询过去七天内有挂号的医生id
    @Select("SELECT DISTINCT d.doctor_id " +
            "FROM doctor d " +
            "INNER JOIN registration_schedule rs ON d.id = rs.doctor_id " +
            "WHERE d.department_id = #{departmentId} " +
            "AND rs.schedule_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY);")
    List<Long> getDoctorIdListByDepartmentIdLastWeek(Long departmentId);

    @Select("SELECT DISTINCT d.* " +
            "FROM doctor d " +
            "INNER JOIN registration_schedule rs ON d.id = rs.doctor_id " +
            "WHERE d.department_id = #{departmentId} " +
            "AND rs.schedule_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY);")
    List<Doctor> getDoctorListByDepartmentIdLastWeek(Long departmentId);


    /**
     * 根据医生id列表、状态、最大提前天数、最小提前小时数查询医生列表
     * @param doctorIds 医生id列表
     * @param status 状态
     * @param maxAdvanceDays 最大提前天数
     * @return 医生列表
     */
    // todo 有问题
    List<Doctor> listDoctorsByDoctorIdsAndStatusAndMaxAdvanceDays(List<Long> doctorIds, Integer status, Integer maxAdvanceDays);
}
