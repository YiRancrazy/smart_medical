package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.ConsultationRoomMapper;
import com.yirancrazy.smartmedical.pojo.ConsultationRoom;
import com.yirancrazy.smartmedical.service.ConsultationRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 诊室服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class ConsultationRoomServiceImpl implements ConsultationRoomService {

    private final ConsultationRoomMapper consultationRoomMapper;

    /**
     * 插入诊室
     * @param consultationRoom 诊室
     * @return 插入的行数
     */
    @Override
    public Integer insertConsultationRoom(ConsultationRoom consultationRoom) {
        return consultationRoomMapper.insert(consultationRoom);
    }

    /**
     * 根据id获取诊室
     * @param id 诊室id
     * @return 诊室
     */
    @Override
    public ConsultationRoom getConsultationRoomById(Long id) {
        return consultationRoomMapper.selectById(id);
    }

    /**
     * 更新诊室
     * @param consultationRoom 诊室
     * @return 更新的行数
     */
    @Override
    public Integer updateConsultationRoomById(ConsultationRoom consultationRoom) {
        return consultationRoomMapper.updateById(consultationRoom);
    }

    /**
     * 删除诊室
     * @param id 诊室id
     * @return 删除的行数
     */
    @Override
    public Integer deleteConsultationRoomById(Long id) {
        return consultationRoomMapper.deleteById(id);
    }

    /**
     * 获取所有诊室
     * @return 诊室列表
     */
    @Override
    public List<ConsultationRoom> listAllConsultationRooms() {
        return consultationRoomMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页获取诊室
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 诊室列表
     */
    @Override
    public PageInfo<ConsultationRoom> listConsultationRoomsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ConsultationRoom> consultationRooms = consultationRoomMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(consultationRooms);
    }
}
