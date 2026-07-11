package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yirancrazy.smartmedical.mapper.MedicalRecordMapper;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import org.springframework.stereotype.Service;

/**
 * 电子病历 Service 实现
 * @Author: YiRanCrazy@gmail.com
 * @Description: 病历 Service 实现（骨架）
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Service
public class MedicalRecordServiceImpl
        extends ServiceImpl<MedicalRecordMapper, MedicalRecord>
        implements MedicalRecordService {
}