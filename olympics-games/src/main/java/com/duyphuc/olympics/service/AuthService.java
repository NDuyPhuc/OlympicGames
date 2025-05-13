// src/main/java/com/duyphuc/olympics/service/AuthService.java
package com.duyphuc.olympics.service;

import com.duyphuc.olympics.dao.UserDAO;
import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.util.PasswordHasher;

public class AuthService {

    private static AuthService instance; // For Singleton pattern
    private final UserDAO userDAO;
    private User currentUser; // Lưu người dùng đang đăng nhập (instance variable)

    // Private constructor to prevent instantiation from outside
    private AuthService() {
        this.userDAO = new UserDAO(); // Khởi tạo UserDAO
    }

    // Static method to get the single instance of AuthService
    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Attempts to log in a user.
     * @param username The username.
     * @param password The plain text password.
     * @return The User object if login is successful, null otherwise.
     */
    public User login(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            System.err.println("AuthService: Tên đăng nhập và mật khẩu không được để trống.");
            return null;
        }

        User user = userDAO.getUserByUsername(username);

        if (user == null) {
            System.err.println("AuthService: Tên đăng nhập '" + username + "' không tồn tại.");
            return null;
        }

        // Giả sử PasswordHasher.verifyPassword hoạt động đúng
        if (PasswordHasher.verifyPassword(password, user.getHashedPassword())) {
            this.currentUser = user; // Lưu session người dùng vào instance
            System.out.println("AuthService: User '" + username + "' logged in successfully. Role: " + user.getRole());
            return user;
        } else {
            System.err.println("AuthService: Mật khẩu không đúng cho user '" + username + "'.");
            this.currentUser = null; // Đảm bảo currentUser là null nếu đăng nhập thất bại
            return null;
        }
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        if (this.currentUser != null) {
            System.out.println("AuthService: User '" + this.currentUser.getUsername() + "' logged out.");
        } else {
            System.out.println("AuthService: No user was logged in to log out.");
        }
        this.currentUser = null;
    }

    /**
     * Gets the currently logged-in user.
     * @return The User object if a user is logged in, null otherwise.
     */
    public User getCurrentUser() {
        return this.currentUser; // Trả về currentUser của instance
    }

    /**
     * Registers a new user.
     * @param username The username.
     * @param plainPassword The plain text password.
     * @param email The email address.
     * @param role The role of the user.
     * @return true if registration is successful, false otherwise.
     */
    public boolean registerUser(String username, String plainPassword, String email, String role) {
        if (username == null || username.isEmpty() ||
            plainPassword == null || plainPassword.isEmpty() ||
            email == null || email.isEmpty() ||
            role == null || role.isEmpty()) {
            System.err.println("AuthService: Tất cả các trường thông tin đăng ký không được để trống.");
            return false;
        }

        // Kiểm tra username đã tồn tại chưa
        if (userDAO.getUserByUsername(username) != null) {
            System.err.println("AuthService: Tên đăng nhập '" + username + "' đã tồn tại.");
            return false;
        }

        // (Tùy chọn) Kiểm tra email đã tồn tại chưa (nếu UserDAO có phương thức getUserByEmail)
        // if (userDAO.getUserByEmail(email) != null) {
        //     System.err.println("AuthService: Email '" + email + "' đã được sử dụng.");
        //     return false;
        // }

        // (Tùy chọn) Validate định dạng email
        // if (!isValidEmail(email)) {
        //    System.err.println("AuthService: Định dạng email không hợp lệ.");
        //    return false;
        // }

        String hashedPassword = PasswordHasher.hashPassword(plainPassword);
        if (hashedPassword == null) {
            System.err.println("AuthService: Lỗi khi băm mật khẩu.");
            return false;
        }

        User newUser = new User(username, hashedPassword, email, role); // Giả sử User có constructor này
        boolean success = userDAO.addUser(newUser);
        if (success) {
            System.out.println("AuthService: User '" + username + "' registered successfully.");
        } else {
            System.err.println("AuthService: Failed to register user '" + username + "'.");
        }
        return success;
    }

    // (Tùy chọn) Phương thức kiểm tra định dạng email đơn giản
    // private boolean isValidEmail(String email) {
    //     String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    //     return email.matches(emailRegex);
    // }
}