package com.yirancrazy.smartmedical.utils;

import com.yirancrazy.smartmedical.utils.NicknameGenerator.NicknameType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NicknameGenerator 单测
 */
class NicknameGeneratorTest {

    @Test
    void generateChineseNickname_isNotNull() {
        String nickname = NicknameGenerator.generateChineseNickname();
        assertNotNull(nickname);
        assertTrue(nickname.length() > 0);
    }

    @Test
    void generateEnglishNickname_isNotNull() {
        String nickname = NicknameGenerator.generateEnglishNickname();
        assertNotNull(nickname);
        assertTrue(nickname.length() > 0);
    }

    @Test
    void generateEnglishNicknameWithDot_containsDotAndLowercase() {
        String nickname = NicknameGenerator.generateEnglishNicknameWithDot();
        assertNotNull(nickname);
        assertTrue(nickname.contains("."), "expected a '.' separator, got: " + nickname);
        assertEquals(nickname.toLowerCase(), nickname, "should be lowercase");
    }

    @Test
    void generateEnglishNicknameWithUnderscore_containsUnderscore() {
        String nickname = NicknameGenerator.generateEnglishNicknameWithUnderscore();
        assertNotNull(nickname);
        assertTrue(nickname.contains("_"), "expected a '_' separator, got: " + nickname);
    }

    @Test
    void generateAnimalNickname_isNotNull() {
        String nickname = NicknameGenerator.generateAnimalNickname();
        assertNotNull(nickname);
        assertTrue(nickname.length() > 0);
    }

    @Test
    void generateNatureNickname_isNotNull() {
        String nickname = NicknameGenerator.generateNatureNickname();
        assertNotNull(nickname);
        assertTrue(nickname.length() > 0);
    }

    @Test
    void generateNicknameWithNumbers_endsWithNumber() {
        String nickname = NicknameGenerator.generateNicknameWithNumbers();
        assertNotNull(nickname);
        char last = nickname.charAt(nickname.length() - 1);
        assertTrue(Character.isDigit(last), "expected trailing digit, got: " + nickname);
    }

    @Test
    void generateSimpleEnglishNickname_isNotNull() {
        String nickname = NicknameGenerator.generateSimpleEnglishNickname();
        assertNotNull(nickname);
        assertTrue(nickname.length() > 0);
    }

    @Test
    void generateRandomNickname_isNotNull() {
        String nickname = NicknameGenerator.generateRandomNickname();
        assertNotNull(nickname);
        assertTrue(nickname.length() > 0);
    }

    @Test
    void generateRandomAlphabeticNickname_honorsLength() {
        String nickname = NicknameGenerator.generateRandomAlphabeticNickname(12);
        assertNotNull(nickname);
        assertEquals(12, nickname.length());
        for (int i = 0; i < nickname.length(); i++) {
            char c = nickname.charAt(i);
            assertTrue(Character.isLetter(c), "non-letter at index " + i + ": " + nickname);
        }
    }

    @Test
    void generateRandomAlphabeticNickname_zeroLengthDefaultsToEight() {
        String nickname = NicknameGenerator.generateRandomAlphabeticNickname(0);
        assertNotNull(nickname);
        assertEquals(8, nickname.length());
    }

    @Test
    void generateRandomAlphanumericNickname_honorsLength() {
        String nickname = NicknameGenerator.generateRandomAlphanumericNickname(16);
        assertNotNull(nickname);
        assertEquals(16, nickname.length());
        for (int i = 0; i < nickname.length(); i++) {
            char c = nickname.charAt(i);
            assertTrue(Character.isLetterOrDigit(c), "non-alphanumeric at index " + i + ": " + nickname);
        }
    }

    @Test
    void generateRandomAlphanumericNickname_zeroLengthDefaultsToEight() {
        String nickname = NicknameGenerator.generateRandomAlphanumericNickname(0);
        assertNotNull(nickname);
        assertEquals(8, nickname.length());
    }

    @Test
    void generateBatchNicknames_honorsCount() {
        String[] names = NicknameGenerator.generateBatchNicknames(5, NicknameType.CHINESE);
        assertEquals(5, names.length);
        for (String name : names) {
            assertNotNull(name);
            assertTrue(name.length() > 0);
        }
    }

    @Test
    void generateBatchNicknames_zeroCountDefaultsToOne() {
        String[] names = NicknameGenerator.generateBatchNicknames(0, NicknameType.CHINESE);
        assertEquals(1, names.length);
        assertNotNull(names[0]);
    }

    @Test
    void generateBatchNicknames_allTypesProduceNonEmpty() {
        for (NicknameType type : NicknameType.values()) {
            String[] names = NicknameGenerator.generateBatchNicknames(3, type);
            assertEquals(3, names.length);
            for (String name : names) {
                assertNotNull(name, "null entry for type " + type);
                assertTrue(name.length() > 0, "empty entry for type " + type);
            }
        }
    }

    @Test
    void generateBatchNicknames_producesVariety() {
        // 100 次生成，检查批量不全部相同（松散断言，避免偶发极端失败）
        String[] names = NicknameGenerator.generateBatchNicknames(100, NicknameType.RANDOM);
        Set<String> unique = new HashSet<>();
        for (String name : names) {
            unique.add(name);
        }
        assertTrue(unique.size() > 1, "expected varied outputs across 100 generations");
    }
}
