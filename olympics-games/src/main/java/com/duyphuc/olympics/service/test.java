package com.duyphuc.olympics.service;

import org.apache.commons.codec.digest.DigestUtils;

public class test { // Đặt tên khác để tránh nhầm lẫn
    public static void main(String[] args) {
        String plainPassword = "admin123";
        String hashedPassword = DigestUtils.sha256Hex(plainPassword);
        System.out.println("Hash SHA-256 cho 'admin123' là: " + hashedPassword);
        // Output sẽ là: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
    }
}