package com.yirancrazy.smartmedical.utils;

import cn.hutool.core.util.IdUtil;

/**
 * 雪花 ID 生成工具
 * @Author: YiRanCrazy@gmail.com
 * @Description: 雪花 ID 生成工具
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * 生成下一个雪花 ID（Long）
     */
    public static Long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 生成下一个雪花 ID（String）
     */
    public static String nextIdStr() {
        return String.valueOf(IdUtil.getSnowflakeNextId());
    }
}