package com.yirancrazy.smartmedical.pojo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Result 单测
 */
class ResultTest {

    @Test
    void success_setsCode200AndDefaultMessage() {
        Result<String> result = Result.success("payload");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("payload", result.getData());
    }

    @Test
    void fail_setsCode500AndProvidedMessage() {
        Result<String> result = Result.fail("oops");
        assertEquals(500, result.getCode());
        assertEquals("oops", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void fail_withCustomCode_setsBoth() {
        Result<String> result = Result.fail(404, "not found");
        assertEquals(404, result.getCode());
        assertEquals("not found", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void info_setsCodeMessageAndData() {
        Result<Integer> result = Result.info(201, "created", 42);
        assertEquals(201, result.getCode());
        assertEquals("created", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    void booleanSuccessConstructor_setsDefaults() {
        Result<String> result = new Result<>("x", true, "ignored-on-success");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("x", result.getData());
    }

    @Test
    void booleanFailureConstructor_setsFailureFields() {
        Result<String> result = new Result<>(null, false, "boom");
        assertEquals(500, result.getCode());
        assertEquals("boom", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void singleArgConstructor_initializesFields() {
        Result<Integer> result = new Result<>(7);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(7, result.getData());
    }
}
