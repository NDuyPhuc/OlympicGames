package com.duyphuc.olympics.service;

import org.apache.commons.codec.digest.DigestUtils;

public class test { // Đặt tên khác để tránh nhầm lẫn
    public static void main(String[] args) {
        String plainPassword = "admin123";
        String hashedPassword = DigestUtils.sha256Hex(plainPassword);
        System.out.println("Hash SHA-256 cho 'admin123' là: " + hashedPassword);
        // Output sẽ là: a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3
    }
}