package com.example.webapp_project.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 临时工具类 - 生成 admin123 的 BCrypt 哈希
 * 在 IDEA 中右键 → Run 'GenerateAdminHash.main()' 即可运行
 */
public class GenerateAdminHash {
    public static void main(String[] args) {
        String hash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        System.out.println("========================================");
        System.out.println("密码: admin123");
        System.out.println("哈希: " + hash);
        System.out.println("========================================");
        System.out.println("把上面的哈希值复制到 schema.sql 中替换现有的 password_hash");
    }
}
