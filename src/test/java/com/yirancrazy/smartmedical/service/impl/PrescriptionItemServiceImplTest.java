package com.yirancrazy.smartmedical.service.impl;

import com.yirancrazy.smartmedical.mapper.PrescriptionItemMapper;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 处方明细 Service 查询语义单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 覆盖空集合短路与按处方ID查询
 * @Datetime: 2026-10-01 02:20
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PrescriptionItemServiceImplTest {

    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;

    @InjectMocks
    private PrescriptionItemServiceImpl prescriptionItemService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(prescriptionItemService, "baseMapper", prescriptionItemMapper);
    }

    @Test
    void listByPrescriptionIds_empty_returnsEmptyWithoutQuery() {
        assertTrue(prescriptionItemService.listByPrescriptionIds(Collections.emptyList()).isEmpty());
        verifyNoInteractions(prescriptionItemMapper);
    }

    @Test
    void listByPrescriptionId_null_returnsEmptyWithoutQuery() {
        assertTrue(prescriptionItemService.listByPrescriptionId(null).isEmpty());
        verifyNoInteractions(prescriptionItemMapper);
    }

    @Test
    void listByPrescriptionIds_nonEmpty_queriesMapper() {
        PrescriptionItem item = new PrescriptionItem();
        item.setId(1L);
        when(prescriptionItemMapper.selectList(any())).thenReturn(List.of(item));

        List<PrescriptionItem> result = prescriptionItemService.listByPrescriptionIds(List.of(10L));

        assertEquals(List.of(item), result);
        verify(prescriptionItemMapper).selectList(any());
    }
}
