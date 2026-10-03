package com.yirancrazy.smartmedical.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JwtTokenRevoker 单测
 * 覆盖账号级吊销时间戳的写入、读取与异常值处理
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenRevokerTest {

    @Mock
    private RedisUtil redisUtil;

    private JwtTokenRevoker jwtTokenRevoker;

    @BeforeEach
    void setUp() {
        jwtTokenRevoker = new JwtTokenRevoker(redisUtil);
        ReflectionTestUtils.setField(jwtTokenRevoker, "revokedTokenPrefix", "admin-revoked-token:");
    }

    @Test
    void revoke_shouldWriteTimestampWithSevenDayTtl() {
        jwtTokenRevoker.revoke(42L);

        verify(redisUtil).setEx(
                eq("admin-revoked-token:42"),
                anyString(),
                eq(7L),
                eq(TimeUnit.DAYS));
    }

    @Test
    void revoke_whenAccountIdNull_shouldSkipRedis() {
        jwtTokenRevoker.revoke(null);

        verify(redisUtil, never()).setEx(anyString(), anyString(), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    void findRevokedAtMillis_shouldReturnParsedTimestamp() {
        when(redisUtil.get("admin-revoked-token:42")).thenReturn("1728000000000");

        assertEquals(1728000000000L, jwtTokenRevoker.findRevokedAtMillis(42L));
    }

    @Test
    void findRevokedAtMillis_whenMissing_shouldReturnNull() {
        when(redisUtil.get("admin-revoked-token:42")).thenReturn(null);

        assertNull(jwtTokenRevoker.findRevokedAtMillis(42L));
    }

    @Test
    void findRevokedAtMillis_whenBlank_shouldReturnNull() {
        when(redisUtil.get("admin-revoked-token:42")).thenReturn("   ");

        assertNull(jwtTokenRevoker.findRevokedAtMillis(42L));
    }

    @Test
    void findRevokedAtMillis_whenMalformed_shouldThrowNumberFormatException() {
        when(redisUtil.get("admin-revoked-token:42")).thenReturn("not-a-timestamp");

        assertThrows(NumberFormatException.class, () -> jwtTokenRevoker.findRevokedAtMillis(42L));
    }
}
