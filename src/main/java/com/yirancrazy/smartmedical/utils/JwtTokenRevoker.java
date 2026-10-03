package com.yirancrazy.smartmedical.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * JWT 账号级吊销记录器
 * @Author: YiRanCrazy@gmail.com
 * @Description: 记录账号级吊销时间戳，使该时间点之前签发的 access token 失效
 * @Datetime: 2026-10-03 10:00
 * @Version: 1.0
 */

@Component
@RequiredArgsConstructor
public final class JwtTokenRevoker {

    /** 与 access token 最长有效期保持一致 */
    private static final long REVOCATION_TTL_DAYS = 7L;

    @Value("${jwt.revokedTokenPrefix:admin-revoked-token:}")
    private String revokedTokenPrefix;

    private final RedisUtil redisUtil;

    /**
     * 吊销账号在本次调用之前签发的全部 JWT
     * @param accountId 账号 ID
     */
    public void revoke(Long accountId) {
        if (accountId == null) {
            return;
        }
        redisUtil.setEx(
                revokedTokenPrefix + accountId,
                String.valueOf(System.currentTimeMillis()),
                REVOCATION_TTL_DAYS,
                TimeUnit.DAYS);
    }

    /**
     * 查询账号最近一次吊销时间
     * @param accountId 账号 ID
     * @return 吊销时间戳（毫秒）；未吊销返回 null
     */
    public Long findRevokedAtMillis(Long accountId) {
        String value = redisUtil.get(revokedTokenPrefix + accountId);
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value.trim());
    }
}
