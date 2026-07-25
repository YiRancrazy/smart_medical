package com.yirancrazy.smartmedical.service.impl;

import com.yirancrazy.smartmedical.mapper.DoctorPositionMapper;
import com.yirancrazy.smartmedical.pojo.DoctorPosition;
import com.yirancrazy.smartmedical.service.DoctorPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生职位服务实现类
 * @Datetime: 2026-02-20 09:01
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class DoctorPositionServiceImpl implements DoctorPositionService {

    private final DoctorPositionMapper doctorPositionMapper;

    /**
     * 添加医生职位
     * @param doctorPosition 医生职位
     * @return 添加结果
     */
    @Override
    public Integer insertDoctorPosition(DoctorPosition doctorPosition) {
        return doctorPositionMapper.insert(doctorPosition);
    }

    /**
     * 删除医生职位
     * @param id 医生职位ID
     * @return 删除结果
     */
    @Override
    public Integer deleteDoctorPositionById(Long id) {
        return doctorPositionMapper.deleteById(id);
    }

    /**
     * 修改医生职位
     * @param doctorPosition 医生职位
     * @return 修改结果
     */
    @Override
    public Integer updateDoctorPositionById(DoctorPosition doctorPosition) {
        return doctorPositionMapper.updateById(doctorPosition);
    }

    /**
     * 根据ID获取医生职位
     * @param id 医生职位ID
     * @return 医生职位
     */
    @Override
    public DoctorPosition getPositionById(Long id) {
        return doctorPositionMapper.selectById(id);
    }

    /**
     * 根据ID列表批量查询医生职位
     * @param ids 医生职位ID列表
     * @return 医生职位列表
     */
    @Override
    public List<DoctorPosition> listPositionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return doctorPositionMapper.selectByIds(ids);
    }

    /**
     * 获取所有医生职位
     * @return 医生职位列表
     */
    @Override
    public List<DoctorPosition> listDoctorPositions() {
        return doctorPositionMapper.selectList(null);
    }

    /**
     * 获取医生职位数量
     * @return 医生职位数量
     */
    @Override
    public Long countPosition() {
        return doctorPositionMapper.selectCount(null);
    }
}