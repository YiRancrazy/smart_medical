package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Medicine;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface MedicineService {

    /**
     * 添加药品
     * @param medicine 药品信息
     * @return 添加结果
     */
    int insertMedicine(Medicine medicine);

    /**
     * 根据ID获取药品信息
     * @param id 药品ID
     * @return 药品信息
     */
    Medicine getMedicineById(Long id);

    /**
     * 更新药品信息
     * @param medicine 药品信息
     * @return 更新结果
     */
    int updateMedicineById(Medicine medicine);

    /**
     * 根据ID删除药品
     * @param id 药品ID
     * @return 删除结果
     */
    int deleteMedicineById(Long id);

    /**
     * 获取所有药品列表
     * @return 药品列表
     */
    List<Medicine> listAllMedicines();

    /**
     * 分页查询药品
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Medicine> getMedicinePage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除药品
     * @param ids 药品ID列表
     * @return 删除结果
     */
    int deleteBatch(List<Long> ids);
}
