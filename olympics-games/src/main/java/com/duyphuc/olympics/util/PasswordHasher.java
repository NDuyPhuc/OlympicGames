package com.duyphuc.olympics.util;
//PasswordHasher.java
import org.apache.commons.codec.digest.DigestUtils;

public class PasswordHasher {

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null; // Hoặc throw exception
        }
        return DigestUtils.sha256Hex(plainPassword);
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }
        String newHash = DigestUtils.sha256Hex(plainPassword);
        return newHash.equals(hashedPassword);
    }
}