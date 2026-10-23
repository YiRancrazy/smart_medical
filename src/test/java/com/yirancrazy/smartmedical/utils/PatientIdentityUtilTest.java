package com.yirancrazy.smartmedical.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 患者身份证工具单测
 */
class PatientIdentityUtilTest {

    @Test
    void normalizeIdCard_trimsAndUppercases() {
        assertEquals("11010519491231002X", PatientIdentityUtil.normalizeIdCard(" 11010519491231002x "));
    }

    @Test
    void normalizeIdCard_blank_returnsNull() {
        assertNull(PatientIdentityUtil.normalizeIdCard("   "));
        assertNull(PatientIdentityUtil.normalizeIdCard(null));
    }

    @Test
    void isValidIdCard_accepts15And18Digits() {
        assertTrue(PatientIdentityUtil.isValidIdCard("11010519491231002X"));
        assertTrue(PatientIdentityUtil.isValidIdCard("110105491231002"));
    }

    @Test
    void isValidIdCard_rejectsWrongChecksumAndFormat() {
        assertFalse(PatientIdentityUtil.isValidIdCard("110105194912310021"));
        assertFalse(PatientIdentityUtil.isValidIdCard("123"));
        assertFalse(PatientIdentityUtil.isValidIdCard(null));
    }
}
