package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.DraftMedicalRecordRequest;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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
    @Mock private PatientService patientService;
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

        Patient patient = new Patient();
        patient.setId(3001L);
        patient.setName("张三");
        patient.setPhone("13800138000");
        when(patientService.getPatientById(3001L)).thenReturn(patient);
        Doctor doctor = new Doctor();
        doctor.setName("李医生");
        when(doctorService.getDoctorById(1001L)).thenReturn(doctor);

        MedicalRecordDetailVO vo = medicalRecordManager.getMedicalRecordDetailForAdmin(1L, 1001L);

        assertEquals(1001L, vo.getDoctorId());
        assertEquals("李医生", vo.getDoctorName());
        assertEquals("张三", vo.getPatientName());
        assertEquals("13800138000", vo.getPatientPhone());
    }

    @Test
    void draft_usesRegistrationPatientId() {
        Registration reg = new Registration();
        reg.setId(10L);
        reg.setPatientId(3001L);
        reg.setStatus(com.yirancrazy.smartmedical.constant.RegistrationStatusEnum.IN_TREATMENT.getCode());
        reg.setRegistrationScheduleId(20L);

        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(20L);
        schedule.setRegistrationScheduleTemplateId(30L);

        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(30L);
        template.setDoctorId(1001L);

        DraftMedicalRecordRequest request = new DraftMedicalRecordRequest();
        request.setRegistrationId(10L);

        when(registrationService.getRegistrationById(10L)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(20L)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(30L)).thenReturn(template);
        when(medicalRecordService.getByRegistrationId(10L)).thenReturn(null);

        medicalRecordManager.draft(request, 1001L);

        ArgumentCaptor<MedicalRecord> captor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(medicalRecordService).save(captor.capture());
        assertEquals(3001L, captor.getValue().getPatientId());
    }

    @Test
    void getMedicalRecordById_usesAccessiblePatientIdsAndPatientService() {
        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        record.setPatientId(3001L);
        record.setRegistrationId(10L);

        Patient patient = new Patient();
        patient.setId(3001L);
        patient.setName("张三");
        patient.setPhone("13800138000");

        when(medicalRecordService.getById(1L)).thenReturn(record);
        when(userPatientRelationService.listAccessiblePatientIds(99L)).thenReturn(List.of(3001L));
        when(patientService.getPatientById(3001L)).thenReturn(patient);
        when(prescriptionService.getByMedicalRecordId(1L)).thenReturn(null);

        var vo = medicalRecordManager.getMedicalRecordById(1L, 99L);

        assertEquals("张三", vo.getPatientName());
        assertEquals("13800138000", vo.getPatientPhone());
        verify(userPatientRelationService).listAccessiblePatientIds(99L);
        verify(patientService).getPatientById(3001L);
    }
}
