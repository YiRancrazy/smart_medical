package com.yirancrazy.smartmedical.utils;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * 密码校验与加密工具
 * @Author: YiRanCrazy@gmail.com
 * @Description: 兼容历史 BCrypt/MD5/明文三种存储格式校验，并提供 BCrypt 加密与升级判断；
 *              登录时若 needsBcryptUpgrade 返回 true，调用方应在校验通过后用 encode 重写数据库，逐步消除弱密码
 * @Datetime: 2026-07-23 22:30
 * @Version: 1.0
 */

public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * 校验原始密码与加密后密码是否匹配，兼容 BCrypt/MD5/明文历史格式
     * @param rawPassword 原始明文密码
     * @param encodedPassword 数据库存储的密码
     * @return 匹配返回 true，否则 false
     */
    public static boolean verify(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        if (isBcrypt(encodedPassword)) {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        }
        // MD5 兼容旧系统
        if (encodedPassword.equalsIgnoreCase(DigestUtil.md5Hex(rawPassword))) {
            return true;
        }
        // 明文兼容历史种子数据
        return rawPassword.equals(encodedPassword);
    }

    /**
     * 判断密码是否需要升级为 BCrypt（非 BCrypt 格式即需升级）
     * @param encodedPassword 数据库存储的密码
     * @return 需升级返回 true
     */
    public static boolean needsBcryptUpgrade(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        return !isBcrypt(encodedPassword);
    }

    /**
     * 用 BCrypt 加密明文密码
     * @param rawPassword 原始明文密码
     * @return BCrypt 加密后的密码
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    private static boolean isBcrypt(String s) {
        return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
    }
}
