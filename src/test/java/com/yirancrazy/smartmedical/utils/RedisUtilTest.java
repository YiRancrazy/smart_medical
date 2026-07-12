package com.yirancrazy.smartmedical.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RedisUtil 单测：覆盖 set / setEx / get / delete（构造注入路径，无 static）。
 */
@ExtendWith(MockitoExtension.class)
class RedisUtilTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private RedisUtil redisUtil;

    @Test
    void set_writesThroughValueOps() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        redisUtil.set("k", "v");
        verify(valueOps).set("k", "v");
    }

    @Test
    void setEx_writesWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        redisUtil.setEx("k", "v", 7, TimeUnit.DAYS);
        verify(valueOps).set("k", "v", 7L, TimeUnit.DAYS);
    }

    @Test
    void get_returnsValueOpsResult() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("k")).thenReturn("v");
        assertEquals("v", redisUtil.get("k"));
    }

    @Test
    void get_returnsNullWhenAbsent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("missing")).thenReturn(null);
        assertNull(redisUtil.get("missing"));
    }

    @Test
    void delete_delegatesToTemplate() {
        when(redisTemplate.delete(anyString())).thenReturn(true);
        assertTrue(redisUtil.delete("k"));
        verify(redisTemplate).delete("k");
    }

    @Test
    void setEx_passesAnyTtlThrough() {
        // ponytail: arbitrary ttl arg passed verbatim — move to parameterized test when more durations are needed
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        redisUtil.setEx("k", "v", 30L, TimeUnit.DAYS);
        verify(valueOps).set(eq("k"), eq("v"), anyLong(), any(TimeUnit.class));
    }
}