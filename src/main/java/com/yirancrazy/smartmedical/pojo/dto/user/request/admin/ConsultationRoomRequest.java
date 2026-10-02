package com.yirancrazy.smartmedical.pojo.dto.user.request.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 诊室新增/修改请求
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRoomRequest {

    private Long sn;

    private String name;

    private Long departmentId;

    private String location;

    private String equipmentDesc;
}
