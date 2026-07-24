package com.yirancrazy.smartmedical.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.DrugInventoryMapper;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
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
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import com.yirancrazy.smartmedical.service.UserService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
        user.setUsername("13800138000");

        when(medicalRecordService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(record));
        when(prescriptionService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(rx));
        when(userService.listUsersByUserIds(List.of(3001L))).thenReturn(List.of(user));
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
        user.setUsername("13800138000");

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
        when(prescriptionItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        when(drugService.getDrugById(6001L)).thenReturn(drug);

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
}
