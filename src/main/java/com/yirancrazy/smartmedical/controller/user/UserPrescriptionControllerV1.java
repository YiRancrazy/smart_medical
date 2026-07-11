package com.yirancrazy.smartmedical.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 处方查询
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@Tag(name = "用户端 - 处方", description = "我的处方查询")
@RestController
@RequestMapping("api/user/v1/prescription")
@RequiredArgsConstructor
public class UserPrescriptionControllerV1 {

    private final PrescriptionService prescriptionService;
    private final PrescriptionItemService prescriptionItemService;
    private final MedicalRecordService medicalRecordService;

    @Operation(summary = "用户端 - 处方详情")
    @Parameter(name = "id", description = "处方ID", required = true)
    @GetMapping("/{id}")
    public Result<PrescriptionDetailVO> detail(@PathVariable Long id, @RequestParam Long userId) {
        Prescription rx = prescriptionService.getById(id);
        if (rx == null) {
            return Result.fail("处方不存在");
        }
        // 通过 medicalRecord → patientId 校验所有权
        if (rx.getMedicalRecordId() != null) {
            MedicalRecord record = medicalRecordService.getById(rx.getMedicalRecordId());
            if (record == null || !userId.equals(record.getPatientId())) {
                return Result.fail("无权查看此处方");
            }
        }
        List<PrescriptionItem> items = prescriptionItemService.list(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, id));

        PrescriptionDetailVO vo = new PrescriptionDetailVO();
        vo.setId(rx.getId());
        vo.setStatus(rx.getStatus());
        vo.setTotalAmount(rx.getTotalAmount());
        vo.setOrderId(rx.getOrderId());
        vo.setItems(items.stream().map(item -> {
            PrescriptionDetailVO.PrescriptionItemVO itemVO = new PrescriptionDetailVO.PrescriptionItemVO();
            itemVO.setDrugId(item.getDrugId());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUsageMethod(item.getUsageMethod());
            return itemVO;
        }).collect(Collectors.toList()));
        return Result.success(vo);
    }
}