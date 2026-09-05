package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.DoctorPosition;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AppointmentResponseSimple;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
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
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * RegistrationManager#getRegistrationByUid 单测
 * 覆盖：空列表、单条 happy path、template 缺失跳过（C6/C14 null-safe 路径）。
 */
@ExtendWith(MockitoExtension.class)
class RegistrationManagerTest {

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

    @InjectMocks
    private RegistrationManager registrationManager;

    @BeforeEach
    void setUp() {
        when(userPatientRelationService.getAccessiblePatientUserIds(7L, null)).thenReturn(List.of(7L));
    }

    @Test
    void getRegistrationByUid_emptyList_returnsEmptyResult() {
        when(registrationService.listRegistrationsByUserIds(List.of(7L))).thenReturn(Collections.emptyList());

        Result<PageResult<AppointmentResponseSimple>> result = registrationManager.getRegistrationByUid(7L, null, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getList().isEmpty());
    }

    @Test
    void getRegistrationByUid_missingTemplate_isSkipped() {
        Registration registration = new Registration();
        registration.setId(101L);
        registration.setUserId(7L);
        registration.setRegistrationScheduleId(99L);
        registration.setStatus(1);

        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(99L);
        schedule.setRegistrationScheduleTemplateId(88L);

        User user = new User();
        user.setId(7L);

        when(registrationService.listRegistrationsByUserIds(List.of(7L))).thenReturn(List.of(registration));
        when(userService.listUsersByUserIds(List.of(7L))).thenReturn(List.of(user));
        when(registrationScheduleService.listRegistrationSchedulesByIds(List.of(99L))).thenReturn(List.of(schedule));
        when(registrationScheduleTemplateService.listAllRegistrationScheduleTemplateByIdList(List.of(88L))).thenReturn(Collections.emptyList());

        Result<PageResult<AppointmentResponseSimple>> result = registrationManager.getRegistrationByUid(7L, null, null, null);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().getList().isEmpty(), "registration with missing template should be skipped, not NPE");
    }

    @Test
    void getRegistrationByUid_happyPath_populatesDoctorAndDepartment() {
        Registration registration = new Registration();
        registration.setId(101L);
        registration.setUserId(7L);
        registration.setRegistrationScheduleId(99L);
        registration.setStatus(1);

        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(99L);
        schedule.setRegistrationScheduleTemplateId(88L);

        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(88L);
        template.setDoctorId(55L);

        Doctor doctor = new Doctor();
        doctor.setId(55L);
        doctor.setName("张三");
        doctor.setAvatar("a.jpg");
        doctor.setDepartmentId(11L);
        doctor.setDoctorPositionId(22L);

        Department department = new Department();
        department.setId(11L);
        department.setName("内科");

        DoctorPosition position = new DoctorPosition();
        position.setId(22L);
        position.setName("主任医师");

        User user = new User();
        user.setId(7L);
        user.setNickname("李四");

        when(registrationService.listRegistrationsByUserIds(List.of(7L))).thenReturn(List.of(registration));
        when(userService.listUsersByUserIds(List.of(7L))).thenReturn(List.of(user));
        when(registrationScheduleService.listRegistrationSchedulesByIds(List.of(99L))).thenReturn(List.of(schedule));
        when(registrationScheduleTemplateService.listAllRegistrationScheduleTemplateByIdList(List.of(88L))).thenReturn(List.of(template));
        when(doctorService.listDoctorsByIds(List.of(55L))).thenReturn(List.of(doctor));
        when(departmentService.listDepartmentsByIds(List.of(11L))).thenReturn(List.of(department));
        when(doctorPositionService.listPositionsByIds(List.of(22L))).thenReturn(List.of(position));

        Result<PageResult<AppointmentResponseSimple>> result = registrationManager.getRegistrationByUid(7L, null, null, null);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getList().size());
        AppointmentResponseSimple item = result.getData().getList().get(0);
        assertEquals("101", item.getId());
        assertEquals("55", item.getDoctorId());
        assertEquals("张三", item.getDoctorName());
        assertEquals("李四", item.getPatientName());
        assertEquals("内科", item.getDepartmentName());
        assertEquals("主任医师", item.getDoctorPosition());
    }

    @Test
    void getRegistrationByUid_nullTemplateId_isSkipped() {
        // ponytail: 防御性测试 — 直接构造字段为 null 的 Registration，走 C14 同款 null-safe 分支
        Registration registration = new Registration();
        registration.setId(102L);
        registration.setUserId(7L);
        // registrationScheduleTemplateId 留空

        User user = new User();
        user.setId(7L);

        when(registrationService.listRegistrationsByUserIds(List.of(7L))).thenReturn(List.of(registration));
        when(userService.listUsersByUserIds(List.of(7L))).thenReturn(List.of(user));
        when(registrationScheduleService.listRegistrationSchedulesByIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        Result<PageResult<AppointmentResponseSimple>> result = registrationManager.getRegistrationByUid(7L, null, null, null);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().getList().isEmpty());
    }
}