package com.yirancrazy.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;

import java.util.Collection;
import java.util.List;

/**
 * 处方明细 Service
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方明细读写骨架，业务方法后续按需添加
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

public interface PrescriptionItemService extends IService<PrescriptionItem> {

    /**
     * 按处方ID查询明细
     * @param prescriptionId 处方ID
     * @return 明细列表
     */
    List<PrescriptionItem> listByPrescriptionId(Long prescriptionId);

    /**
     * 按处方ID集合批量查询明细（空集合返回空列表）
     * @param prescriptionIds 处方ID集合
     * @return 明细列表
     */
    List<PrescriptionItem> listByPrescriptionIds(Collection<Long> prescriptionIds);
}
