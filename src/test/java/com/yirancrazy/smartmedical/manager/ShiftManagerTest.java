package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.Shift;
import com.yirancrazy.smartmedical.pojo.dto.user.response.ShiftSimpleResponse;
import com.yirancrazy.smartmedical.service.ShiftService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ShiftManager 单测
 */
@ExtendWith(MockitoExtension.class)
class ShiftManagerTest {

    @Mock
    private ShiftService shiftService;

    @InjectMocks
    private ShiftManager shiftManager;

    @Test
    void addShift_setsIdAndDelegates() {
        when(shiftService.insertShift(any(Shift.class))).thenReturn(1);

        int rows = shiftManager.addShift(new Shift());

        assertEquals(1, rows);
        verify(shiftService).insertShift(any(Shift.class));
    }

    @Test
    void getShiftById_delegates() {
        Shift shift = new Shift();
        shift.setId(1L);
        when(shiftService.getShiftById(1L)).thenReturn(shift);

        Shift result = shiftManager.getShiftById(1L);

        assertEquals(1L, result.getId());
        verify(shiftService).getShiftById(1L);
    }

    @Test
    void listShiftsSimpleResponse_mapsAllFields() {
        Shift s = new Shift();
        s.setId(11L);
        s.setName("上午");
        s.setStartTime(LocalTime.of(8, 0));
        s.setEndTime(LocalTime.of(12, 0));
        when(shiftService.listAllShifts()).thenReturn(List.of(s));

        Result<List<ShiftSimpleResponse>> result = shiftManager.listShiftsSimpleResponse();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        ShiftSimpleResponse dto = result.getData().get(0);
        assertEquals("11", dto.getId());
        assertEquals("上午", dto.getName());
        assertEquals(LocalTime.of(8, 0), dto.getStart());
        assertEquals(LocalTime.of(12, 0), dto.getEnd());
    }

    @Test
    void listShiftsSimpleResponse_emptyInputReturnsEmpty() {
        when(shiftService.listAllShifts()).thenReturn(List.of());

        Result<List<ShiftSimpleResponse>> result = shiftManager.listShiftsSimpleResponse();

        assertNotNull(result.getData());
        assertEquals(0, result.getData().size());
    }
}
