package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.PatientCardMapper;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.service.PatientCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 就诊卡服务实现类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class PatientCardServiceImpl implements PatientCardService {

    private final PatientCardMapper patientCardMapper;

    /**
     * 添加就诊卡
     * @param patientCard 就诊卡信息
     * @return 添加结果
     */
    @Override
    public int insertPatientCard(PatientCard patientCard) {
        return patientCardMapper.insert(patientCard);
    }

    /**
     * 根据ID获取就诊卡信息
     * @param id 就诊卡ID
     * @return 就诊卡信息
     */
    @Override
    public PatientCard getPatientCardById(Long id) {
        return patientCardMapper.selectById(id);
    }

    /**
     * 更新就诊卡信息
     * @param patientCard 就诊卡信息
     * @return 更新结果
     */
    @Override
    public int updatePatientCardById(PatientCard patientCard) {
        return patientCardMapper.updateById(patientCard);
    }

    /**
     * 根据ID删除就诊卡
     * @param id 就诊卡ID
     * @return 删除结果
     */
    @Override
    public int deletePatientCardById(Long id) {
        return patientCardMapper.deleteById(id);
    }

    /**
     * 获取所有就诊卡列表
     * @return 就诊卡列表
     */
    @Override
    public List<PatientCard> listAllPatientCards() {
        return patientCardMapper.selectList(null);
    }

    /**
     * 分页查询就诊卡
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    @Override
    public PageInfo<PatientCard> getPatientCardPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PatientCard> patientCards = patientCardMapper.selectList(null);
        return new PageInfo<>(patientCards);
    }

    /**
     * 批量删除就诊卡
     * @param ids 就诊卡ID列表
     * @return 删除结果
     */
    @Override
    public int deleteBatch(List<Long> ids) {
        return patientCardMapper.deleteBatchIds(ids);
    }
/// TODO 2026-02-28 12:02:19 临时注释, 七日后失效即可删除
    
    //    /**
//     * 根据用户ID获取就诊卡信息
//     * @param userId 用户ID
//     * @return 就诊卡信息
//     */
//    @Override
//    public PatientCard getPatientCardByUserId(Long userId) {
//        return patientCardMapper.selectOne(new QueryWrapper<PatientCard>().eq("user_id",userId));
//    }

//    /**
//     * 获取默认就诊人基本信息
//     * @param userId 账号ID
//     * @return
//     */
//    @Override
//    public PatientCard getDefaultPatientBaseInfoByUserId(Long userId) {
//        return patientCardMapper.getDefaultPatientBaseInfoByUserId(userId);
//    }

    /**
     * 根据用户ID获取所有就诊卡信息
     * @param userIdList 用户ID
     * @return 就诊卡信息
     */
    @Override
    public List<PatientCard> listPatientCardByUserIdList(List<Long> userIdList) {
        return patientCardMapper.selectList(new QueryWrapper<PatientCard>().in("user_id",userIdList));
    }

    @Override
    public List<PatientCard> getPatientCardsByIds(List<Long> ids) {
        return patientCardMapper.selectByIds(ids);
    }

    @Override
    public PatientCard getPatientCardBySn(Long patientCardSn) {
        return patientCardMapper.selectOne(new QueryWrapper<PatientCard>().eq("sn",patientCardSn));
    }
}
