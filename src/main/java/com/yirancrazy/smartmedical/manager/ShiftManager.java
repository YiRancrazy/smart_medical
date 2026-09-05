package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.Shift;
import com.yirancrazy.smartmedical.pojo.dto.user.response.ShiftSimpleResponse;
import com.yirancrazy.smartmedical.service.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class ShiftManager {

    private final ShiftService shiftService;

    /**
     * 新增班次（补雪花 ID 后入库）
     * @param shift 班次实体
     * @return 影响行数
     */
    public int addShift(Shift shift) {
        shift.setId(IdUtil.getSnowflakeNextId());
        return shiftService.insertShift(shift);
    }

    /**
     * 按 ID 查询班次
     * @param id 班次 ID
     * @return 班次实体；不存在返回 null
     */
    public Shift getShiftById(Long id) {
        return shiftService.getShiftById(id);
    }

    /**
     * 获取所有班次信息
     * @return 班次信息
     */
    public Result<List<ShiftSimpleResponse>> listShiftsSimpleResponse() {
        List<Shift> shifts = shiftService.listAllShifts();
        List<ShiftSimpleResponse> shiftSimpleResponses = shifts
                .stream()
                .map(shift -> new ShiftSimpleResponse(String.valueOf(
                        shift.getId()),
                        shift.getName(),
                        shift.getStartTime(),
                        shift.getEndTime()))
                .toList();
        return Result.success(shiftSimpleResponses);
    }
}
