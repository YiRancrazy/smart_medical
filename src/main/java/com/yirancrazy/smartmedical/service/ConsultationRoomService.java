package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.ConsultationRoom;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 诊室服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface ConsultationRoomService {

    /**
     * 插入诊室
     * @param consultationRoom 诊室
     * @return 插入的行数
     */
    Integer insertConsultationRoom(ConsultationRoom consultationRoom);

    /**
     * 根据id查询诊室
     * @param id 诊室id
     * @return 诊室
     */
    ConsultationRoom getConsultationRoomById(Long id);

    /**
     * 更新诊室
     * @param consultationRoom 诊室
     * @return 更新的行数
     */
    Integer updateConsultationRoomById(ConsultationRoom consultationRoom);

    /**
     * 删除诊室
     * @param id 诊室id
     * @return 删除的行数
     */
    Integer deleteConsultationRoomById(Long id);

    /**
     * 获取所有诊室
     * @return 诊室列表
     */
    List<ConsultationRoom> listAllConsultationRooms();

    /**
     * 分页获取诊室
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 诊室列表
     */
    PageInfo<ConsultationRoom> listConsultationRoomsByPage(Integer pageNum, Integer pageSize);
}
