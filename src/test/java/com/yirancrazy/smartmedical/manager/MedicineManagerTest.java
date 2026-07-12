package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Medicine;
import com.yirancrazy.smartmedical.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MedicineManager 单测
 */
@ExtendWith(MockitoExtension.class)
class MedicineManagerTest {

    @Mock private MedicineService medicineService;

    @InjectMocks
    private MedicineManager medicineManager;

    @Test
    void addMedicine_setsSnowflakeMedicineIdAndDelegates() {
        when(medicineService.insertMedicine(any(Medicine.class))).thenReturn(1);
        Medicine m = new Medicine();

        int rows = medicineManager.addMedicine(m);

        assertEquals(1, rows);
        assertNotNull(m.getMedicineId());
        verify(medicineService).insertMedicine(m);
    }

    @Test
    void getMedicineById_delegates() {
        Medicine m = new Medicine();
        m.setMedicineId(99L);
        when(medicineService.getMedicineById(99L)).thenReturn(m);

        Medicine result = medicineManager.getMedicineById(99L);

        assertEquals(99L, result.getMedicineId());
    }
}
