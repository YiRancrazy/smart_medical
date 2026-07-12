package com.yirancrazy.smartmedical.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IdGenerator 单测
 */
class IdGeneratorTest {

    @Test
    void nextId_returnsNonNull() {
        Long id = IdGenerator.nextId();
        assertNotNull(id);
    }

    @Test
    void nextId_returnsPositive() {
        Long id = IdGenerator.nextId();
        assertTrue(id > 0L, "snowflake id should be positive, got " + id);
    }

    @Test
    void nextId_returnsUniqueIds() {
        Set<Long> ids = new HashSet<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            ids.add(IdGenerator.nextId());
        }
        assertEquals(n, ids.size(), "snowflake ids should be unique");
    }

    @Test
    void nextIdStr_returnsStringRepresentation() {
        String idStr = IdGenerator.nextIdStr();
        assertNotNull(idStr);
        // 雪花 ID 字符串应可解析回 Long
        Long parsed = Long.valueOf(idStr);
        assertNotNull(parsed);
    }

    @Test
    void nextIdStr_isNotEmptyAndDiffersAcrossCalls() {
        String a = IdGenerator.nextIdStr();
        String b = IdGenerator.nextIdStr();
        assertNotEquals(a, b);
        assertTrue(a.length() > 0);
        assertTrue(b.length() > 0);
    }
}