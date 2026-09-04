package com.yirancrazy.smartmedical.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 滑块验证码支撑组件（通过标记 / 连错冷却 / 客户端标识），供验证码接口与登录守卫共用
 * @Author: YiRanCrazy@gmail.com
 * @Description: 以「设备ID + 客户端IP」为维度维护滑块校验结果：
 *              校验通过写 passed 标记（登录守卫据此放行，登录不携带验证码）；
 *              校验失败累计次数，连错满 3 次进入 60s 冷却。
 * @Datetime: 2026-09-04 14:20
 * @Version: 1.0
 */

@Component
@RequiredArgsConstructor
public class CaptchaSupport {

    private final RedisUtil redisUtil;

    /** 客户端设备标识请求头 */
    private static final String DEVICE_HEADER = "X-Device-Id";
    /** 通过标记 TTL：5 分钟 */
    private static final long PASS_TTL_MINUTES = 5L;
    /** 连错阈值：3 次 */
    private static final long FAIL_THRESHOLD = 3L;
    /** 冷却时长：60 秒 */
    private static final long COOLDOWN_SECONDS = 60L;

    /**
     * 是否为已通过滑块校验的客户端
     * @param request 当前请求
     * @return true 表示该设备/IP 已通过且未过期
     */
    public boolean isPassed(HttpServletRequest request) {
        return redisUtil.get(passedKey(request)) != null;
    }

    /**
     * 标记滑块校验通过（设备/IP 维度，TTL 5 分钟）
     * @param request 当前请求
     */
    public void markPassed(HttpServletRequest request) {
        redisUtil.setEx(passedKey(request), "1", PASS_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 记录一次校验失败；达到阈值则对该设备/IP 进入冷却
     * @param request 当前请求
     * @return true 表示累计失败已满阈值并进入冷却
     */
    public boolean recordFail(HttpServletRequest request) {
        Long count = redisUtil.incrAndExpireOnFirst(failKey(request), 1L, FAIL_THRESHOLD, TimeUnit.MINUTES);
        if (count != null && count >= FAIL_THRESHOLD) {
            redisUtil.setEx(cooldownKey(request), "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 清空失败计数（校验通过后调用）
     * @param request 当前请求
     */
    public void clearFail(HttpServletRequest request) {
        redisUtil.delete(failKey(request));
    }

    /**
     * 是否处于冷却中（连错超阈值后的短暂禁校验）
     * @param request 当前请求
     * @return true 表示冷却中
     */
    public boolean isCooled(HttpServletRequest request) {
        return redisUtil.get(cooldownKey(request)) != null;
    }

    private String passedKey(HttpServletRequest request) {
        return clientKey(request, "pass");
    }

    private String failKey(HttpServletRequest request) {
        return clientKey(request, "fail");
    }

    private String cooldownKey(HttpServletRequest request) {
        return clientKey(request, "cooldown");
    }

    private String clientKey(HttpServletRequest request, String scene) {
        return "captcha:" + scene + ":" + clientIp(request) + ":" + deviceId(request);
    }

    /**
     * 获取客户端设备标识（前端在 get/check/login 三个请求统一携带）
     * @param request 当前请求
     * @return 设备ID；缺失时以"unknown"兜底，避免 key 拼接异常
     */
    public String deviceId(HttpServletRequest request) {
        String id = request.getHeader(DEVICE_HEADER);
        return (id == null || id.isBlank()) ? "unknown" : id;
    }

    /**
     * 获取客户端真实 IP（优先透传的 X-Forwarded-For 首段，否则取 remoteAddr）
     * @param request 当前请求
     * @return 客户端IP；取不到时以"unknown"兜底
     */
    public String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        return (ip == null || ip.isBlank()) ? "unknown" : ip;
    }
}