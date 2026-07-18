package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.type.RegistrationShiftTypeEnum;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.excel.ExcelRegistrationTemplate;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-18 18:03
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class ExcelManager {
    private final RegistrationScheduleService registrationScheduleService;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;

    /**
     * 上传挂号排班 excel 文件
     * @param excelFile excel 文件
     * @return 插入数量
     */
    public Result<Integer> uploadRegistrationTemplate(MultipartFile excelFile) {
        if (excelFile == null || excelFile.isEmpty()) {
            return Result.fail("请上传有效文件");
        }
        // 将 MultipartFile 转换为临时文件
        File file;
        try {
            String suffix = "";
            String originalFilename = excelFile.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            file = File.createTempFile("upload_", suffix);
            excelFile.transferTo(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }

        List<ExcelRegistrationTemplate> list;
        try {
            list = EasyExcel.read(file, ExcelRegistrationTemplate.class, new AnalysisEventListener<ExcelRegistrationTemplate>() {
                private final List<ExcelRegistrationTemplate> rows = new ArrayList<>();

                @Override
                public void invoke(ExcelRegistrationTemplate excelRegistrationTemplate, AnalysisContext analysisContext) {
                    rows.add(excelRegistrationTemplate);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    log.info("[excel] 读取完成，共{}行", rows.size());
                }
            }).sheet().doReadSync();
        } finally {
            file.delete();
        }


        // 读取 excel 文件记录，将其转换为 RegistrationScheduleTemplate 对象
        List<RegistrationScheduleTemplate> list1 = list.stream().map(
                excelRegistrationTemplate -> {
                    RegistrationScheduleTemplate registrationScheduleTemplate = new RegistrationScheduleTemplate();  // 创建挂号排班模板对象
                    registrationScheduleTemplate.setId(IdUtil.getSnowflakeNextId()); // 设置id
                    registrationScheduleTemplate.setName(excelRegistrationTemplate.getDoctorName()+"医生普通门诊"); // 设置挂号排班模板名称
                    registrationScheduleTemplate.setDoctorId(Long.valueOf(excelRegistrationTemplate.getDoctorId())); // 设置关联医生id
                    registrationScheduleTemplate.setRegistrationType(RegistrationShiftTypeEnum
                            .getCodeByName(excelRegistrationTemplate.getRegistrationType())); // 挂号类型
                    // 挂号日期
                    registrationScheduleTemplate.setRegistrationDate(LocalDate
                            .parse(excelRegistrationTemplate
                                    .getRegistrationDate()));
                    registrationScheduleTemplate.setStartTime(LocalTime.parse(excelRegistrationTemplate.getStartTime())); // 开始时间
                    registrationScheduleTemplate.setEndTime(LocalTime.parse(excelRegistrationTemplate.getEndTime())); // 结束时间
                    registrationScheduleTemplate.setTotalQuota(excelRegistrationTemplate.getTotal()); // 总数
                    registrationScheduleTemplate.setPrice(Integer.valueOf(excelRegistrationTemplate.getPrice())); //  价格
                    registrationScheduleTemplate.setPriority(0);  // 优先级
                    registrationScheduleTemplate.setEnabled(true); // 设置是否启用
                    registrationScheduleTemplate.setConsultationRoomId(Long.valueOf(excelRegistrationTemplate.getSn())); // 诊室编号
                    registrationScheduleTemplate.setRemark(excelRegistrationTemplate.getRemark());  // 设置备注
                    return registrationScheduleTemplate;
                }
        ).toList();

        registrationScheduleTemplateService.insertRegistrationScheduleTemplates(list1);

        List<RegistrationSchedule> list2= new ArrayList<>();
        // 生成对应的医生排班表
        for (RegistrationScheduleTemplate registrationScheduleTemplate : list1) {
            // 计算时间间隔
            Duration duration = Duration.between(registrationScheduleTemplate.getStartTime(), registrationScheduleTemplate.getEndTime());
            // 生成挂号排班对象

            Integer totalQuota = registrationScheduleTemplate.getTotalQuota();
            int quota = Math.toIntExact(registrationScheduleTemplate.getTotalQuota() / duration.toHours());
            for (int i = 0; i < duration.toHours(); i++) {
                RegistrationSchedule registrationSchedule = new RegistrationSchedule();

                registrationSchedule.setStartTime(
                        LocalDateTime.of(registrationScheduleTemplate.getRegistrationDate(),registrationScheduleTemplate.getStartTime().plusHours(i))
                );
                registrationSchedule.setEndTime(registrationSchedule.getStartTime().plusHours(1));
                registrationSchedule.setDoctorId(registrationScheduleTemplate.getDoctorId());
                registrationSchedule.setRegistrationScheduleTemplateId(registrationScheduleTemplate.getId());
                registrationSchedule.setStatus(registrationSchedule.getStatus());
                if(totalQuota - quota<0){
                    registrationSchedule.setRemainingQuota(totalQuota);
                }else {
                    registrationSchedule.setRemainingQuota(quota);
                }
                totalQuota-=quota;
                registrationSchedule.setId(IdUtil.getSnowflakeNextId()); // 多条记录需要重新生成id

                list2.add(registrationSchedule);
            }

        }
        registrationScheduleService.insertRegistrationScheduleList(list2);


        return Result.success(1);
    }
}