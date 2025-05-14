// src/main/java/com/duyphuc/olympics/exception/AuthenticationException.java
package com.duyphuc.olympics.exception;

public class AuthenticationException extends Exception {

    private boolean accountLocked; // Thêm thuộc tính để biết có phải lỗi do tài khoản bị khóa không
    private long lockDurationMillis; // Thời gian còn lại của việc khóa (nếu có)

    public AuthenticationException(String message) {
        super(message);
        this.accountLocked = false;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.accountLocked = false;
    }

    // Constructor cho trường hợp tài khoản bị khóa
    public AuthenticationException(String message, boolean accountLocked, long lockDurationMillis) {
        super(message);
        this.accountLocked = accountLocked;
        this.lockDurationMillis = lockDurationMillis;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public long getLockDurationMillis() {
        return lockDurationMillis;
    }
}