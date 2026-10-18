package com.yirancrazy.smartmedical.manager;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.PrescriptionQueryRequest;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionPageItemVO;
import com.yirancrazy.smartmedical.pojo.dto.common.PageResult;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorPrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorPrescriptionListVO;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PrescriptionListVO;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.DrugService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.PageUtils;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处方查询业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端、医生端、管理端处方查询与详情组装
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class PrescriptionQueryManager {

    private final MedicalRecordService medicalRecordService;
    private final PrescriptionService prescriptionService;
    private final PrescriptionItemService prescriptionItemService;
    private final DrugService drugService;
    private final DoctorService doctorService;
    private final DepartmentService departmentService;
    private final UserPatientRelationService userPatientRelationService;
    private final UserService userService;
    private final AccountService accountService;

    /**
     * 医生端 - 处方列表（按当前医生过滤）
     * @param doctorId 医生ID
     * @return 处方列表 VO
     */
    public List<DoctorPrescriptionListVO> listDoctorPrescriptions(Long doctorId) {
        if (doctorId == null) {
            return Collections.emptyList();
        }
        List<MedicalRecord> records = medicalRecordService
                .listMedicalRecordsByDoctorIdAndPatientUserIds(doctorId, null);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, MedicalRecord> recordMap = records.stream()
                .collect(Collectors.toMap(MedicalRecord::getId, r -> r));
        List<Long> recordIds = records.stream()
                .map(MedicalRecord::getId)
                .collect(Collectors.toList());

        List<Prescription> prescriptions = prescriptionService.listByMedicalRecordIds(recordIds);
        if (prescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> patientIds = records.stream()
                .map(MedicalRecord::getPatientId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userService.listUsersByUserIds(patientIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Long> prescriptionIds = prescriptions.stream()
                .map(Prescription::getId)
                .collect(Collectors.toList());
        Map<Long, Long> itemCountMap = prescriptionItemService.listByPrescriptionIds(prescriptionIds)
                .stream()
                .collect(Collectors.groupingBy(PrescriptionItem::getPrescriptionId, Collectors.counting()));

        return prescriptions.stream().map(rx -> {
            DoctorPrescriptionListVO vo = new DoctorPrescriptionListVO();
            vo.setId(rx.getId());
            vo.setMedicalRecordId(rx.getMedicalRecordId());
            MedicalRecord record = recordMap.get(rx.getMedicalRecordId());
            if (record != null) {
                vo.setPatientId(record.getPatientId());
                User user = userMap.get(record.getPatientId());
                if (user != null) {
                    vo.setPatientName(user.getNickname());
                }
                Account account = accountService.getAccountByUserId(record.getPatientId());
                if (account != null) {
                    vo.setPatientPhone(account.getPhone());
                }
            }
            vo.setTotalAmount(rx.getTotalAmount());
            vo.setStatus(rx.getStatus());
            vo.setItemCount(itemCountMap.getOrDefault(rx.getId(), 0L).intValue());
            vo.setCreateTime(rx.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 医生端 - 处方详情（含医生所有权校验）
     * @param prescriptionId 处方ID
     * @param doctorId 当前医生ID
     * @return 处方详情 VO
     * @throws BizException PRESCRIPTION_NOT_FOUND / PRESCRIPTION_NOT_OWNED
     */
    public DoctorPrescriptionDetailVO getDoctorPrescriptionDetail(Long prescriptionId, Long doctorId) {
        Prescription rx = prescriptionService.getById(prescriptionId);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        MedicalRecord record = rx.getMedicalRecordId() == null
                ? null : medicalRecordService.getById(rx.getMedicalRecordId());
        if (record == null || !doctorId.equals(record.getDoctorId())) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_OWNED);
        }

        User user = record.getPatientId() == null
                ? null : userService.getUserById(record.getPatientId());

        List<PrescriptionItem> items = prescriptionItemService.listByPrescriptionId(prescriptionId);
        List<Long> drugIds = items.stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Drug> drugMap = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        DoctorPrescriptionDetailVO vo = new DoctorPrescriptionDetailVO();
        vo.setId(rx.getId());
        vo.setMedicalRecordId(rx.getMedicalRecordId());
        vo.setPatientId(record.getPatientId());
        if (user != null) {
            vo.setPatientName(user.getNickname());
        }
        Account account = record.getPatientId() == null
                ? null : accountService.getAccountByUserId(record.getPatientId());
        if (account != null) {
            vo.setPatientPhone(account.getPhone());
        }
        vo.setStatus(rx.getStatus());
        vo.setTotalAmount(rx.getTotalAmount());
        vo.setOrderId(rx.getOrderId());
        vo.setCreateTime(rx.getCreateTime());
        vo.setItems(items.stream().map(item -> {
            DoctorPrescriptionDetailVO.PrescriptionItemVO itemVO = new DoctorPrescriptionDetailVO.PrescriptionItemVO();
            itemVO.setDrugId(item.getDrugId());
            Drug drug = drugMap.get(item.getDrugId());
            if (drug != null) {
                itemVO.setCommonName(drug.getCommonName());
                itemVO.setSpecification(drug.getSpecification());
                itemVO.setUnit(drug.getUnit());
            }
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUsageMethod(item.getUsageMethod());
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 用户端 - 处方列表（按就诊人过滤）
     * ponytail: N+1 查询，用户处方列表 < 100，可接受
     * @param patientUserIds 可访问的用户ID列表
     * @return 处方列表 VO
     */
    public List<PrescriptionListVO> listUserPrescriptions(List<Long> patientUserIds) {
        if (patientUserIds == null || patientUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<MedicalRecord> records = medicalRecordService.listByPatientUserIds(patientUserIds);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> recordIds = records.stream()
                .map(MedicalRecord::getId)
                .collect(Collectors.toList());

        List<Prescription> prescriptions = prescriptionService.listByMedicalRecordIds(recordIds);
        if (prescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> prescriptionIds = prescriptions.stream()
                .map(Prescription::getId)
                .collect(Collectors.toList());
        Map<Long, Long> itemCountMap = prescriptionItemService.listByPrescriptionIds(prescriptionIds)
                .stream()
                .collect(Collectors.groupingBy(PrescriptionItem::getPrescriptionId, Collectors.counting()));

        return prescriptions.stream().map(rx -> {
            PrescriptionListVO vo = new PrescriptionListVO();
            vo.setId(rx.getId());
            vo.setMedicalRecordId(rx.getMedicalRecordId());
            vo.setTotalAmount(rx.getTotalAmount());
            vo.setStatus(rx.getStatus());
            vo.setItemCount(itemCountMap.getOrDefault(rx.getId(), 0L).intValue());
            vo.setCreateTime(rx.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 用户端 - 处方详情（含权限校验）
     * @param prescriptionId 处方ID
     * @param userId 当前用户ID
     * @return 处方详情 VO
     * @throws BizException PRESCRIPTION_NOT_FOUND / PRESCRIPTION_NOT_OWNED
     */
    public PrescriptionDetailVO getPrescriptionDetail(Long prescriptionId, Long userId) {
        Prescription rx = prescriptionService.getById(prescriptionId);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        // 通过 medicalRecord → patientId 校验所有权（复用可访问患者集合，含家属授权）
        MedicalRecord record = null;
        if (rx.getMedicalRecordId() != null) {
            record = medicalRecordService.getById(rx.getMedicalRecordId());
            if (record == null) {
                throw new BizException(BizErrorCode.PRESCRIPTION_NOT_OWNED);
            }
            List<Long> accessibleUserIds = userPatientRelationService.listAccessiblePatientUserIds(userId);
            if (!accessibleUserIds.contains(record.getPatientId())) {
                throw new BizException(BizErrorCode.PRESCRIPTION_NOT_OWNED);
            }
        }
        List<PrescriptionItem> items = prescriptionItemService.listByPrescriptionId(prescriptionId);
        List<Long> drugIds = items.stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Drug> drugMap = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        PrescriptionDetailVO vo = new PrescriptionDetailVO();
        vo.setId(rx.getId());
        vo.setMedicalRecordId(rx.getMedicalRecordId());
        vo.setStatus(rx.getStatus());
        vo.setTotalAmount(rx.getTotalAmount());
        vo.setOrderId(rx.getOrderId());
        vo.setCreateTime(rx.getCreateTime());
        if (record != null) {
            User user = record.getPatientId() == null
                    ? null : userService.getUserById(record.getPatientId());
            if (user != null) {
                vo.setPatientName(user.getNickname());
            }
            Doctor doctor = record.getDoctorId() == null
                    ? null : doctorService.getDoctorById(record.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
                if (doctor.getDepartmentId() != null) {
                    Department dept = departmentService.getDepartmentById(doctor.getDepartmentId());
                    if (dept != null) {
                        vo.setDepartmentName(dept.getName());
                    }
                }
            }
        }
        vo.setItems(items.stream().map(item -> {
            PrescriptionDetailVO.PrescriptionItemVO itemVO = new PrescriptionDetailVO.PrescriptionItemVO();
            itemVO.setDrugId(item.getDrugId());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUsageMethod(item.getUsageMethod());
            itemVO.setUnitPrice(item.getUnitPrice());
            Drug drug = drugMap.get(item.getDrugId());
            if (drug != null) {
                itemVO.setDrugName(drug.getCommonName());
                itemVO.setSpecification(drug.getSpecification());
                itemVO.setUnit(drug.getUnit());
            }
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 管理端/医生端 - 处方历史分页查询
     * @param request 查询条件
     * @param doctorId 医生ID；null 表示查询全部（管理员/药师），非null则按医生过滤
     * @return 处方分页列表
     */
    public PageResult<PrescriptionPageItemVO> pagePrescriptions(
            PrescriptionQueryRequest request, Long doctorId) {

        List<Long> allowedMedicalRecordIds = resolveAllowedMedicalRecordIds(doctorId, request.getPatientName());
        boolean restrictByMedicalRecord = allowedMedicalRecordIds != null;
        if (restrictByMedicalRecord && allowedMedicalRecordIds.isEmpty()) {
            return PageUtils.toResult(new PageInfo<>(), Collections.emptyList());
        }

        LocalDateTime createTimeStart = request.getStartDate() == null
                ? null : request.getStartDate().atStartOfDay();
        LocalDateTime createTimeEnd = request.getEndDate() == null
                ? null : request.getEndDate().atTime(LocalTime.MAX);
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        PageInfo<Prescription> prescriptionPage = prescriptionService
                .listPrescriptionsByMedicalRecordIdsAndStatusAndCreateTimePage(
                        allowedMedicalRecordIds, request.getStatus(), createTimeStart, createTimeEnd,
                        pageNum, pageSize);
        return PageUtils.toResult(prescriptionPage, toAdminPageItemVOs(prescriptionPage.getList()));
    }

    /**
     * 根据医生ID和患者姓名解析允许查询的病历ID集合
     * @param doctorId 医生ID
     * @param patientName 患者姓名
     * @return 允许的病历ID集合；null 表示无限制
     */
    private List<Long> resolveAllowedMedicalRecordIds(Long doctorId, String patientName) {
        List<Long> patientUserIds = null;
        if (patientName != null && !patientName.trim().isEmpty()) {
            patientUserIds = userService.listUserIdsByNicknameLike(patientName.trim());
            if (patientUserIds.isEmpty()) {
                return Collections.emptyList();
            }
        }

        if (doctorId == null && patientUserIds == null) {
            return null;
        }
        return medicalRecordService
                .listMedicalRecordsByDoctorIdAndPatientUserIds(doctorId, patientUserIds)
                .stream()
                .map(MedicalRecord::getId)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换处方实体为管理端列表 VO
     * @param prescriptions 处方列表
     * @return 管理端列表 VO
     */
    private List<PrescriptionPageItemVO> toAdminPageItemVOs(List<Prescription> prescriptions) {
        if (prescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> medicalRecordIds = prescriptions.stream()
                .map(Prescription::getMedicalRecordId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MedicalRecord> recordMap = medicalRecordService.listByIds(medicalRecordIds)
                .stream()
                .collect(Collectors.toMap(MedicalRecord::getId, r -> r));

        List<Long> patientIds = recordMap.values().stream()
                .map(MedicalRecord::getPatientId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> doctorIds = recordMap.values().stream()
                .map(MedicalRecord::getDoctorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userService.listUsersByUserIds(patientIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Doctor> doctorMap = doctorService.listDoctorsByIds(doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));

        List<Long> prescriptionIds = prescriptions.stream()
                .map(Prescription::getId)
                .collect(Collectors.toList());
        Map<Long, Long> itemCountMap = prescriptionItemService.listByPrescriptionIds(prescriptionIds)
                .stream()
                .collect(Collectors.groupingBy(PrescriptionItem::getPrescriptionId, Collectors.counting()));

        return prescriptions.stream().map(rx -> {
            PrescriptionPageItemVO vo = new PrescriptionPageItemVO();
            vo.setId(rx.getId());
            vo.setMedicalRecordId(rx.getMedicalRecordId());
            vo.setTotalAmount(rx.getTotalAmount());
            vo.setStatus(rx.getStatus());
            vo.setItemCount(itemCountMap.getOrDefault(rx.getId(), 0L).intValue());
            vo.setCreateTime(rx.getCreateTime());

            MedicalRecord record = recordMap.get(rx.getMedicalRecordId());
            if (record != null) {
                User user = userMap.get(record.getPatientId());
                if (user != null) {
                    vo.setPatientName(user.getNickname());
                }
                Doctor doctor = doctorMap.get(record.getDoctorId());
                if (doctor != null) {
                    vo.setDoctorName(doctor.getName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 管理端 - 处方详情
     * @param id 处方ID
     * @return 处方详情 VO
     * @throws BizException PRESCRIPTION_NOT_FOUND
     */
    public com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO getPrescriptionDetailForAdmin(Long id) {
        Prescription rx = prescriptionService.getById(id);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        MedicalRecord record = rx.getMedicalRecordId() == null
                ? null : medicalRecordService.getById(rx.getMedicalRecordId());

        com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO vo =
                new com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO();
        vo.setId(rx.getId());
        vo.setMedicalRecordId(rx.getMedicalRecordId());
        vo.setStatus(rx.getStatus());
        vo.setTotalAmount(rx.getTotalAmount());
        vo.setOrderId(rx.getOrderId());
        vo.setCreateTime(rx.getCreateTime());

        if (record != null) {
            vo.setPatientId(record.getPatientId());
            User user = userService.getUserById(record.getPatientId());
            if (user != null) {
                vo.setPatientName(user.getNickname());
            }
            Account account = accountService.getAccountByUserId(record.getPatientId());
            if (account != null) {
                vo.setPatientPhone(account.getPhone());
            }
            Doctor doctor = doctorService.getDoctorById(record.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
            }
        }

        List<PrescriptionItem> items = prescriptionItemService.listByPrescriptionId(id);
        List<Long> drugIds = items.stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Drug> drugMap = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        vo.setItems(items.stream().map(item -> {
            com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO.PrescriptionItemVO itemVO =
                    new com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO.PrescriptionItemVO();
            itemVO.setDrugId(item.getDrugId());
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUsageMethod(item.getUsageMethod());
            Drug drug = drugMap.get(item.getDrugId());
            if (drug != null) {
                itemVO.setCommonName(drug.getCommonName());
                itemVO.setSpecification(drug.getSpecification());
                itemVO.setUnit(drug.getUnit());
            }
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }
}
