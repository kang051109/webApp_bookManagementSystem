package com.example.webapp_project.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Temporary utility - Generate admin123 的 BCrypt hash
 * Run in  IDEA right-click → Run 'GenerateAdminHash.main()' to run
 */
public class GenerateAdminHash {
    public static void main(String[] args) {
        String hash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        System.out.println("========================================");
        System.out.println("Password: admin123");
        System.out.println("hash: " + hash);
        System.out.println("========================================");
        System.out.println("把上面的hash值复制到 schema.sql to replace existing password_hash");
    }
}
