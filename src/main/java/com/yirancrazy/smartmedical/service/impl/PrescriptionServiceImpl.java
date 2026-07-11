package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yirancrazy.smartmedical.mapper.PrescriptionMapper;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import org.springframework.stereotype.Service;

/**
 * 处方 Service 实现
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方 Service 实现（骨架）
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Service
public class PrescriptionServiceImpl
        extends ServiceImpl<PrescriptionMapper, Prescription>
        implements PrescriptionService {
}