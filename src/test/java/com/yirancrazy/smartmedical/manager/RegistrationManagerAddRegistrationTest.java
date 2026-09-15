package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.ProductionTypeConstant;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorPositionService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.OrderItemService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.OrderTypeService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RegistrationManager#addRegistration 单测
 * 覆盖挂号主链路、重复挂号、依赖缺失、价格兜底与唯一键并发兜底。
 */
@ExtendWith(MockitoExtension.class)
class RegistrationManagerAddRegistrationTest {

    private static final Long SCHEDULE_ID = 1L;
    private static final Long PATIENT_CARD_ID = 2L;
    private static final Long REQUEST_USER_ID = 3L;
    private static final Long PATIENT_USER_ID = 7L;
    private static final Long TEMPLATE_ID = 4L;

    @Mock private RegistrationService registrationService;
    @Mock private RedisUtil redisUtil;
    @Mock private PatientCardService patientCardService;
    @Mock private PatientService patientService;
    @Mock private UserPatientRelationService userPatientRelationService;
    @Mock private OrderService orderService;
    @Mock private OrderTypeService orderTypeService;
    @Mock private OrderItemService orderItemService;
    @Mock private RegistrationScheduleService registrationScheduleService;
    @Mock private RegistrationScheduleTemplateService registrationScheduleTemplateService;
    @Mock private DoctorService doctorService;
    @Mock private UserService userService;
    @Mock private DepartmentService departmentService;
    @Mock private DoctorPositionService doctorPositionService;
    @Mock private RegistrationStatusLogService registrationStatusLogService;

    @InjectMocks
    private RegistrationManager registrationManager;

    @Test
    void addRegistration_happyPath_persistsOrderRegistrationAndStatusLog() {
        stubRegistrationPrerequisites();
        when(registrationScheduleService.deductRemainingQuota(SCHEDULE_ID)).thenReturn(1);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(TEMPLATE_ID))
                .thenReturn(templateWithPrice(120));

        Result<String> result = registrationManager.addRegistration(
                SCHEDULE_ID, REQUEST_USER_ID, PATIENT_CARD_ID);

        assertEquals(200, result.getCode());

        ArgumentCaptor<Registration> registrationCaptor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationService).insertRegistration(registrationCaptor.capture());
        Registration registration = registrationCaptor.getValue();
        assertEquals(PATIENT_USER_ID, registration.getUserId());
        assertEquals(SCHEDULE_ID, registration.getRegistrationScheduleId());
        assertEquals(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(), registration.getStatus());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderService).insertOrder(orderCaptor.capture());
        Order order = orderCaptor.getValue();
        assertEquals(registration.getOrderId(), order.getId());
        assertEquals(PATIENT_USER_ID, order.getUserId());
        assertEquals(OrderStatus.WAITING_FOR_PAYMENT.getCode(), order.getStatus());
        assertEquals(120, order.getTotalAmount());
        assertEquals(String.valueOf(order.getId()), result.getData());

        ArgumentCaptor<OrderItem> orderItemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemService).insertOrderItem(orderItemCaptor.capture());
        OrderItem orderItem = orderItemCaptor.getValue();
        assertEquals(order.getId(), orderItem.getOrderId());
        assertEquals(registration.getId(), orderItem.getProductionId());
        assertEquals(ProductionTypeConstant.DRUG, orderItem.getProductionTypeId());
        assertEquals(1, orderItem.getQuantity());

        verify(registrationStatusLogService).writeLog(
                eq(registration.getId()),
                isNull(),
                eq(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode()),
                eq(REQUEST_USER_ID),
                eq("user"),
                eq("挂号创建"));
    }

    @Test
    void addRegistration_patientCardMissing_returnsFailWithoutDownstreamWrites() {
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(null);

        Result<String> result = registrationManager.addRegistration(
                SCHEDULE_ID, REQUEST_USER_ID, PATIENT_CARD_ID);

        assertEquals(500, result.getCode());
        assertEquals("患者卡不存在", result.getMessage());
        verify(patientService, never()).getPatientByPatientCardId(any());
        verify(registrationScheduleService, never()).getRegistrationScheduleById(any());
        verify(registrationService, never()).insertRegistration(any());
    }

    @Test
    void addRegistration_patientMissing_returnsFailWithoutReservingQuota() {
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(new PatientCard());
        when(patientService.getPatientByPatientCardId(PATIENT_CARD_ID)).thenReturn(null);

        Result<String> result = registrationManager.addRegistration(
                SCHEDULE_ID, REQUEST_USER_ID, PATIENT_CARD_ID);

        assertEquals(500, result.getCode());
        assertEquals("患者信息不存在", result.getMessage());
        verify(registrationScheduleService, never()).getRegistrationScheduleById(any());
        verify(registrationScheduleService, never()).deductRemainingQuota(any());
    }

    @Test
    void addRegistration_scheduleMissing_returnsFailWithoutReservingQuota() {
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(new PatientCard());
        when(patientService.getPatientByPatientCardId(PATIENT_CARD_ID)).thenReturn(patient());
        when(registrationScheduleService.getRegistrationScheduleById(SCHEDULE_ID)).thenReturn(null);

        Result<String> result = registrationManager.addRegistration(
                SCHEDULE_ID, REQUEST_USER_ID, PATIENT_CARD_ID);

        assertEquals(500, result.getCode());
        assertEquals("排班信息不存在", result.getMessage());
        verify(registrationScheduleService, never()).deductRemainingQuota(any());
    }

    @Test
    void addRegistration_duplicateForPatient_returnsFailBeforeReservingQuota() {
        stubPatientAndSchedule();
        when(registrationService.getRegistrationByRegistrationScheduleIdAndUserId(
                SCHEDULE_ID, PATIENT_USER_ID)).thenReturn(new Registration());

        Result<String> result = registrationManager.addRegistration(
                SCHEDULE_ID, REQUEST_USER_ID, PATIENT_CARD_ID);

        assertEquals(500, result.getCode());
        assertEquals("该就诊人已挂号此排班", result.getMessage());
        verify(registrationScheduleService, never()).deductRemainingQuota(SCHEDULE_ID);
        verify(orderService, never()).insertOrder(any());
    }

    @Test
    void addRegistration_templateMissing_usesZeroPrice() {
        stubRegistrationPrerequisites();
        when(registrationScheduleService.deductRemainingQuota(SCHEDULE_ID)).thenReturn(1);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(TEMPLATE_ID))
                .thenReturn(null);

        Result<String> result = registrationManager.addRegistration(
                SCHEDULE_ID, REQUEST_USER_ID, PATIENT_CARD_ID);

        assertEquals(200, result.getCode());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderService).insertOrder(orderCaptor.capture());
        assertEquals(0, orderCaptor.getValue().getTotalAmount());
    }

    @Test
    void addRegistration_duplicateKeyRace_throwsBizExceptionAndStopsOrderCreation() {
        stubRegistrationPrerequisites();
        when(registrationScheduleService.deductRemainingQuota(SCHEDULE_ID)).thenReturn(1);
        when(registrationService.insertRegistration(any()))
                .thenThrow(new DuplicateKeyException("uk_reg_schedule_user"));

        BizException exception = assertThrows(BizException.class, () ->
                registrationManager.addRegistration(SCHEDULE_ID, REQUEST_USER_ID, PATIENT_CARD_ID));

        assertEquals(BizErrorCode.REGISTRATION_ALREADY_EXISTS.getCode(), exception.getCode());
        verify(registrationStatusLogService, never()).writeLog(any(), any(), any(), any(), any(), any());
        verify(orderService, never()).insertOrder(any());
        verify(orderItemService, never()).insertOrderItem(any());
    }

    private void stubRegistrationPrerequisites() {
        stubPatientAndSchedule();
        when(registrationService.getRegistrationByRegistrationScheduleIdAndUserId(
                SCHEDULE_ID, PATIENT_USER_ID)).thenReturn(null);
    }

    private void stubPatientAndSchedule() {
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(new PatientCard());
        when(patientService.getPatientByPatientCardId(PATIENT_CARD_ID)).thenReturn(patient());
        when(registrationScheduleService.getRegistrationScheduleById(SCHEDULE_ID)).thenReturn(schedule());
    }

    private Patient patient() {
        Patient patient = new Patient();
        patient.setUserId(PATIENT_USER_ID);
        return patient;
    }

    private RegistrationSchedule schedule() {
        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(SCHEDULE_ID);
        schedule.setRegistrationScheduleTemplateId(TEMPLATE_ID);
        return schedule;
    }

    private RegistrationScheduleTemplate templateWithPrice(int price) {
        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(TEMPLATE_ID);
        template.setPrice(price);
        return template;
    }
}
