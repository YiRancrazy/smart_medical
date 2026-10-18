package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordDetailVO;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * MedicalRecordManager 病历详情归属校验单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 覆盖 doctor 查详情时的归属校验与 admin 无约束
 * @Datetime: 2026-09-05 12:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class MedicalRecordManagerTest {

    @Mock private MedicalRecordService medicalRecordService;
    @Mock private RegistrationService registrationService;
    @Mock private RegistrationScheduleService registrationScheduleService;
    @Mock private RegistrationScheduleTemplateService registrationScheduleTemplateService;
    @Mock private UserService userService;
    @Mock private AccountService accountService;
    @Mock private DoctorService doctorService;
    @Mock private DepartmentService departmentService;
    @Mock private PrescriptionService prescriptionService;
    @Mock private UserPatientRelationService userPatientRelationService;

    @InjectMocks
    private MedicalRecordManager medicalRecordManager;

    @Test
    void getMedicalRecordDetailForAdmin_notFound_throws() {
        when(medicalRecordService.getById(1L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class,
                () -> medicalRecordManager.getMedicalRecordDetailForAdmin(1L, 2001L));
        assertEquals(BizErrorCode.MEDICAL_RECORD_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void getMedicalRecordDetailForAdmin_unownedDoctor_throws() {
        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        record.setDoctorId(1001L);
        when(medicalRecordService.getById(1L)).thenReturn(record);

        BizException ex = assertThrows(BizException.class,
                () -> medicalRecordManager.getMedicalRecordDetailForAdmin(1L, 9999L));
        assertEquals(BizErrorCode.DOCTOR_NOT_MATCH.getCode(), ex.getCode());
    }

    @Test
    void getMedicalRecordDetailForAdmin_ownedDoctor_returnsVo() {
        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        record.setDoctorId(1001L);
        record.setPatientId(3001L);
        when(medicalRecordService.getById(1L)).thenReturn(record);

        User user = new User();
        user.setNickname("张三");
        when(userService.getUserById(3001L)).thenReturn(user);
        Account account = new Account();
        account.setPhone("13800138000");
        when(accountService.getAccountByUserId(3001L)).thenReturn(account);
        Doctor doctor = new Doctor();
        doctor.setName("李医生");
        when(doctorService.getDoctorById(1001L)).thenReturn(doctor);

        MedicalRecordDetailVO vo = medicalRecordManager.getMedicalRecordDetailForAdmin(1L, 1001L);

        assertEquals(1001L, vo.getDoctorId());
        assertEquals("李医生", vo.getDoctorName());
        assertEquals("张三", vo.getPatientName());
        assertEquals("13800138000", vo.getPatientPhone());
    }
}