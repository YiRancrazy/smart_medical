package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 电子病历实体类
 * @Author: YiRanCrazy@gmail.com
 * @Description: 一次挂号 = 一份病历（registration_id 唯一约束）
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "电子病历实体")
public class MedicalRecord {
    @Schema(description = "病历ID")
    @TableId
    private Long id;

    @Schema(description = "挂号ID(强绑定)")
    private Long registrationId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "主诉")
    private String chiefComplaint;

    @Schema(description = "现病史")
    private String presentIllness;

    @Schema(description = "既往史")
    private String pastHistory;

    @Schema(description = "查体")
    private String physicalExam;

    @Schema(description = "诊断")
    private String diagnosis;

    @Schema(description = "治疗方案")
    private String treatmentPlan;

    @Schema(description = "状态: 0-草稿 1-已提交")
    private Integer status;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}