package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Shift;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 班次服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface ShiftService {

    /**
     * 插入班次
     * @param shift 班次
     * @return 插入结果
     */
    Integer insertShift(Shift shift);

    /**
     * 根据ID查询班次
     * @param id 班次ID
     * @return 班次信息
     */
    Shift getShiftById(Long id);

    /**
     * 根据ID更新班次
     * @param shift 班次
     * @return 更新结果
     */
    Integer updateShiftById(Shift shift);

    /**
     * 根据ID删除班次
     * @param id 班次ID
     * @return 删除结果
     */
    Integer deleteShiftById(Long id);

    /**
     * 查询所有班次
     * @return 班次列表
     */
    List<Shift> listAllShifts();

    /**
     * 分页查询班次
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 班次列表
     */
    PageInfo<Shift> listShiftsByPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除班次
     * @param ids 班次ID列表
     * @return 删除结果
     */
    Integer deleteShiftByIds(List<Long> ids);
}
