package com.yirancrazy.smartmedical.pojo.dto.user.response.admin.simple;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-18 17:03
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFileSimpleResponse {
        private Long id;               // 文件id
        private String name;           // 文件名
        private String md5;            // 文件md5
        private String path;           // 文件路径
        private Long size;             // 文件大小
}