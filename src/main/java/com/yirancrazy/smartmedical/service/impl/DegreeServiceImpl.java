package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.DegreeMapper;
import com.yirancrazy.smartmedical.pojo.Degree;
import com.yirancrazy.smartmedical.service.DegreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 学历字典服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class DegreeServiceImpl implements DegreeService {

    private final DegreeMapper degreeMapper;

    /**
     * 插入学历
     * @param degree 学历
     * @return 插入的学历id
     */
    @Override
    public Integer insertDegree(Degree degree) {
        return degreeMapper.insert(degree);
    }

    /**
     * 根据id获取学历
     * @param id 学历id
     * @return 学历
     */
    @Override
    public Degree getDegreeById(Long id) {
        return degreeMapper.selectById(id);
    }

    /**
     * 更新学历
     * @param degree 学历
     * @return 影响的行数
     */
    @Override
    public Integer updateDegreeById(Degree degree) {
        return degreeMapper.updateById(degree);
    }

    /**
     * 删除学历
     * @param id 学历id
     * @return 影响的行数
     */
    @Override
    public Integer deleteDegreeById(Long id) {
        return degreeMapper.deleteById(id);
    }

    /**
     * 获取所有学历
     * @return 所有学历
     */
    @Override
    public List<Degree> listAllDegrees() {
        return degreeMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页获取学历
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页的学历
     */
    @Override
    public PageInfo<Degree> listDegreesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Degree> degrees = degreeMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(degrees);
    }
}
