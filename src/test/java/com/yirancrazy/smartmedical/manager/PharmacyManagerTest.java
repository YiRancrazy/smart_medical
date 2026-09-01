package com.yirancrazy.smartmedical.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.mapper.DrugInventoryMapper;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.PendingPrescriptionVO;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import com.yirancrazy.smartmedical.service.DrugService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.DispenseVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PharmacyManager#listPending / listLowStock 单测
 * 覆盖：F31 分页改造后的 VO 映射 + PageResult 结构。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药师端列表查询单测
 * @Datetime: 2026-07-24 17:30
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PharmacyManagerTest {

    @Mock private PrescriptionService prescriptionService;
    @Mock private PrescriptionItemService prescriptionItemService;
    @Mock private DrugService drugService;
    @Mock private DrugInventoryService drugInventoryService;
    @Mock private DrugInventoryMapper drugInventoryMapper;
    @Mock private InventoryTransactionService inventoryTransactionService;
    @Mock private RegistrationService registrationService;
    @Mock private MedicalRecordService medicalRecordService;
    @Mock private OrderService orderService;
    @Mock private RegistrationStatusLogManager statusLogManager;
    @Mock private OrderStatusLogManager orderStatusLogManager;

    @InjectMocks
    private PharmacyManager pharmacyManager;

    /**
     * listPending happy path：1 条已支付处方，关联病历+挂号，VO 字段正确映射
     */
    @Test
    @SuppressWarnings("unchecked")
    void listPending_happyPath_mapsVoCorrectly() {
        Prescription rx = new Prescription();
        rx.setId(1001L);
        rx.setOrderId(2001L);
        rx.setTotalAmount(5000);
        rx.setMedicalRecordId(3001L);

        MedicalRecord record = new MedicalRecord();
        record.setId(3001L);
        record.setRegistrationId(4001L);

        Registration reg = new Registration();
        reg.setId(4001L);
        reg.setUserId(5001L);

        when(prescriptionService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(rx));
        when(medicalRecordService.listByIds(List.of(3001L))).thenReturn(List.of(record));
        when(registrationService.listRegistrationsByIds(List.of(4001L))).thenReturn(List.of(reg));

        Result<PageResult<PendingPrescriptionVO>> result = pharmacyManager.listPending(null, null);

        assertEquals(200, result.getCode());
        PageResult<PendingPrescriptionVO> data = result.getData();
        assertNotNull(data);
        assertEquals(1, data.getList().size());
        assertEquals(1L, data.getTotal());
        PendingPrescriptionVO vo = data.getList().get(0);
        assertEquals(1001L, vo.getPrescriptionId());
        assertEquals(2001L, vo.getOrderId());
        assertEquals(5000, vo.getTotalAmount());
        assertEquals(5001L, vo.getPatientId());
        assertEquals(4001L, vo.getRegistrationSn());
    }

    /**
     * listPending 空列表：返回空 PageResult
     */
    @Test
    void listPending_empty_returnsEmptyPageResult() {
        when(prescriptionService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        Result<PageResult<PendingPrescriptionVO>> result = pharmacyManager.listPending(null, null);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().getList().isEmpty());
        assertEquals(0L, result.getData().getTotal());
    }

    /**
     * listPending 处方无 medicalRecordId：VO 中 patientId/registrationSn 为 null
     */
    @Test
    void listPending_noMedicalRecord_patientIdIsNull() {
        Prescription rx = new Prescription();
        rx.setId(1002L);
        rx.setTotalAmount(3000);
        // medicalRecordId 留 null

        when(prescriptionService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(rx));

        Result<PageResult<PendingPrescriptionVO>> result = pharmacyManager.listPending(null, null);

        PendingPrescriptionVO vo = result.getData().getList().get(0);
        assertNull(vo.getPatientId());
        assertNull(vo.getRegistrationSn());
    }

    /**
     * listLowStock happy path：返回库存低于 min_stock 的列表
     */
    @Test
    @SuppressWarnings("unchecked")
    void listLowStock_returnsFilteredInventories() {
        DrugInventory lowInv = new DrugInventory();
        lowInv.setId(1L);
        lowInv.setStockQuantity(5);
        lowInv.setMinStock(10);

        when(drugInventoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(lowInv));

        Result<PageResult<DrugInventory>> result = pharmacyManager.listLowStock(null, null);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getTotal());
        DrugInventory inv = result.getData().getList().get(0);
        assertEquals(5, inv.getStockQuantity());
        assertEquals(10, inv.getMinStock());
    }

    /**
     * listLowStock 空列表
     */
    @Test
    @SuppressWarnings("unchecked")
    void listLowStock_empty_returnsEmpty() {
        when(drugInventoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        Result<PageResult<DrugInventory>> result = pharmacyManager.listLowStock(null, null);

        assertTrue(result.getData().getList().isEmpty());
        assertEquals(0L, result.getData().getTotal());
    }

    // ===== dispense() 测试 =====

    @Test
    @SuppressWarnings("unchecked")
    void dispense_happyPath_dispensesAllItems() {
        Long pharmacistId = 3001L;
        Prescription rx = new Prescription();
        rx.setId(1001L);
        rx.setMedicalRecordId(2001L);
        rx.setOrderId(4001L);
        rx.setStatus(PrescriptionStatus.PAID.getCode());

        PrescriptionItem item = new PrescriptionItem();
        item.setDrugId(5001L);
        item.setQuantity(2);

        Drug drug = new Drug();
        drug.setId(5001L);
        drug.setCommonName("阿莫西林");

        DrugInventory inv = new DrugInventory();
        inv.setDrugId(5001L);
        inv.setId(6001L);
        inv.setWarehouseId(7001L);
        inv.setStockQuantity(20);
        inv.setLockedQuantity(5);
        inv.setAvailableQuantity(15);

        MedicalRecord record = new MedicalRecord();
        record.setId(2001L);
        record.setRegistrationId(8001L);

        Registration reg = new Registration();
        reg.setId(8001L);
        reg.setStatus(RegistrationStatusEnum.SUCCESS.getCode());

        Order order = new Order();
        order.setId(4001L);
        order.setStatus(OrderStatus.PAID.getCode());

        when(prescriptionService.getById(1001L)).thenReturn(rx);
        when(prescriptionItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        when(drugService.listDrugsByIds(List.of(5001L))).thenReturn(List.of(drug));
        when(drugInventoryService.selectForUpdate(5001L)).thenReturn(inv);
        when(medicalRecordService.getById(2001L)).thenReturn(record);
        when(registrationService.getRegistrationById(8001L)).thenReturn(reg);
        when(orderService.getOrderById(4001L)).thenReturn(order);

        DispenseVO vo = pharmacyManager.dispense(1001L, pharmacistId);

        assertNotNull(vo);
        assertEquals(1001L, vo.getPrescriptionId());
        assertEquals(1, vo.getItems().size());
        assertEquals(5001L, vo.getItems().get(0).getDrugId());
        assertEquals(2, vo.getItems().get(0).getQuantity());
        assertEquals(PrescriptionStatus.DISPENSED.getCode(), vo.getPrescriptionStatus());
        assertNotNull(vo.getDispensedAt());
        // 库存扣减验证
        assertEquals(18, inv.getStockQuantity());
        assertEquals(3, inv.getLockedQuantity());
        verify(drugInventoryService).updateDrugInventoryById(inv);
        verify(inventoryTransactionService).insertInventoryTransaction(any(InventoryTransaction.class));
        verify(prescriptionService).updateById(rx);
        verify(statusLogManager).transition(eq(reg), anyInt(), eq(pharmacistId), eq("pharmacist"), eq("发药完成"));
        verify(orderService).updateOrderById(order);
        verify(orderStatusLogManager).addOrderStatusLog(any(OrderStatusLog.class));
    }

    @Test
    void dispense_notFound_throwsException() {
        when(prescriptionService.getById(1001L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> pharmacyManager.dispense(1001L, 3001L));
        assertEquals(BizErrorCode.PRESCRIPTION_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void dispense_notPaid_throwsException() {
        Prescription rx = new Prescription();
        rx.setId(1001L);
        rx.setStatus(PrescriptionStatus.PENDING_PAYMENT.getCode());

        when(prescriptionService.getById(1001L)).thenReturn(rx);

        BizException ex = assertThrows(BizException.class,
                () -> pharmacyManager.dispense(1001L, 3001L));
        assertEquals(BizErrorCode.PRESCRIPTION_NOT_PAID.getCode(), ex.getCode());
    }
}
