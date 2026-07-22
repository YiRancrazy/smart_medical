package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.RegistrationScheduleTemplateMapper;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班模板服务实现类
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class RegistrationScheduleTemplateServiceImpl implements RegistrationScheduleTemplateService {

    private final RegistrationScheduleTemplateMapper registrationScheduleTemplateMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertRegistrationScheduleTemplate(RegistrationScheduleTemplate registrationScheduleTemplate) {
        return registrationScheduleTemplateMapper.insert(registrationScheduleTemplate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertRegistrationScheduleTemplates(List<RegistrationScheduleTemplate> registrationScheduleTemplates) {
        registrationScheduleTemplateMapper.insert(registrationScheduleTemplates);
        return registrationScheduleTemplates.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegistrationScheduleTemplate getRegistrationScheduleTemplateById(Long id) {
        return registrationScheduleTemplateMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateRegistrationScheduleTemplateById(RegistrationScheduleTemplate registrationScheduleTemplate) {
        return registrationScheduleTemplateMapper.updateById(registrationScheduleTemplate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteRegistrationScheduleTemplateById(Long id) {
        return registrationScheduleTemplateMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegistrationScheduleTemplate> listAllRegistrationScheduleTemplates() {
        return registrationScheduleTemplateMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<RegistrationScheduleTemplate> listRegistrationScheduleTemplatesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RegistrationScheduleTemplate> registrationScheduleTemplates = registrationScheduleTemplateMapper
                .selectList(new LambdaQueryWrapper<RegistrationScheduleTemplate>()
                        .orderByAsc(RegistrationScheduleTemplate::getRegistrationDate));
        return new PageInfo<>(registrationScheduleTemplates);
    }

    @Override
    public List<RegistrationScheduleTemplate> listAllRegistrationScheduleTemplateByIdList(List<Long> registrationScheduleIdList) {
        return registrationScheduleTemplateMapper.selectList(new LambdaQueryWrapper<RegistrationScheduleTemplate>()
                .in(RegistrationScheduleTemplate::getId, registrationScheduleIdList));
    }

    @Override
    public List<RegistrationScheduleTemplate> listRegistrationScheduleTemplatesByDoctorIdListAndDate(List<Long> doctorIdList, LocalDate startDate, LocalDate endDate) {
        return registrationScheduleTemplateMapper.selectList(new LambdaQueryWrapper<RegistrationScheduleTemplate>()
                .in(RegistrationScheduleTemplate::getDoctorId, doctorIdList)
                .between(RegistrationScheduleTemplate::getRegistrationDate, startDate, endDate));
    }

    @Override
    public List<RegistrationScheduleTemplate> listRegistrationScheduleTemplatesByDoctorId(Long doctorId) {
        return registrationScheduleTemplateMapper.selectList(new LambdaQueryWrapper<RegistrationScheduleTemplate>()
                .eq(RegistrationScheduleTemplate::getDoctorId, doctorId));
    }

    @Override
    public List<RegistrationScheduleTemplate> getRegistrationScheduleTemplateByDoctorIdAndDate(Long doctorId, LocalDate date) {
        return registrationScheduleTemplateMapper.selectList(new LambdaQueryWrapper<RegistrationScheduleTemplate>()
                .eq(RegistrationScheduleTemplate::getDoctorId, doctorId)
                .eq(RegistrationScheduleTemplate::getRegistrationDate, date));
    }

    @Override
    public List<RegistrationScheduleTemplate> listRegistrationScheduleTemplatesByDoctorIdAndDate(Long doctorId, LocalDate startDate, LocalDate endDate) {
        return registrationScheduleTemplateMapper.selectList(new LambdaQueryWrapper<RegistrationScheduleTemplate>()
                .eq(RegistrationScheduleTemplate::getDoctorId, doctorId)
                .between(RegistrationScheduleTemplate::getRegistrationDate, startDate, endDate));
    }
}
