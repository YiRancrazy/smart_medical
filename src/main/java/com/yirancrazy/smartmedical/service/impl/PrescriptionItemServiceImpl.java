package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yirancrazy.smartmedical.mapper.PrescriptionItemMapper;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 处方明细 Service 实现
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方明细 Service 实现（骨架）
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Service
public class PrescriptionItemServiceImpl
        extends ServiceImpl<PrescriptionItemMapper, PrescriptionItem>
        implements PrescriptionItemService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PrescriptionItem> listByPrescriptionId(Long prescriptionId) {
        if (prescriptionId == null) {
            return List.of();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<PrescriptionItem>()
                .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PrescriptionItem> listByPrescriptionIds(Collection<Long> prescriptionIds) {
        if (prescriptionIds == null || prescriptionIds.isEmpty()) {
            return List.of();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<PrescriptionItem>()
                .in(PrescriptionItem::getPrescriptionId, prescriptionIds));
    }
}
