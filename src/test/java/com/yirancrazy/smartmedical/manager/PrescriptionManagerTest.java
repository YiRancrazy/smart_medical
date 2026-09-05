package com.yirancrazy.smartmedical.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.DrugInventoryMapper;
import com.yirancrazy.smartmedical.mapper.PaymentRecordMapper;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorPrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorPrescriptionListVO;
import com.yirancrazy.smartmedical.service.DrugService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.OrderItemService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.pojo.PaymentRecord;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.PrescriptionItemRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.SubmitPrescriptionRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.PrescriptionSubmitVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PrescriptionManager 医生端查询单测
 * 覆盖：医生端处方列表/详情查询及所有权校验。
 * @Author: YiRanCrazy@gmail.com
 * @Description: PrescriptionManager 单测
 * @Datetime: 2026-07-24 19:10
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PrescriptionManagerTest {

    @Mock private PrescriptionService prescriptionService;
    @Mock private PrescriptionItemService prescriptionItemService;
    @Mock private MedicalRecordService medicalRecordService;
    @Mock private DrugService drugService;
    @Mock private DrugInventoryMapper drugInventoryMapper;
    @Mock private PaymentRecordMapper paymentRecordMapper;
    @Mock private OrderService orderService;
    @Mock private OrderItemService orderItemService;
    @Mock private InventoryTransactionService inventoryTransactionService;
    @Mock private RegistrationService registrationService;
    @Mock private RegistrationScheduleService registrationScheduleService;
    @Mock private RegistrationScheduleTemplateService registrationScheduleTemplateService;
    @Mock private RegistrationStatusLogService registrationStatusLogService;
    @Mock private RegistrationStatusLogManager statusLogManager;
    @Mock private OrderStatusLogManager orderStatusLogManager;
    @Mock private PatientManager patientManager;
    @Mock private UserService userService;
    @Mock private AccountService accountService;

    @InjectMocks
    private PrescriptionManager prescriptionManager;

    /**
     * listDoctorPrescriptions happy path：返回该医生的处方列表，患者信息正确映射
     */
    @Test
    @SuppressWarnings("unchecked")
    void listDoctorPrescriptions_happyPath_mapsVoCorrectly() {
        MedicalRecord record = new MedicalRecord();
        record.setId(1001L);
        record.setDoctorId(2001L);
        record.setPatientId(3001L);

        Prescription rx = new Prescription();
        rx.setId(4001L);
        rx.setMedicalRecordId(1001L);
        rx.setTotalAmount(15000);
        rx.setStatus(PrescriptionStatus.PENDING_PAYMENT.getCode());
        rx.setCreateTime(LocalDateTime.now());

        User user = new User();
        user.setId(3001L);
        user.setNickname("张三");

        Account account = new Account();
        account.setUserId(3001L);
        account.setPhone("13800138000");

        when(medicalRecordService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(record));
        when(prescriptionService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(rx));
        when(userService.listUsersByUserIds(List.of(3001L))).thenReturn(List.of(user));
        when(accountService.getAccountByUserId(3001L)).thenReturn(account);
        PrescriptionItem item = new PrescriptionItem();
        item.setPrescriptionId(4001L);
        when(prescriptionItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));

        List<DoctorPrescriptionListVO> result = prescriptionManager.listDoctorPrescriptions(2001L);

        assertEquals(1, result.size());
        DoctorPrescriptionListVO vo = result.get(0);
        assertEquals(4001L, vo.getId());
        assertEquals(1001L, vo.getMedicalRecordId());
        assertEquals(3001L, vo.getPatientId());
        assertEquals("张三", vo.getPatientName());
        assertEquals("13800138000", vo.getPatientPhone());
        assertEquals(15000, vo.getTotalAmount());
        assertEquals(1, vo.getItemCount());
    }

    /**
     * listDoctorPrescriptions doctorId 为 null：返回空列表
     */
    @Test
    void listDoctorPrescriptions_nullDoctorId_returnsEmpty() {
        List<DoctorPrescriptionListVO> result = prescriptionManager.listDoctorPrescriptions(null);
        assertTrue(result.isEmpty());
    }

    /**
     * listDoctorPrescriptions 该医生无病历：返回空列表
     */
    @Test
    @SuppressWarnings("unchecked")
    void listDoctorPrescriptions_noRecords_returnsEmpty() {
        when(medicalRecordService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<DoctorPrescriptionListVO> result = prescriptionManager.listDoctorPrescriptions(2001L);

        assertTrue(result.isEmpty());
    }

    /**
     * listDoctorPrescriptions 有病历但无处方：返回空列表
     */
    @Test
    @SuppressWarnings("unchecked")
    void listDoctorPrescriptions_noPrescriptions_returnsEmpty() {
        MedicalRecord record = new MedicalRecord();
        record.setId(1001L);
        record.setDoctorId(2001L);

        when(medicalRecordService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(record));
        when(prescriptionService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<DoctorPrescriptionListVO> result = prescriptionManager.listDoctorPrescriptions(2001L);

        assertTrue(result.isEmpty());
    }

    /**
     * getDoctorPrescriptionDetail happy path：返回详情，药品信息正确映射
     */
    @Test
    @SuppressWarnings("unchecked")
    void getDoctorPrescriptionDetail_happyPath_mapsVoCorrectly() {
        Prescription rx = new Prescription();
        rx.setId(4001L);
        rx.setMedicalRecordId(1001L);
        rx.setOrderId(5001L);
        rx.setTotalAmount(15000);
        rx.setStatus(PrescriptionStatus.PENDING_PAYMENT.getCode());
        rx.setCreateTime(LocalDateTime.now());

        MedicalRecord record = new MedicalRecord();
        record.setId(1001L);
        record.setDoctorId(2001L);
        record.setPatientId(3001L);

        User user = new User();
        user.setId(3001L);
        user.setNickname("张三");

        Account account = new Account();
        account.setUserId(3001L);
        account.setPhone("13800138000");

        PrescriptionItem item = new PrescriptionItem();
        item.setDrugId(6001L);
        item.setQuantity(2);
        item.setUnitPrice(7500);
        item.setUsageMethod("每日一次");

        Drug drug = new Drug();
        drug.setId(6001L);
        drug.setCommonName("阿莫西林");
        drug.setSpecification("0.25g*24粒");
        drug.setUnit("盒");

        when(prescriptionService.getById(4001L)).thenReturn(rx);
        when(medicalRecordService.getById(1001L)).thenReturn(record);
        when(userService.getUserById(3001L)).thenReturn(user);
        when(accountService.getAccountByUserId(3001L)).thenReturn(account);
        when(prescriptionItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        when(drugService.listDrugsByIds(List.of(6001L))).thenReturn(List.of(drug));

        DoctorPrescriptionDetailVO vo = prescriptionManager.getDoctorPrescriptionDetail(4001L, 2001L);

        assertNotNull(vo);
        assertEquals(4001L, vo.getId());
        assertEquals(1001L, vo.getMedicalRecordId());
        assertEquals(3001L, vo.getPatientId());
        assertEquals("张三", vo.getPatientName());
        assertEquals("13800138000", vo.getPatientPhone());
        assertEquals(5001L, vo.getOrderId());
        assertEquals(1, vo.getItems().size());
        DoctorPrescriptionDetailVO.PrescriptionItemVO itemVO = vo.getItems().get(0);
        assertEquals(6001L, itemVO.getDrugId());
        assertEquals("阿莫西林", itemVO.getCommonName());
        assertEquals("0.25g*24粒", itemVO.getSpecification());
        assertEquals("盒", itemVO.getUnit());
        assertEquals(2, itemVO.getQuantity());
        assertEquals(7500, itemVO.getUnitPrice());
        assertEquals("每日一次", itemVO.getUsageMethod());
    }

    /**
     * getDoctorPrescriptionDetail 处方不存在：抛 PRESCRIPTION_NOT_FOUND
     */
    @Test
    void getDoctorPrescriptionDetail_notFound_throwsException() {
        when(prescriptionService.getById(4001L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> prescriptionManager.getDoctorPrescriptionDetail(4001L, 2001L));
        assertEquals(BizErrorCode.PRESCRIPTION_NOT_FOUND.getCode(), ex.getCode());
    }

    /**
     * getDoctorPrescriptionDetail 非当前医生处方：抛 PRESCRIPTION_NOT_OWNED
     */
    @Test
    void getDoctorPrescriptionDetail_notOwned_throwsException() {
        Prescription rx = new Prescription();
        rx.setId(4001L);
        rx.setMedicalRecordId(1001L);

        MedicalRecord record = new MedicalRecord();
        record.setId(1001L);
        record.setDoctorId(9999L);

        when(prescriptionService.getById(4001L)).thenReturn(rx);
        when(medicalRecordService.getById(1001L)).thenReturn(record);

        BizException ex = assertThrows(BizException.class,
                () -> prescriptionManager.getDoctorPrescriptionDetail(4001L, 2001L));
        assertEquals(BizErrorCode.PRESCRIPTION_NOT_OWNED.getCode(), ex.getCode());
    }

    // ===== submit() 测试 =====

    @Test
    @SuppressWarnings("unchecked")
    void submit_happyPathWithItems_createsPrescriptionAndOrder() {
        Long doctorId = 2001L;
        Registration reg = new Registration();
        reg.setId(1001L);
        reg.setUserId(3001L);
        reg.setStatus(RegistrationStatusEnum.IN_TREATMENT.getCode());
        reg.setRegistrationScheduleId(5001L);

        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(5001L);
        schedule.setRegistrationScheduleTemplateId(6001L);

        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(6001L);
        template.setDoctorId(2001L);

        Drug drug = new Drug();
        drug.setId(7001L);
        drug.setCommonName("阿莫西林");
        drug.setPrice(1000);

        DrugInventory inv = new DrugInventory();
        inv.setDrugId(7001L);
        inv.setId(8001L);
        inv.setWarehouseId(9001L);
        inv.setAvailableQuantity(10);
        inv.setLockedQuantity(0);

        PrescriptionItemRequest itemReq = new PrescriptionItemRequest();
        itemReq.setDrugId(7001L);
        itemReq.setQuantity(2);
        itemReq.setUsageMethod("每日一次");

        SubmitPrescriptionRequest req = new SubmitPrescriptionRequest();
        req.setItems(List.of(itemReq));
        req.setChiefComplaint("头痛");
        req.setDiagnosis("感冒");

        when(registrationService.getRegistrationById(1001L)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(5001L)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(6001L)).thenReturn(template);
        when(drugService.listDrugsByIds(List.of(7001L))).thenReturn(List.of(drug));
        when(drugInventoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(inv));
        when(drugInventoryMapper.update(eq(null), any())).thenReturn(1);
        when(medicalRecordService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        doAnswer(answer -> {
            MedicalRecord r = answer.getArgument(0);
            r.setId(1001L);
            return true;
        }).when(medicalRecordService).save(any(MedicalRecord.class));

        PrescriptionSubmitVO vo = prescriptionManager.submit(1001L, req, doctorId);

        assertNotNull(vo);
        assertNotNull(vo.getMedicalRecordId());
        assertNotNull(vo.getPrescriptionId());
        assertNotNull(vo.getOrderId());
        assertEquals(2000, vo.getTotalAmount());
        assertEquals(RegistrationStatusEnum.COMPLETED.getCode(), vo.getRegistrationStatus());
        verify(medicalRecordService).save(any(MedicalRecord.class));
        verify(prescriptionService).save(any(Prescription.class));
        verify(orderService).insertOrder(any(Order.class));
        verify(prescriptionItemService).save(any(PrescriptionItem.class));
        verify(orderItemService).insertOrderItem(any(OrderItem.class));
        verify(inventoryTransactionService).insertInventoryTransaction(any(InventoryTransaction.class));
        verify(statusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.COMPLETED.getCode()),
                eq(doctorId), eq("doctor"), eq("提交病历开方"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void submit_noItems_returnsDirectlyWithoutPrescription() {
        Long doctorId = 2001L;
        Registration reg = new Registration();
        reg.setId(1001L);
        reg.setUserId(3001L);
        reg.setStatus(RegistrationStatusEnum.IN_TREATMENT.getCode());
        reg.setRegistrationScheduleId(5001L);

        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(5001L);
        schedule.setRegistrationScheduleTemplateId(6001L);

        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(6001L);
        template.setDoctorId(2001L);

        SubmitPrescriptionRequest req = new SubmitPrescriptionRequest();
        req.setItems(null);
        req.setChiefComplaint("头痛");
        req.setDiagnosis("感冒");

        when(registrationService.getRegistrationById(1001L)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(5001L)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(6001L)).thenReturn(template);
        when(medicalRecordService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        PrescriptionSubmitVO vo = prescriptionManager.submit(1001L, req, doctorId);

        assertNotNull(vo);
        assertNull(vo.getPrescriptionId());
        assertNull(vo.getOrderId());
        assertEquals(0, vo.getTotalAmount());
        assertEquals(RegistrationStatusEnum.COMPLETED.getCode(), vo.getRegistrationStatus());
        verify(medicalRecordService).save(any(MedicalRecord.class));
        verify(prescriptionService, never()).save(any());
        verify(orderService, never()).insertOrder(any());
        verify(statusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.COMPLETED.getCode()),
                eq(doctorId), eq("doctor"), eq("就诊完成(无处方)"));
    }

    @Test
    void submit_registrationNotFound_throwsException() {
        when(registrationService.getRegistrationById(1001L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> prescriptionManager.submit(1001L, new SubmitPrescriptionRequest(), 2001L));
        assertEquals(BizErrorCode.REGISTRATION_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void submit_doctorNotMatch_throwsException() {
        Registration reg = new Registration();
        reg.setId(1001L);
        reg.setStatus(RegistrationStatusEnum.IN_TREATMENT.getCode());
        reg.setRegistrationScheduleId(5001L);

        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(5001L);
        schedule.setRegistrationScheduleTemplateId(6001L);

        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(6001L);
        template.setDoctorId(9999L);

        when(registrationService.getRegistrationById(1001L)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(5001L)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(6001L)).thenReturn(template);

        BizException ex = assertThrows(BizException.class,
                () -> prescriptionManager.submit(1001L, new SubmitPrescriptionRequest(), 2001L));
        assertEquals(BizErrorCode.DOCTOR_NOT_MATCH.getCode(), ex.getCode());
    }

    @Test
    void submit_wrongStatus_throwsException() {
        Registration reg = new Registration();
        reg.setId(1001L);
        reg.setStatus(RegistrationStatusEnum.SUCCESS.getCode());
        reg.setRegistrationScheduleId(5001L);

        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(5001L);
        schedule.setRegistrationScheduleTemplateId(6001L);

        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(6001L);
        template.setDoctorId(2001L);

        when(registrationService.getRegistrationById(1001L)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(5001L)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(6001L)).thenReturn(template);

        BizException ex = assertThrows(BizException.class,
                () -> prescriptionManager.submit(1001L, new SubmitPrescriptionRequest(), 2001L));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void refund_paidPrescription_releasesInventoryAndRefundsOrder() {
        Long userId = 3001L;
        Prescription rx = new Prescription();
        rx.setId(4001L);
        rx.setMedicalRecordId(1001L);
        rx.setOrderId(5001L);
        rx.setStatus(PrescriptionStatus.PAID.getCode());

        MedicalRecord record = new MedicalRecord();
        record.setId(1001L);
        record.setPatientId(9009L);

        PrescriptionItem item = new PrescriptionItem();
        item.setDrugId(7001L);
        item.setQuantity(2);

        DrugInventory inv = new DrugInventory();
        inv.setId(8001L);
        inv.setDrugId(7001L);
        inv.setWarehouseId(9001L);
        inv.setAvailableQuantity(10);
        inv.setLockedQuantity(2);

        Order order = new Order();
        order.setId(5001L);
        order.setStatus(OrderStatus.PAID.getCode());
        order.setTotalAmount(2000);

        PaymentRecord orig = new PaymentRecord();
        orig.setPaymentMethodId(4);

        when(prescriptionService.getById(4001L)).thenReturn(rx);
        when(medicalRecordService.getById(1001L)).thenReturn(record);
        when(patientManager.getAccessiblePatientUserIds(userId, null)).thenReturn(List.of(9009L));
        when(prescriptionItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        when(drugInventoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(inv));
        when(drugInventoryMapper.update(eq(null), any())).thenReturn(1);
        when(orderService.getOrderById(5001L)).thenReturn(order);
        when(paymentRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(orig);
        when(prescriptionService.update(any(UpdateWrapper.class))).thenReturn(true);

        prescriptionManager.refund(4001L, userId);

        verify(paymentRecordMapper).insert(any(PaymentRecord.class));
        assertEquals(4, orig.getStatus());
        verify(orderService).updateOrderById(order);
        assertEquals(OrderStatus.REFUNDED.getCode(), order.getStatus());
        verify(prescriptionService).update(any(UpdateWrapper.class));
        verify(orderStatusLogManager).addOrderStatusLog(any());
        verify(inventoryTransactionService).insertInventoryTransaction(any(InventoryTransaction.class));
    }

    @Test
    void refund_nonOwnedPrescription_throwsException() {
        Long userId = 3001L;
        Prescription rx = new Prescription();
        rx.setId(4001L);
        rx.setMedicalRecordId(1001L);
        rx.setStatus(PrescriptionStatus.PAID.getCode());

        MedicalRecord record = new MedicalRecord();
        record.setId(1001L);
        record.setPatientId(8888L);

        when(prescriptionService.getById(4001L)).thenReturn(rx);
        when(medicalRecordService.getById(1001L)).thenReturn(record);
        when(patientManager.getAccessiblePatientUserIds(userId, null)).thenReturn(List.of(9009L));

        BizException ex = assertThrows(BizException.class, () -> prescriptionManager.refund(4001L, userId));
        assertEquals(BizErrorCode.PRESCRIPTION_NOT_OWNED.getCode(), ex.getCode());
    }

    @Test
    void refund_dispensedPrescription_rejected() {
        Long userId = 3001L;
        Prescription rx = new Prescription();
        rx.setId(4001L);
        rx.setMedicalRecordId(1001L);
        rx.setStatus(PrescriptionStatus.DISPENSED.getCode());

        MedicalRecord record = new MedicalRecord();
        record.setId(1001L);
        record.setPatientId(9009L);

        when(prescriptionService.getById(4001L)).thenReturn(rx);
        when(medicalRecordService.getById(1001L)).thenReturn(record);
        when(patientManager.getAccessiblePatientUserIds(userId, null)).thenReturn(List.of(9009L));

        BizException ex = assertThrows(BizException.class, () -> prescriptionManager.refund(4001L, userId));
        assertEquals(BizErrorCode.PRESCRIPTION_ALREADY_DISPENSED.getCode(), ex.getCode());
    }
}
