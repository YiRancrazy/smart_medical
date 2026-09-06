package com.yirancrazy.smartmedical.utils;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * 医生账号初始密码生成工具：姓名拼音首字母大写 + 手机号
 * @Author: YiRanCrazy@gmail.com
 * @Description: 例如 张三 + 13800138000 = ZS13800138000
 * @Datetime: 2026-09-06 14:30
 * @Version: 1.0
 */

public final class DoctorInitPasswordUtil {

    private DoctorInitPasswordUtil() {
    }

    /**
     * 生成医生账号初始密码：姓名拼音首字母大写后与手机号拼接
     * @param name 医生姓名（中文）
     * @param phone 手机号
     * @return 初始密码明文
     */
    public static String generate(String name, String phone) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Pinyin.isChinese(c)) {
                String pinyin = Pinyin.toPinyin(c);
                if (pinyin != null && !pinyin.isEmpty()) {
                    sb.append(Character.toUpperCase(pinyin.charAt(0)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.append(phone).toString();
    }
}
