package com.yirancrazy.smartmedical.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.yirancrazy.smartmedical.manager.ConsultationRoomManager;
import com.yirancrazy.smartmedical.pojo.ConsultationRoom;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.common.PageResult;
import com.yirancrazy.smartmedical.pojo.dto.user.request.admin.ConsultationRoomRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员端 - 诊室管理
 * @Datetime: 2026-07-18 18:00
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/admin/v1/consultation-room")
@RequiredArgsConstructor
@Tag(name = "管理员端 - 诊室管理")
public class AdminConsultationRoomControllerV1 {

    private final ConsultationRoomManager consultationRoomManager;

    @PostMapping
    @Operation(summary = "管理员端 - 新增诊室")
    public Result<ConsultationRoom> add(@RequestBody ConsultationRoomRequest request) {
        ConsultationRoom room = new ConsultationRoom();
        BeanUtil.copyProperties(request, room);
        return Result.success(consultationRoomManager.addConsultationRoom(room));
    }

    @PutMapping("/{id}")
    @Operation(summary = "管理员端 - 修改诊室")
    public Result<Integer> update(@PathVariable Long id, @RequestBody ConsultationRoomRequest request) {
        ConsultationRoom room = new ConsultationRoom();
        BeanUtil.copyProperties(request, room);
        room.setId(id);
        return Result.success(consultationRoomManager.updateConsultationRoom(room));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "管理员端 - 删除诊室")
    public Result<Integer> delete(@PathVariable Long id) {
        return Result.success(consultationRoomManager.deleteConsultationRoom(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "管理员端 - 诊室详情")
    public Result<ConsultationRoom> detail(@PathVariable Long id) {
        return Result.success(consultationRoomManager.getConsultationRoom(id));
    }

    @GetMapping("/list")
    @Operation(summary = "管理员端 - 诊室分页列表")
    public Result<PageResult<ConsultationRoom>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return consultationRoomManager.listConsultationRooms(pageNum, pageSize);
    }
}
