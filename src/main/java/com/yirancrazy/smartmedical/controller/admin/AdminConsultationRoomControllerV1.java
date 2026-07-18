package com.yirancrazy.smartmedical.controller.admin;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.manager.ConsultationRoomManager;
import com.yirancrazy.smartmedical.pojo.ConsultationRoom;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Result<ConsultationRoom> add(@RequestBody ConsultationRoom room) {
        return Result.success(consultationRoomManager.addConsultationRoom(room));
    }

    @PutMapping("/{id}")
    @Operation(summary = "管理员端 - 修改诊室")
    public Result<Integer> update(@PathVariable Long id, @RequestBody ConsultationRoom room) {
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
    public Result<PageInfo<ConsultationRoom>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(consultationRoomManager.listConsultationRooms(pageNum, pageSize));
    }
}
