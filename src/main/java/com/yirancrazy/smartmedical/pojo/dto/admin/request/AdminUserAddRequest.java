package com.yirancrazy.smartmedical.pojo.dto.admin.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员端 - 新增用户请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员新增用户时提交的基础信息
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserAddRequest {

    private String nickname;

    private String username;

    private String avatar;

    private String address;

    private String idCard;

    private Integer sex;
}
