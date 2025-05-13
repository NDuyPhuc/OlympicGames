package com.duyphuc.olympics.model;

public class User {
    private int id;
    private String username;
    private String hashedPassword; // Lưu trữ mật khẩu đã băm
    private String email;
    private String role; // Ví dụ: "ADMIN", "STAFF"

    // Constructors
    public User() {
    }

    public User(String username, String hashedPassword, String email, String role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.role = role;
    }

    public User(int id, String username, String hashedPassword, String email, String role) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", role='" + role + '\'' +
               '}';
    }
}
