package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Degree;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 学历字典服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */


public interface DegreeService {

    /**
     * 插入学历
     * @param degree 学历对象
     * @return 影响的行数
     */
    Integer insertDegree(Degree degree);

    /**
     * 根据id查询学历
     * @param id id
     * @return 学历对象
     */
    Degree getDegreeById(Long id);

    /**
     * 根据id更新学历
     * @param degree 学历对象
     * @return 影响的行数
     */
    Integer updateDegreeById(Degree degree);

    /**
     * 根据id删除学历
     * @param id id
     * @return 影响的行数
     */
    Integer deleteDegreeById(Long id);

    /**
     * 查询所有学历
     * @return 学历列表
     */
    List<Degree> listAllDegrees();

    /**
     * 分页查询学历
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 学历列表
     */
    PageInfo<Degree> listDegreesByPage(Integer pageNum, Integer pageSize);
}
