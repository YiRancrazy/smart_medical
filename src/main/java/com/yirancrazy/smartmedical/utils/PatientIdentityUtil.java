package com.yirancrazy.smartmedical.utils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 患者身份证工具
 * @Author: YiRanCrazy@gmail.com
 * @Description: 身份证规范化与格式校验
 * @Datetime: 2026-10-22 23:00
 * @Version: 1.0
 */

public final class PatientIdentityUtil {

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^(\\d{15}|\\d{17}[\\dX])$");

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private PatientIdentityUtil() {
    }

    /**
     * 规范化身份证号
     * @param rawIdCard 原始身份证号
     * @return 规范化结果；空值返回 null
     */
    public static String normalizeIdCard(String rawIdCard) {
        if (rawIdCard == null) {
            return null;
        }
        String normalized = rawIdCard.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 校验身份证号格式和 18 位校验码
     * @param normalizedIdCard 已规范化身份证号
     * @return 是否合法
     */
    public static boolean isValidIdCard(String normalizedIdCard) {
        String idCard = normalizeIdCard(normalizedIdCard);
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }
        if (idCard.length() == 15) {
            return true;
        }
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11] == idCard.charAt(17);
    }
}
