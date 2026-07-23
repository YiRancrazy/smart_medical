package com.yirancrazy.smartmedical.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类（精简版）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 仅暴露当前业务实际使用的几个 Redis 操作；构造注入，无 static 字段。
 * @Datetime: 2026-07-11 12:30
 * @Version: 1.0
 */

@Component
@RequiredArgsConstructor
public final class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    /**
     * 写入字符串（无过期时间）
     * @param key 键
     * @param value 值
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入字符串并设置过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时长
     * @param unit 时间单位
     */
    public void setEx(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 读取字符串
     * @param key 键
     * @return 值；不存在返回 null
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除键
     * @param key 键
     * @return true 删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 自增计数并在首次自增时设置过期时间（用于滑动窗口限流等场景）
     * @param key 键
     * @param delta 自增量（通常为 1）
     * @param timeout 过期时长
     * @param unit 时间单位
     * @return 自增后的值；不存在则从 0 开始
     */
    public Long incrAndExpireOnFirst(String key, long delta, long timeout, TimeUnit unit) {
        Long count = redisTemplate.opsForValue().increment(key, delta);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, timeout, unit);
        }
        return count;
    }
}