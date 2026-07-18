package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.ConsultationRoom;
import com.yirancrazy.smartmedical.service.ConsultationRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 诊室业务编排
 * @Datetime: 2026-07-18 18:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class ConsultationRoomManager {

    private final ConsultationRoomService consultationRoomService;

    /**
     * 新增诊室
     */
    public ConsultationRoom addConsultationRoom(ConsultationRoom room) {
        room.setId(IdUtil.getSnowflakeNextId());
        consultationRoomService.insertConsultationRoom(room);
        return room;
    }

    /**
     * 更新诊室
     */
    public Integer updateConsultationRoom(ConsultationRoom room) {
        return consultationRoomService.updateConsultationRoomById(room);
    }

    /**
     * 删除诊室
     */
    public Integer deleteConsultationRoom(Long id) {
        return consultationRoomService.deleteConsultationRoomById(id);
    }

    /**
     * 查询诊室
     */
    public ConsultationRoom getConsultationRoom(Long id) {
        return consultationRoomService.getConsultationRoomById(id);
    }

    /**
     * 分页查询诊室
     */
    public PageInfo<ConsultationRoom> listConsultationRooms(Integer pageNum, Integer pageSize) {
        return consultationRoomService.listConsultationRoomsByPage(pageNum, pageSize);
    }
}
