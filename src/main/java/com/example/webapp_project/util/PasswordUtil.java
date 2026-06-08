package com.example.webapp_project.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码加密工具类 - 使用 BCrypt 算法
 */
public class PasswordUtil {

    /**
     * 对明文密码进行加密
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 验证明文密码是否与哈希匹配
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
