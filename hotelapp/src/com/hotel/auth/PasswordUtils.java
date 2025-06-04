package com.hotel.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 密码工具类
 * 用于密码加密、验证、强度评估和生成盐值
 */
public class PasswordUtils {
    
    // 算法名称
    private static final String HASH_ALGORITHM = "SHA-256";
    
    // 盐值长度
    private static final int SALT_LENGTH = 16;
    
    // 密码强度级别
    public static final int STRENGTH_VERY_WEAK = 0;
    public static final int STRENGTH_WEAK = 1;
    public static final int STRENGTH_MEDIUM = 2;
    public static final int STRENGTH_STRONG = 3;
    public static final int STRENGTH_VERY_STRONG = 4;
    
    // 密码复杂度规则
    private static final Pattern HAS_NUMBER = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    
    /**
     * 生成随机盐值
     * @return 返回Base64编码的盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * 对密码进行加密
     * @param password 明文密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("密码和盐值不能为空");
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密算法不可用", e);
        }
    }
    
    /**
     * 验证密码
     * @param inputPassword 输入的明文密码
     * @param storedPassword 存储的加密密码
     * @param salt 盐值
     * @return 密码是否匹配
     */
    public static boolean verifyPassword(String inputPassword, String storedPassword, String salt) {
        if (inputPassword == null || storedPassword == null || salt == null) {
            return false;
        }
        
        String hashedInput = hashPassword(inputPassword, salt);
        return hashedInput.equals(storedPassword);
    }
    
    /**
     * 评估密码强度
     * @param password 要评估的密码
     * @return 密码强度级别 (0-4)
     */
    public static int evaluatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return STRENGTH_VERY_WEAK;
        }
        
        int score = 0;
        
        // 长度评分
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // 复杂度评分
        if (HAS_NUMBER.matcher(password).matches()) score++;
        if (HAS_LOWERCASE.matcher(password).matches()) score++;
        if (HAS_UPPERCASE.matcher(password).matches()) score++;
        if (HAS_SPECIAL_CHAR.matcher(password).matches()) score++;
        
        // 计算最终得分 (最高4分)
        return Math.min(STRENGTH_VERY_STRONG, score / 2);
    }
    
    /**
     * 获取密码强度描述
     * @param strength 密码强度级别
     * @return 密码强度描述
     */
    public static String getStrengthDescription(int strength) {
        switch (strength) {
            case STRENGTH_VERY_WEAK: return "非常弱";
            case STRENGTH_WEAK: return "弱";
            case STRENGTH_MEDIUM: return "中等";
            case STRENGTH_STRONG: return "强";
            case STRENGTH_VERY_STRONG: return "非常强";
            default: return "未知";
        }
    }
    
    /**
     * 验证密码是否满足复杂度要求
     * @param password 密码
     * @return 验证结果和失败原因
     */
    public static Map<String, Object> validatePasswordComplexity(String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        
        if (password == null || password.length() < 8) {
            result.put("valid", false);
            result.put("reason", "密码长度必须至少为8个字符");
            return result;
        }
        
        if (!HAS_NUMBER.matcher(password).matches()) {
            result.put("valid", false);
            result.put("reason", "密码必须包含至少一个数字");
            return result;
        }
        
        if (!HAS_LOWERCASE.matcher(password).matches() && !HAS_UPPERCASE.matcher(password).matches()) {
            result.put("valid", false);
            result.put("reason", "密码必须包含至少一个字母");
            return result;
        }
        
        return result;
    }
    
    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8; // 确保最小长度为8
        }
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // 定义字符类别
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*";
        
        // 确保每种字符至少有一个
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // 添加其余字符
        String allChars = lowerChars + upperChars + numbers + specialChars;
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // 打乱字符顺序
        char[] passwordChars = password.toString().toCharArray();
        for (int i = 0; i < passwordChars.length; i++) {
            int j = random.nextInt(passwordChars.length);
            char temp = passwordChars[i];
            passwordChars[i] = passwordChars[j];
            passwordChars[j] = temp;
        }
        
        return new String(passwordChars);
    }
    
    /**
     * 生成易记的随机密码（包含单词和数字）
     * @return 易记的随机密码
     */
    public static String generateMemorablePassword() {
        SecureRandom random = new SecureRandom();
        
        // 常用单词列表
        String[] words = {"apple", "book", "cat", "dog", "fish", "home", "king", "lion", "moon", "star"};
        
        // 选择两个随机单词
        String word1 = words[random.nextInt(words.length)];
        String word2 = words[random.nextInt(words.length)];
        
        // 首字母大写
        word1 = word1.substring(0, 1).toUpperCase() + word1.substring(1);
        
        // 添加2-4位随机数字
        int numbers = random.nextInt(900) + 100;
        
        // 添加一个特殊字符
        String specialChars = "!@#$%^&*";
        char specialChar = specialChars.charAt(random.nextInt(specialChars.length()));
        
        return word1 + numbers + specialChar + word2;
    }
} 