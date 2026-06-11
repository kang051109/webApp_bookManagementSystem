package com.example.webapp_project.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordPassword utility - Using BCrypt algorithm
 */
public class PasswordUtil {

    /**
     * Hash plaintextPassword
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verify plaintextPassword是否与hash匹配
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
