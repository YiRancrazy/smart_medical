package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班模板服务接口
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

public interface RegistrationScheduleTemplateService {

    /**
     * 添加挂号排班模板
     * @param registrationScheduleTemplate 挂号排班模板对象
     * @return 添加结果
     */
    Integer insertRegistrationScheduleTemplate(RegistrationScheduleTemplate registrationScheduleTemplate);

    /**
     * 批量添加挂号排班模板
     * @param registrationScheduleTemplates 挂号排班模板列表
     * @return 添加结果
     */
    Integer insertRegistrationScheduleTemplates(List<RegistrationScheduleTemplate> registrationScheduleTemplates);

    /**
     * 根据ID查询挂号排班模板
     * @param id 模板ID
     * @return 挂号排班模板对象
     */
    RegistrationScheduleTemplate getRegistrationScheduleTemplateById(Long id);

    /**
     * 根据ID更新挂号排班模板
     * @param registrationScheduleTemplate 挂号排班模板对象
     * @return 更新结果
     */
    Integer updateRegistrationScheduleTemplateById(RegistrationScheduleTemplate registrationScheduleTemplate);

    /**
     * 根据ID删除挂号排班模板
     * @param id 模板ID
     * @return 删除结果
     */
    Integer deleteRegistrationScheduleTemplateById(Long id);

    /**
     * 查询所有挂号排班模板
     * @return 挂号排班模板列表
     */
    List<RegistrationScheduleTemplate> listAllRegistrationScheduleTemplates();

    /**
     * 分页查询挂号排班模板
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<RegistrationScheduleTemplate> listRegistrationScheduleTemplatesByPage(Integer pageNum, Integer pageSize);

    /**
     * 通过挂号模板id列表查询挂号模板信息
     * @param registrationScheduleTemplateIdList 挂号模板id列表
     * @return 挂号模板信息
     */
    List<RegistrationScheduleTemplate> listAllRegistrationScheduleTemplateByIdList(List<Long> registrationScheduleIdList);

    /**
     * 通过医生id、开始日期、结束日期查询挂号排班模板
     * @param doctorId 医生id
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 挂号排班模板列表
     */
    List<RegistrationScheduleTemplate> listRegistrationScheduleTemplatesByDoctorIdAndDate(Long doctorId, LocalDate startDate, LocalDate endDate);

    /**
     * 通过医生id列表、开始日期、结束日期查询挂号排班模板
     * @param doctorIdList 医生id列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 挂号排班模板列表
     */
    List<RegistrationScheduleTemplate> listRegistrationScheduleTemplatesByDoctorIdListAndDate(List<Long> doctorIdList, LocalDate startDate, LocalDate endDate);

    /**
     * 通过医生id查询挂号排班模板
     * @param doctorId 医生id
     * @return 挂号排班模板列表
     */
    List<RegistrationScheduleTemplate> listRegistrationScheduleTemplatesByDoctorId(Long doctorId);

    /**
     * 通过医生id和日期查询挂号排班模板
     * @param doctorId 医生id
     * @param date 日期
     * @return 挂号排班模板列表
     */
    List<RegistrationScheduleTemplate> getRegistrationScheduleTemplateByDoctorIdAndDate(Long doctorId, LocalDate date);
}
