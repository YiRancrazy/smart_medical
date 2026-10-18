package com.yirancrazy.smartmedical.service;

/**
 * 短信验证码服务
 * @Author: YiRanCrazy@gmail.com
 * @Description: 发送短信验证码到手机号，并在注册前校验其有效性；验证码存 Redis，一次性使用
 * @Datetime: 2026-09-04 20:30
 * @Version: 1.0
 */

public interface SmsService {

    /**
     * 生成并发送验证码（带 60s 重发冷却）
     * @param phone 手机号
     * @return 发送结果提示文案（mock 模式返回可识别提示，便于排查环境混淆）
     * @throws com.yirancrazy.smartmedical.exception.BizException 冷却期内或发送失败时抛出
     */
    String sendCode(String phone);

    /**
     * 校验验证码，成功后立即失效（一次性使用）
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @throws com.yirancrazy.smartmedical.exception.BizException 过期或错误时抛出
     */
    void verifyCode(String phone, String code);
}
