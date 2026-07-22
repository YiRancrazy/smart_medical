package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.type.RegistrationShiftTypeEnum;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.excel.ExcelRegistrationTemplate;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入管理器
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班模板 Excel 解析与导入
 * @Datetime: 2026-03-18 18:03
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class ExcelManager {
    private final RegistrationScheduleService registrationScheduleService;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;

    private static final DateTimeFormatter SLASH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");

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
            }).charset(java.nio.charset.StandardCharsets.UTF_8).sheet().headRowNumber(1).doReadSync();
        } finally {
            file.delete();
        }

        List<String> errors = new ArrayList<>();
        List<RegistrationScheduleTemplate> templates = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            RegistrationScheduleTemplate template = convert(list.get(i), i + 1, errors);
            if (template != null) {
                templates.add(template);
            }
        }

        if (!errors.isEmpty()) {
            return Result.fail(String.join("; ", errors));
        }
        if (templates.isEmpty()) {
            return Result.fail("未读取到有效数据，请检查文件内容");
        }

        // 去重：跳过已存在的同医生/日期/开始时段模板
        List<RegistrationScheduleTemplate> toInsert = new ArrayList<>();
        for (RegistrationScheduleTemplate template : templates) {
            List<RegistrationScheduleTemplate> existing = registrationScheduleTemplateService
                    .getRegistrationScheduleTemplateByDoctorIdAndDate(
                            template.getDoctorId(), template.getRegistrationDate());
            boolean dup = existing != null && existing.stream()
                    .anyMatch(e -> template.getStartTime() != null
                            && template.getStartTime().equals(e.getStartTime()));
            if (dup) {
                log.warn("[excel] 模板已存在，跳过：doctorId={}, date={}, startTime={}",
                        template.getDoctorId(), template.getRegistrationDate(), template.getStartTime());
            } else {
                toInsert.add(template);
            }
        }
        if (toInsert.isEmpty()) {
            return Result.fail("所有模板均已存在，未生成新排班");
        }

        registrationScheduleTemplateService.insertRegistrationScheduleTemplates(toInsert);

        List<RegistrationSchedule> schedules = buildSchedules(toInsert);
        registrationScheduleService.insertRegistrationScheduleList(schedules);

        return Result.success(toInsert.size());
    }

    /**
     * 将 Excel 行转换为挂号排班模板对象
     * @param row Excel 行数据
     * @param rowNo 行号（从 1 开始）
     * @param errors 错误收集列表
     * @return 转换后的模板对象；数据非法时返回 null 并向 errors 追加错误信息
     */
    private RegistrationScheduleTemplate convert(ExcelRegistrationTemplate row, int rowNo, List<String> errors) {
        if (isBlank(row.getDoctorId())) {
            errors.add("第" + rowNo + "行：医生id为空");
            return null;
        }
        if (isBlank(row.getDoctorName())) {
            errors.add("第" + rowNo + "行：医生姓名为空");
            return null;
        }
        if (isBlank(row.getRegistrationDate())) {
            errors.add("第" + rowNo + "行：排班日期为空");
            return null;
        }
        if (isBlank(row.getStartTime()) || isBlank(row.getEndTime())) {
            errors.add("第" + rowNo + "行：开始时间或结束时间为空");
            return null;
        }
        if (row.getTotal() == null) {
            errors.add("第" + rowNo + "行：号源数量为空");
            return null;
        }
        if (isBlank(row.getPrice())) {
            errors.add("第" + rowNo + "行：挂号价格为空");
            return null;
        }
        if (isBlank(row.getSn())) {
            errors.add("第" + rowNo + "行：诊室编号为空");
            return null;
        }

        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(IdUtil.getSnowflakeNextId());
        template.setName(row.getDoctorName() + "医生普通门诊");

        try {
            template.setDoctorId(Long.valueOf(row.getDoctorId().trim()));
        } catch (NumberFormatException e) {
            errors.add("第" + rowNo + "行：医生id格式错误");
            return null;
        }

        Integer registrationType = RegistrationShiftTypeEnum.getCodeByName(row.getRegistrationType());
        if (registrationType == null) {
            errors.add("第" + rowNo + "行：班次类型非法");
            return null;
        }
        template.setRegistrationType(registrationType);

        LocalDate date = parseDate(row.getRegistrationDate());
        if (date == null) {
            errors.add("第" + rowNo + "行：排班日期格式错误，支持 yyyy-MM-dd 或 yyyy/M/d");
            return null;
        }
        template.setRegistrationDate(date);

        try {
            template.setStartTime(LocalTime.parse(row.getStartTime().trim()));
            template.setEndTime(LocalTime.parse(row.getEndTime().trim()));
        } catch (DateTimeParseException e) {
            errors.add("第" + rowNo + "行：时间格式错误，支持 HH:mm 或 HH:mm:ss");
            return null;
        }

        template.setTotalQuota(row.getTotal());

        try {
            template.setPrice(Integer.valueOf(row.getPrice().trim()));
        } catch (NumberFormatException e) {
            errors.add("第" + rowNo + "行：挂号价格格式错误");
            return null;
        }

        template.setPriority(0);
        template.setEnabled(true);

        try {
            template.setConsultationRoomId(Long.valueOf(row.getSn().trim()));
        } catch (NumberFormatException e) {
            errors.add("第" + rowNo + "行：诊室编号格式错误");
            return null;
        }

        template.setRemark(row.getRemark());
        return template;
    }

    /**
     * 根据模板生成医生排班列表
     * @param templates 挂号排班模板列表
     * @return 医生排班列表
     */
    private List<RegistrationSchedule> buildSchedules(List<RegistrationScheduleTemplate> templates) {
        List<RegistrationSchedule> schedules = new ArrayList<>();
        for (RegistrationScheduleTemplate template : templates) {
            Duration duration = Duration.between(template.getStartTime(), template.getEndTime());
            long hours = duration.toHours();
            if (hours <= 0) {
                log.warn("[excel] 模板 {} 时间区间不合法，跳过生成排班", template.getId());
                continue;
            }

            Integer totalQuota = template.getTotalQuota();
            int quota = Math.toIntExact(template.getTotalQuota() / hours);
            int remaining = totalQuota;

            for (int i = 0; i < hours; i++) {
                RegistrationSchedule schedule = new RegistrationSchedule();
                schedule.setStartTime(LocalDateTime.of(template.getRegistrationDate(), template.getStartTime().plusHours(i)));
                schedule.setEndTime(schedule.getStartTime().plusHours(1));
                schedule.setDoctorId(template.getDoctorId());
                schedule.setRegistrationScheduleTemplateId(template.getId());
                schedule.setStatus(1);
                if (remaining - quota < 0) {
                    schedule.setRemainingQuota(remaining);
                } else {
                    schedule.setRemainingQuota(quota);
                }
                remaining -= schedule.getRemainingQuota();
                schedule.setId(IdUtil.getSnowflakeNextId());
                schedules.add(schedule);
            }
        }
        return schedules;
    }

    /**
     * 解析日期，支持 yyyy-MM-dd 与 yyyy/M/d
     * @param dateStr 日期字符串
     * @return 解析后的日期；失败返回 null
     */
    private LocalDate parseDate(String dateStr) {
        String trimmed = dateStr.trim();
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(trimmed, SLASH_DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    /**
     * 判断字符串是否为空或空白
     * @param str 字符串
     * @return 是否为空
     */
    private boolean isBlank(String str) {
        return StrUtil.isBlank(str);
    }
}
