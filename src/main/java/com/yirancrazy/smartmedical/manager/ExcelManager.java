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
import java.util.*;

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

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy年M月d日"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日"),
            DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    };

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

        // 去重：跳过文件内重复及数据库已存在的同医生/日期/开始时段模板
        // 批量查已有模板，消除 N+1：按 (doctorId, date) 分组一次性查询
        Set<String> seen = new HashSet<>();
        Map<String, List<RegistrationScheduleTemplate>> existingMap = new HashMap<>();
        for (RegistrationScheduleTemplate template : templates) {
            String groupKey = template.getDoctorId() + "@" + template.getRegistrationDate();
            if (!existingMap.containsKey(groupKey)) {
                List<RegistrationScheduleTemplate> existing = registrationScheduleTemplateService
                        .getRegistrationScheduleTemplateByDoctorIdAndDate(
                                template.getDoctorId(), template.getRegistrationDate());
                existingMap.put(groupKey, existing == null ? Collections.emptyList() : existing);
            }
        }
        List<RegistrationScheduleTemplate> toInsert = new ArrayList<>();
        for (RegistrationScheduleTemplate template : templates) {
            String key = template.getDoctorId() + "#" + template.getRegistrationDate() + "#" + template.getStartTime();
            if (seen.contains(key)) {
                log.warn("[excel] 文件内重复，跳过：doctorId={}, date={}, startTime={}",
                        template.getDoctorId(), template.getRegistrationDate(), template.getStartTime());
                continue;
            }
            String groupKey = template.getDoctorId() + "@" + template.getRegistrationDate();
            boolean dup = existingMap.getOrDefault(groupKey, Collections.emptyList()).stream()
                    .anyMatch(e -> template.getStartTime() != null
                            && template.getStartTime().equals(e.getStartTime()));
            if (dup) {
                log.warn("[excel] 模板已存在，跳过：doctorId={}, date={}, startTime={}",
                        template.getDoctorId(), template.getRegistrationDate(), template.getStartTime());
            } else {
                seen.add(key);
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
            errors.add("第" + rowNo + "行：排班日期格式错误，当前值=[" + row.getRegistrationDate() + "]，支持 yyyy-MM-dd 或 yyyy/M/d");
            return null;
        }
        template.setRegistrationDate(date);

        LocalTime startTime = parseTime(row.getStartTime());
        LocalTime endTime = parseTime(row.getEndTime());
        if (startTime == null || endTime == null) {
            errors.add("第" + rowNo + "行：时间格式错误，开始时间=[" + row.getStartTime() + "], 结束时间=[" + row.getEndTime() + "], 支持 HH:mm 或 HH:mm:ss");
            return null;
        }
        template.setStartTime(startTime);
        template.setEndTime(endTime);

        template.setTotalQuota(row.getTotal());

        try {
            template.setPrice(Integer.valueOf(row.getPrice().trim()) * 100);
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
     * 解析日期，支持 yyyy-MM-dd、yyyy/M/d、yyyy/MM/dd、yyyy年MM月dd日、Excel 数字日期等
     * @param dateStr 日期字符串
     * @return 解析后的日期；失败返回 null
     */
    private LocalDate parseDate(String dateStr) {
        if (StrUtil.isBlank(dateStr)) {
            return null;
        }
        String trimmed = dateStr.trim().replace("\uFEFF", "");

        // ISO 日期
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ignored) {
        }

        // 自定义格式（含日期时间）
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalDateTime.parse(trimmed, formatter).toLocalDate();
            } catch (DateTimeParseException ignored) {
            }
        }

        // yyyyMMdd 纯数字日期（如 20260725）以及 Excel 保存后带小数的变体（如 20260726.5）
        try {
            String intPart = trimmed.contains(".") ? trimmed.substring(0, trimmed.indexOf('.')) : trimmed;
            return LocalDate.parse(intPart, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException | NumberFormatException ignored) {
        }

        // Excel 数字日期（1899-12-30 起的天数），仅解析合理范围内的数值
        try {
            double excelDays = Double.parseDouble(trimmed);
            if (excelDays > 0 && excelDays < 50000) {
                return LocalDate.of(1899, 12, 30).plusDays((long) excelDays);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    /**
     * 解析时间，支持 HH:mm、HH:mm:ss、H:mm、H:mm:ss，以及从日期时间字符串中提取时间
     * @param timeStr 时间字符串
     * @return 解析后的时间；失败返回 null
     */
    private LocalTime parseTime(String timeStr) {
        if (StrUtil.isBlank(timeStr)) {
            return null;
        }
        String trimmed = timeStr.trim().replace("\uFEFF", "");

        // 从日期时间字符串中提取时间部分（如 "2026/07/25 09:00:00"）
        int spaceIdx = trimmed.lastIndexOf(' ');
        if (spaceIdx > 0) {
            trimmed = trimmed.substring(spaceIdx + 1);
        }

        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("H:mm:ss")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
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
