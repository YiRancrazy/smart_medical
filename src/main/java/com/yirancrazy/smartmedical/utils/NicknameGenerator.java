package com.yirancrazy.smartmedical.utils;

import java.util.Random;

/**
 * 随机昵称生成工具类
 */
public class NicknameGenerator {
    
    // 私有构造方法，防止实例化
    private NicknameGenerator() {}
    
    // 常用形容词
    private static final String[] ADJECTIVES = {
        "勇敢的", "聪明的", "快乐的", "安静的", "热情的", "温柔的", "幽默的", "神秘的",
        "优雅的", "活泼的", "坚强的", "诚实的", "浪漫的", "冷静的", "好奇的", "勤奋的",
        "乐观的", "谦虚的", "独立的", "创意的", "敏捷的", "稳重的", "慷慨的", "耐心的",
        "可爱的", "帅气的", "美丽的", "酷炫的", "时尚的", "独特的", "非凡的", "完美的"
    };
    
    // 常用名词
    private static final String[] NOUNS = {
        "老虎", "狮子", "熊猫", "狐狸", "狼", "鹰", "海豚", "蝴蝶",
        "猫咪", "狗狗", "兔子", "小鸟", "鱼儿", "海星", "贝壳", "彩虹",
        "星星", "月亮", "太阳", "云朵", "山川", "河流", "森林", "花朵",
        "战士", "骑士", "法师", "精灵", "巫师", "诗人", "画家", "音乐家",
        "程序员", "探险家", "旅行者", "梦想家", "观察者", "思考者", "学习者"
    };
    
    // 英文形容词
    private static final String[] EN_ADJECTIVES = {
        "Brave", "Smart", "Happy", "Quiet", "Warm", "Gentle", "Funny", "Mysterious",
        "Elegant", "Active", "Strong", "Honest", "Romantic", "Calm", "Curious", "Hardworking",
        "Optimistic", "Modest", "Independent", "Creative", "Swift", "Steady", "Generous", "Patient",
        "Lovely", "Handsome", "Beautiful", "Cool", "Fashion", "Unique", "Extraordinary", "Perfect"
    };
    
    // 英文名词
    private static final String[] EN_NOUNS = {
        "Tiger", "Lion", "Panda", "Fox", "Wolf", "Eagle", "Dolphin", "Butterfly",
        "Cat", "Dog", "Rabbit", "Bird", "Fish", "Starfish", "Shell", "Rainbow",
        "Star", "Moon", "Sun", "Cloud", "Mountain", "River", "Forest", "Flower",
        "Warrior", "Knight", "Mage", "Elf", "Wizard", "Poet", "Painter", "Musician",
        "Programmer", "Explorer", "Traveler", "Dreamer", "Observer", "Thinker", "Learner"
    };
    
    // 随机数生成器
    private static final Random random = new Random();
    
    /**
     * 生成中文昵称（形容词+名词）
     */
    public static String generateChineseNickname() {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        return adjective + noun;
    }
    
    /**
     * 生成英文昵称（形容词+名词）
     */
    public static String generateEnglishNickname() {
        String adjective = EN_ADJECTIVES[random.nextInt(EN_ADJECTIVES.length)];
        String noun = EN_NOUNS[random.nextInt(EN_NOUNS.length)];
        return adjective + noun;
    }
    
    /**
     * 生成英文昵称（小写，用点分隔）
     */
    public static String generateEnglishNicknameWithDot() {
        String adjective = EN_ADJECTIVES[random.nextInt(EN_ADJECTIVES.length)].toLowerCase();
        String noun = EN_NOUNS[random.nextInt(EN_NOUNS.length)].toLowerCase();
        return adjective + "." + noun;
    }
    
    /**
     * 生成英文昵称（首字母大写，用下划线分隔）
     */
    public static String generateEnglishNicknameWithUnderscore() {
        String adjective = EN_ADJECTIVES[random.nextInt(EN_ADJECTIVES.length)];
        String noun = EN_NOUNS[random.nextInt(EN_NOUNS.length)];
        return adjective + "_" + noun;
    }
    
    /**
     * 生成动物主题昵称
     */
    public static String generateAnimalNickname() {
        String[] animals = {"熊猫", "老虎", "狮子", "狐狸", "狼", "鹰", "海豚", "蝴蝶", "猫咪", "狗狗"};
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String animal = animals[random.nextInt(animals.length)];
        return adjective + animal;
    }
    
    /**
     * 生成自然主题昵称
     */
    public static String generateNatureNickname() {
        String[] nature = {"山川", "河流", "森林", "花朵", "星星", "月亮", "太阳", "云朵", "彩虹", "海洋"};
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String nat = nature[random.nextInt(nature.length)];
        return adjective + nat;
    }
    
    /**
     * 生成带数字的昵称
     */
    public static String generateNicknameWithNumbers() {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int number = random.nextInt(1000); // 0-999之间的随机数
        return adjective + noun + number;
    }
    
    /**
     * 生成简洁英文昵称（单个单词）
     */
    public static String generateSimpleEnglishNickname() {
        // 50%概率使用形容词，50%概率使用名词
        if (random.nextBoolean()) {
            return EN_ADJECTIVES[random.nextInt(EN_ADJECTIVES.length)];
        } else {
            return EN_NOUNS[random.nextInt(EN_NOUNS.length)];
        }
    }
    
    /**
     * 随机生成任意类型的昵称
     */
    public static String generateRandomNickname() {
        int type = random.nextInt(6);
        switch (type) {
            case 0: return generateChineseNickname();
            case 1: return generateEnglishNickname();
            case 2: return generateEnglishNicknameWithDot();
            case 3: return generateAnimalNickname();
            case 4: return generateNatureNickname();
            case 5: return generateNicknameWithNumbers();
            default: return generateChineseNickname();
        }
    }
    
    /**
     * 生成指定长度的随机字母昵称
     */
    public static String generateRandomAlphabeticNickname(int length) {
        if (length <= 0) {
            length = 8;
        }
        
        StringBuilder sb = new StringBuilder();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * 生成指定长度的随机字母数字昵称
     */
    public static String generateRandomAlphanumericNickname(int length) {
        if (length <= 0) {
            length = 8;
        }
        
        StringBuilder sb = new StringBuilder();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * 批量生成昵称
     */
    public static String[] generateBatchNicknames(int count, NicknameType type) {
        if (count <= 0) {
            count = 1;
        }
        
        String[] nicknames = new String[count];
        for (int i = 0; i < count; i++) {
            nicknames[i] = generateByType(type);
        }
        return nicknames;
    }
    
    private static String generateByType(NicknameType type) {
        switch (type) {
            case CHINESE: return generateChineseNickname();
            case ENGLISH: return generateEnglishNickname();
            case ENGLISH_WITH_DOT: return generateEnglishNicknameWithDot();
            case ENGLISH_WITH_UNDERSCORE: return generateEnglishNicknameWithUnderscore();
            case ANIMAL: return generateAnimalNickname();
            case NATURE: return generateNatureNickname();
            case WITH_NUMBERS: return generateNicknameWithNumbers();
            case SIMPLE_ENGLISH: return generateSimpleEnglishNickname();
            case RANDOM_ALPHABETIC: return generateRandomAlphabeticNickname(8);
            case RANDOM_ALPHANUMERIC: return generateRandomAlphanumericNickname(8);
            case RANDOM: return generateRandomNickname();
            default: return generateChineseNickname();
        }
    }
    
    /**
     * 昵称类型枚举
     */
    public enum NicknameType {
        CHINESE,            // 中文昵称
        ENGLISH,            // 英文昵称
        ENGLISH_WITH_DOT,   // 英文昵称（点分隔）
        ENGLISH_WITH_UNDERSCORE, // 英文昵称（下划线分隔）
        ANIMAL,             // 动物主题
        NATURE,             // 自然主题
        WITH_NUMBERS,       // 带数字
        SIMPLE_ENGLISH,     // 简洁英文
        RANDOM_ALPHABETIC,  // 随机字母
        RANDOM_ALPHANUMERIC, // 随机字母数字
        RANDOM              // 完全随机
    }
}