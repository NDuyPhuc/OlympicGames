package com.duyphuc.olympics.service;

import com.duyphuc.olympics.dao.UserDAO;
import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.util.PasswordHasher;


public class AuthService {
    private UserDAO userDAO;
    public static User currentUser; // Lưu người dùng đang đăng nhập

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User login(String username, String password) /*throws AuthenticationException, DataAccessException*/ {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            // throw new AuthenticationException("Tên đăng nhập và mật khẩu không được để trống.");
            System.err.println("Tên đăng nhập và mật khẩu không được để trống.");
            return null;
        }

        User user = userDAO.getUserByUsername(username);

        if (user == null) {
            // throw new AuthenticationException("Tên đăng nhập không tồn tại.");
             System.err.println("Tên đăng nhập không tồn tại.");
            return null;
        }

        if (PasswordHasher.verifyPassword(password, user.getHashedPassword())) {
            currentUser = user; // Lưu session người dùng
            return user;
        } else {
            // throw new AuthenticationException("Mật khẩu không đúng.");
            System.err.println("Mật khẩu không đúng.");
            return null;
        }
    }

    public void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

     public boolean registerUser(String username, String plainPassword, String email, String role) {
        // Kiểm tra username, email đã tồn tại chưa (nếu cần)
        if (userDAO.getUserByUsername(username) != null) {
            System.err.println("Tên đăng nhập đã tồn tại.");
            return false;
        }
        // (Tương tự kiểm tra email)

        String hashedPassword = PasswordHasher.hashPassword(plainPassword);
        User newUser = new User(username, hashedPassword, email, role);
        return userDAO.addUser(newUser);
    }
}