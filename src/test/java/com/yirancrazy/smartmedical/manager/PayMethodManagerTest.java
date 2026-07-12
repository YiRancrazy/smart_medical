package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.PaymentMethod;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.booking.confirm.PayMethodVo;
import com.yirancrazy.smartmedical.service.PayMethodService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * PayMethodManager 单测
 */
@ExtendWith(MockitoExtension.class)
class PayMethodManagerTest {

    @Mock private PayMethodService payMethodService;

    @InjectMocks
    private PayMethodManager payMethodManager;

    @Test
    void getPayMethodById_wrapsServiceCall() {
        PaymentMethod pm = new PaymentMethod();
        pm.setId(1L);
        when(payMethodService.getPayMethodNameById(1L)).thenReturn(pm);

        Result<PaymentMethod> result = payMethodManager.getPayMethodById(1L);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    void getDefaultPayMethod_returnsDefaultedPayment() {
        PaymentMethod pm = new PaymentMethod();
        pm.setId(1L);
        pm.setName("微信");
        pm.setIcon("icon.png");
        pm.setDescription("微信支付");
        pm.setDefaulted(true);
        pm.setEnabled(true);
        pm.setParentId(0L);
        pm.setSort(1);

        when(payMethodService.listAllPayMethods()).thenReturn(List.of(pm));

        Result<PayMethodVo> result = payMethodManager.getDefaultPayMethod();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("1", result.getData().getId());
        assertEquals("微信", result.getData().getName());
        assertEquals(true, result.getData().getDefaulted());
    }

    @Test
    void getDefaultPayMethod_noDefault_returnsFail() {
        PaymentMethod pm = new PaymentMethod();
        pm.setDefaulted(false);

        when(payMethodService.listAllPayMethods()).thenReturn(List.of(pm));

        Result<PayMethodVo> result = payMethodManager.getDefaultPayMethod();

        assertEquals(500, result.getCode());
    }

    @Test
    void getAllPayMethod_returnsList() {
        PaymentMethod pm1 = new PaymentMethod();
        pm1.setId(1L);
        when(payMethodService.listAllPayMethods()).thenReturn(List.of(pm1));

        Result<List<PaymentMethod>> result = payMethodManager.getAllPayMethod();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }
}
