package com.yirancrazy.smartmedical.service;

import com.yirancrazy.smartmedical.pojo.DoctorPosition;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生职位服务
 * @Datetime: 2026-02-20 09:00
 * @Version: 1.0
 */


public interface DoctorPositionService {

    /**
     * 添加医生职位
     * @param doctorPosition 医生职位
     * @return 添加结果
     */
    Integer insertDoctorPosition(DoctorPosition doctorPosition);

    /**
     * 删除医生职位
     * @param id 医生职位ID
     * @return 删除结果
     */
    Integer deleteDoctorPositionById(Long id);

    /**
     * 修改医生职位
     * @param doctorPosition 医生职位
     * @return 修改结果
     */
    Integer updateDoctorPositionById(DoctorPosition doctorPosition);

    /**
     * 根据ID获取医生职位
     * @param id 医生职位ID
     * @return 医生职位
     */
    DoctorPosition getPositionById(Long id);

    /**
     * 获取所有医生职位
     * @return 医生职位列表
     */
    List<DoctorPosition> listDoctorPositions();

    /**
     * 获取医生职位数量
     * @return 医生职位数量
     */
    Long countPosition();
}
