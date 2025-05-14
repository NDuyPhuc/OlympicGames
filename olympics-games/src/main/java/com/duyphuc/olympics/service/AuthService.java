// src/main/java/com/duyphuc/olympics/service/AuthService.java
package com.duyphuc.olympics.service;

import com.duyphuc.olympics.dao.UserDAO;
import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.util.PasswordHasher;

import java.sql.SQLException; // Thêm import này
import java.util.Optional; // Thêm import này

public class AuthService {

    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public User login(String username, String password) {
        // ... (code hiện tại của bạn)
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            System.err.println("AuthService: Tên đăng nhập và mật khẩu không được để trống.");
            return null;
        }

        // Nên sử dụng Optional để xử lý null an toàn hơn từ DAO
        Optional<User> userOpt = userDAO.getUserByUsernameOptional(username); // Giả sử UserDAO có phương thức này

        if (userOpt.isEmpty()) {
            System.err.println("AuthService: Tên đăng nhập '" + username + "' không tồn tại.");
            return null;
        }

        User user = userOpt.get();

        if (PasswordHasher.verifyPassword(password, user.getHashedPassword())) {
            this.currentUser = user;
            System.out.println("AuthService: User '" + username + "' logged in successfully. Role: " + user.getRole());
            return user;
        } else {
            System.err.println("AuthService: Mật khẩu không đúng cho user '" + username + "'.");
            this.currentUser = null;
            return null;
        }
    }

    public void logout() {
        // ... (code hiện tại của bạn)
        if (this.currentUser != null) {
            System.out.println("AuthService: User '" + this.currentUser.getUsername() + "' logged out.");
        } else {
            System.out.println("AuthService: No user was logged in to log out.");
        }
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public boolean registerUser(String username, String plainPassword, String email, String role) {
        // ... (code hiện tại của bạn)
        if (username == null || username.isEmpty() ||
            plainPassword == null || plainPassword.isEmpty() ||
            email == null || email.isEmpty() ||
            role == null || role.isEmpty()) {
            System.err.println("AuthService: Tất cả các trường thông tin đăng ký không được để trống.");
            return false;
        }

        if (userDAO.getUserByUsernameOptional(username).isPresent()) { // Sử dụng Optional
            System.err.println("AuthService: Tên đăng nhập '" + username + "' đã tồn tại.");
            return false;
        }

        String hashedPassword = PasswordHasher.hashPassword(plainPassword);
        if (hashedPassword == null) {
            System.err.println("AuthService: Lỗi khi băm mật khẩu.");
            return false;
        }
        // Đảm bảo User model có constructor phù hợp hoặc setter
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setHashedPassword(hashedPassword);
        newUser.setEmail(email);
        newUser.setRole(role);

        boolean success = userDAO.addUser(newUser);
        if (success) {
            System.out.println("AuthService: User '" + username + "' registered successfully.");
        } else {
            System.err.println("AuthService: Failed to register user '" + username + "'.");
        }
        return success;
    }

    /**
     * Changes the password for a given user.
     * @param username The username of the user.
     * @param oldPassword The current plain text password.
     * @param newPassword The new plain text password.
     * @return true if the password was changed successfully, false otherwise.
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || username.isEmpty() ||
            oldPassword == null || oldPassword.isEmpty() ||
            newPassword == null || newPassword.isEmpty()) {
            System.err.println("AuthService: Tên đăng nhập, mật khẩu cũ và mật khẩu mới không được để trống.");
            return false;
        }

        // Lấy thông tin người dùng
        Optional<User> userOpt = userDAO.getUserByUsernameOptional(username); // Giả sử UserDAO có phương thức này
        if (userOpt.isEmpty()) {
            System.err.println("AuthService: Không tìm thấy người dùng '" + username + "' để đổi mật khẩu.");
            return false;
        }

        User user = userOpt.get();

        // Xác minh mật khẩu cũ
        if (!PasswordHasher.verifyPassword(oldPassword, user.getHashedPassword())) {
            System.err.println("AuthService: Mật khẩu cũ không đúng cho người dùng '" + username + "'.");
            return false;
        }

        // Băm mật khẩu mới
        String newHashedPassword = PasswordHasher.hashPassword(newPassword);
        if (newHashedPassword == null) {
            System.err.println("AuthService: Lỗi khi băm mật khẩu mới.");
            return false;
        }

        // Cập nhật mật khẩu trong CSDL
        try {
            boolean success = userDAO.updateUserPassword(username, newHashedPassword); // UserDAO cần phương thức này
            if (success) {
                System.out.println("AuthService: Đổi mật khẩu thành công cho người dùng '" + username + "'.");
                // Nếu người dùng đang đổi mật khẩu là người dùng hiện tại, cập nhật lại hashedPassword trong currentUser
                if (this.currentUser != null && this.currentUser.getUsername().equals(username)) {
                    this.currentUser.setHashedPassword(newHashedPassword);
                }
            } else {
                System.err.println("AuthService: Không thể cập nhật mật khẩu mới cho người dùng '" + username + "' trong CSDL.");
            }
            return success;
        } catch (SQLException e) {
            System.err.println("AuthService: Lỗi SQL khi cập nhật mật khẩu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}