package com.yirancrazy.smartmedical.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.MedicineMapper;
import com.yirancrazy.smartmedical.pojo.Medicine;
import com.yirancrazy.smartmedical.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineMapper medicineMapper;

    @Override
    public int insertMedicine(Medicine medicine) {
        return medicineMapper.insert(medicine);
    }

    @Override
    public Medicine getMedicineById(Long id) {
        return medicineMapper.selectById(id);
    }

    @Override
    public int updateMedicineById(Medicine medicine) {
        return medicineMapper.updateById(medicine);
    }

    @Override
    public int deleteMedicineById(Long id) {
        return medicineMapper.deleteById(id);
    }

    @Override
    public List<Medicine> listAllMedicines() {
        return medicineMapper.selectList(null);
    }

    @Override
    public PageInfo<Medicine> getMedicinePage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Medicine> medicines = medicineMapper.selectList(null);
        return new PageInfo<>(medicines);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        return medicineMapper.deleteByIds(ids);
    }
}
