package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.Shift;
import com.yirancrazy.smartmedical.pojo.dto.user.response.ShiftSimpleResponse;
import com.yirancrazy.smartmedical.service.ShiftService;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class ShiftManager {

    private final ShiftService shiftService;

    public int addShift(Shift shift) {
        shift.setId(IdUtil.getSnowflakeNextId());
        return shiftService.insertShift(shift);
    }

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
