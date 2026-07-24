package com.yirancrazy.smartmedical.pojo.dto.admin.request;

import lombok.Data;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员更新账户请求
 * @Datetime: 2026-07-24 16:30
 * @Version: 1.0
 */

@Data
public class AccountUpdateRequest {
    private Long roleId;        // 角色 ID（1 管理员 / 2 医生 / 4 用户 / 6 药师）
    private Boolean enabled;    // 是否启用
    private String phone;       // 手机号
}
