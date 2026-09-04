package com.yirancrazy.smartmedical.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.service.SmsService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现（Spug push）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证码存 Redis（5 分钟有效、一次性使用），带 60s 重发冷却；
 *              未配置 SMS_TEMPLATE_CODE 时进入 mock 模式，仅打印验证码到日志便于本地联调。
 * @Datetime: 2026-09-04 20:30
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    private static final String CODE_KEY_PREFIX = "sms:code:";
    private static final String COOLDOWN_KEY_PREFIX = "sms:code:cooldown:";

    @Value("${sms.base-url:https://push.spug.cc/sms/}")
    private String baseUrl;
    @Value("${sms.template-code:}")
    private String templateCode;
    @Value("${sms.code-expire-minutes:5}")
    private long codeExpireMinutes;
    @Value("${sms.resend-seconds:60}")
    private long resendSeconds;

    private final RedisUtil redisUtil;

    /**
     * 生成并发送验证码；冷却期内抛 BizException，发送失败删除已存验证码
     * @param phone 手机号
     */
    @Override
    public void sendCode(String phone) {
        String cooldownKey = COOLDOWN_KEY_PREFIX + phone;
        if (redisUtil.get(cooldownKey) != null) {
            log.warn("[sms] 发送过于频繁, phone={}", phone);
            throw new BizException(BizErrorCode.SMS_CODE_SEND_TOO_FREQUENT);
        }
        String code = RandomUtil.randomNumbers(6);
        redisUtil.setEx(CODE_KEY_PREFIX + phone, code, codeExpireMinutes, TimeUnit.MINUTES);
        redisUtil.setEx(cooldownKey, "1", resendSeconds, TimeUnit.SECONDS);

        if (isMock()) {
            log.warn("[sms] mock 模式（未配置 SMS_TEMPLATE_CODE），验证码 code={} 仅记录日志, phone={}", code, phone);
            return;
        }
        if (!doSend(phone, code)) {
            redisUtil.delete(CODE_KEY_PREFIX + phone);
            throw new BizException(BizErrorCode.SMS_CODE_SEND_FAILED);
        }
        log.info("[sms] 验证码已发送, phone={}", phone);
    }

    /**
     * 校验验证码，成功即删除（一次性使用）；过期/错误抛 BizException
     * @param phone 手机号
     * @param code  用户输入的验证码
     */
    @Override
    public void verifyCode(String phone, String code) {
        String key = CODE_KEY_PREFIX + phone;
        String stored = redisUtil.get(key);
        if (stored == null) {
            throw new BizException(BizErrorCode.SMS_CODE_EXPIRED);
        }
        if (!stored.equals(code)) {
            throw new BizException(BizErrorCode.SMS_CODE_WRONG);
        }
        redisUtil.delete(key);
    }

    /**
     * 未配置模板编码即视为 mock 模式（仅本地联调使用）
     */
    private boolean isMock() {
        return templateCode == null || templateCode.isBlank();
    }

    /**
     * 调用 Spug 短信接口发送验证码
     * @return true 发送成功；false 发送失败
     */
    private boolean doSend(String phone, String code) {
        String url = baseUrl.replaceAll("/+$", "") + "/" + templateCode;
        String body = JSONUtil.createObj().set("to", phone).set("code", code).toString();
        try {
            String resp = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .timeout(5000)
                    .execute().body();
            JSONObject json = JSONUtil.parseObj(resp);
            if (json.getInt("code") == null || json.getInt("code") != 200) {
                log.error("[sms] 发送失败 resp={} phone={}", resp, phone);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[sms] 调用 Spug 接口异常 phone={}", phone, e);
            return false;
        }
    }
}
