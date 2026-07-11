package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.ShiftMapper;
import com.yirancrazy.smartmedical.pojo.Shift;
import com.yirancrazy.smartmedical.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 班次服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftMapper shiftMapper;

    /**
     * 插入班次
     * @param shift 班次
     * @return 插入结果
     */
    @Override
    public Integer insertShift(Shift shift) {
        return shiftMapper.insert(shift);
    }

    /**
     * 根据ID查询班次
     * @param id 班次ID
     * @return 班次
     */
    @Override
    public Shift getShiftById(Long id) {
        return shiftMapper.selectById(id);
    }

    /**
     * 根据ID更新班次
     * @param shift 班次
     * @return 更新结果
     */
    @Override
    public Integer updateShiftById(Shift shift) {
        return shiftMapper.updateById(shift);
    }

    /**
     * 根据ID删除班次
     * @param id 班次ID
     * @return 删除结果
     */
    @Override
    public Integer deleteShiftById(Long id) {
        return shiftMapper.deleteById(id);
    }

    /**
     * 查询所有班次
     * @return 班次列表
     */
    @Override
    public List<Shift> listAllShifts() {
        return shiftMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页查询班次
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 班次列表
     */
    @Override
    public PageInfo<Shift> listShiftsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shift> shifts = shiftMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(shifts);
    }

    /**
     * 批量删除班次
     * @param ids 班次ID列表
     * @return 删除结果
     */
    @Override
    public Integer deleteShiftByIds(List<Long> ids) {
        return shiftMapper.deleteByIds(ids);
    }

}
