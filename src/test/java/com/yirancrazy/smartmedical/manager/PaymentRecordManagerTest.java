package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.mapper.OrdersMapper;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.service.*;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentRecordManager 单测
 * 覆盖 BUG-B05: 支付回调 transactionSn 去重
 */
@ExtendWith(MockitoExtension.class)
class PaymentRecordManagerTest {

    @InjectMocks
    private PaymentRecordManager paymentRecordManager;

    @Mock
    private PaymentRecordService paymentRecordService;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private PatientCardService patientCardService;
    @Mock
    private PatientService patientService;
    @Mock
    private OrderService orderService;
    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private OrderTypeService orderTypeService;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private ProductionTypeService productionTypeService;
    @Mock
    private PayMethodService payMethodService;
    @Mock
    private PrescriptionManager prescriptionManager;
    @Mock
    private RegistrationService registrationService;
    @Mock
    private RegistrationStatusLogManager registrationStatusLogManager;
    @Mock
    private OrderStatusLogManager orderStatusLogManager;

    @Test
    void paySuccess_duplicateTransactionSn_shouldSkip() {
        Long transactionSn = 9876543210L;
        when(redisUtil.setIfAbsent(eq("payment:txn:" + transactionSn), eq("1"), eq(24L), eq(TimeUnit.HOURS)))
                .thenReturn(Boolean.FALSE);

        Result<Void> result = paymentRecordManager.paySuccess(1L, null, 1, transactionSn, 100);

        assertEquals(200, result.getCode());
        assertNull(result.getData());
        verify(orderService, never()).getOrderById(anyLong());
    }
}
