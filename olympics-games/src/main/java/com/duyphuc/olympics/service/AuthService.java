// src/main/java/com/duyphuc/olympics/service/AuthService.java
package com.duyphuc.olympics.service;

import com.duyphuc.olympics.dao.UserDAO;
import com.duyphuc.olympics.exception.AuthenticationException; // Đảm bảo import đúng
import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.util.PasswordHasher;

import java.sql.SQLException;
import java.util.HashMap;     // Thêm import
import java.util.Map;         // Thêm import
import java.util.Optional;

public class AuthService {

    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser;

    // Cấu hình cho việc khóa tài khoản
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_SECONDS = 30;
    private static final long LOCK_DURATION_MILLIS = LOCK_DURATION_SECONDS * 1000;

    // Lưu trữ số lần đăng nhập thất bại và thời điểm khóa
    private final Map<String, LoginAttemptInfo> loginAttempts = new HashMap<>();

    private static class LoginAttemptInfo {
        int failedAttempts;
        long lockReleaseTimeMillis;

        LoginAttemptInfo() {
            this.failedAttempts = 0;
            this.lockReleaseTimeMillis = 0;
        }
    }

    private AuthService() {
        this.userDAO = new UserDAO();
    }

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
     * @return The User object if login is successful.
     * @throws AuthenticationException if login fails due to invalid credentials,
     *                                 account lockout, or empty input.
     */
    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            // Ném exception thay vì chỉ in ra lỗi và trả về null
            throw new AuthenticationException("Tên đăng nhập và mật khẩu không được để trống.");
        }

        LoginAttemptInfo attemptInfo = loginAttempts.computeIfAbsent(username, k -> new LoginAttemptInfo());

        // 1. Kiểm tra tài khoản có đang bị khóa không
        if (attemptInfo.lockReleaseTimeMillis > System.currentTimeMillis()) {
            long remainingLockTime = (attemptInfo.lockReleaseTimeMillis - System.currentTimeMillis()) / 1000;
            throw new AuthenticationException(
                "Tài khoản '" + username + "' đang bị tạm khóa. Vui lòng thử lại sau " + (remainingLockTime > 0 ? remainingLockTime : 1) + " giây.",
                true, // accountLocked = true
                (remainingLockTime > 0 ? remainingLockTime : 1) * 1000 // lockDurationMillis
            );
        }

        // 2. Lấy thông tin người dùng
        Optional<User> userOpt = userDAO.getUserByUsernameOptional(username); // Giả sử UserDAO có phương thức này

        if (userOpt.isEmpty()) {
            System.err.println("AuthService: Tên đăng nhập '" + username + "' không tồn tại.");
            // Xử lý như một lần đăng nhập thất bại để tránh tiết lộ username có tồn tại hay không
            incrementFailedAttempts(username, attemptInfo, "Tên đăng nhập hoặc mật khẩu không đúng.");
            // Dòng trên sẽ ném exception, nên không cần return ở đây
            // Dòng dưới đây sẽ không bao giờ được thực thi nếu incrementFailedAttempts ném lỗi
            throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng."); // Fallback, không nên tới đây
        }

        User user = userOpt.get();

        // 3. Xác minh mật khẩu
        if (PasswordHasher.verifyPassword(password, user.getHashedPassword())) {
            resetFailedAttempts(username); // Đăng nhập thành công
            this.currentUser = user;
            System.out.println("AuthService: User '" + username + "' logged in successfully. Role: " + user.getRole());
            return user;
        } else {
            System.err.println("AuthService: Mật khẩu không đúng cho user '" + username + "'.");
            incrementFailedAttempts(username, attemptInfo, "Tên đăng nhập hoặc mật khẩu không đúng.");
            // Dòng trên sẽ ném exception, nên không cần return ở đây
            // Dòng dưới đây sẽ không bao giờ được thực thi nếu incrementFailedAttempts ném lỗi
            throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng."); // Fallback, không nên tới đây
        }
    }

    private void incrementFailedAttempts(String username, LoginAttemptInfo attemptInfo, String baseErrorMessage) throws AuthenticationException {
        attemptInfo.failedAttempts++;
        loginAttempts.put(username, attemptInfo);

        System.out.println("AuthService: Đăng nhập thất bại lần " + attemptInfo.failedAttempts + " cho user '" + username + "'.");

        if (attemptInfo.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            attemptInfo.lockReleaseTimeMillis = System.currentTimeMillis() + LOCK_DURATION_MILLIS;
            // attemptInfo.failedAttempts = 0; // Tùy chọn: reset sau khi khóa
            loginAttempts.put(username, attemptInfo);
            System.out.println("AuthService: Tài khoản '" + username + "' đã bị khóa trong " + LOCK_DURATION_SECONDS + " giây.");
            throw new AuthenticationException(
                "Bạn đã nhập sai quá " + MAX_FAILED_ATTEMPTS + " lần. Tài khoản '" + username + "' đã bị tạm khóa trong " + LOCK_DURATION_SECONDS + " giây.",
                true,
                LOCK_DURATION_MILLIS
            );
        } else {
            // Ném lỗi thông thường nếu chưa bị khóa
            throw new AuthenticationException(baseErrorMessage + " (Thử lại: " + attemptInfo.failedAttempts + "/" + MAX_FAILED_ATTEMPTS + ")");
        }
    }

    private void resetFailedAttempts(String username) {
        loginAttempts.remove(username);
        System.out.println("AuthService: Reset số lần đăng nhập thất bại cho user '" + username + "'.");
    }


    public void logout() {
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
        if (username == null || username.isEmpty() ||
            plainPassword == null || plainPassword.isEmpty() ||
            email == null || email.isEmpty() ||
            role == null || role.isEmpty()) {
            System.err.println("AuthService: Tất cả các trường thông tin đăng ký không được để trống.");
            return false; // Hoặc ném InvalidInputException
        }

        if (userDAO.getUserByUsernameOptional(username).isPresent()) {
            System.err.println("AuthService: Tên đăng nhập '" + username + "' đã tồn tại.");
            return false; // Hoặc ném DuplicateRecordException
        }

        String hashedPassword = PasswordHasher.hashPassword(plainPassword);
        if (hashedPassword == null) {
            System.err.println("AuthService: Lỗi khi băm mật khẩu.");
            return false; // Hoặc ném một lỗi nội bộ
        }

        User newUser = new User(); // Giả sử User có constructor mặc định và setters
        newUser.setUsername(username);
        newUser.setHashedPassword(hashedPassword);
        newUser.setEmail(email);
        newUser.setRole(role);

        boolean success = userDAO.addUser(newUser);
        if (success) {
            System.out.println("AuthService: User '" + username + "' registered successfully.");
            resetFailedAttempts(username); // Reset nếu user này đã từng thử login sai trước khi đăng ký
        } else {
            System.err.println("AuthService: Failed to register user '" + username + "'.");
        }
        return success;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        if (username == null || username.isEmpty() ||
            oldPassword == null || oldPassword.isEmpty() ||
            newPassword == null || newPassword.isEmpty()) {
            System.err.println("AuthService: Tên đăng nhập, mật khẩu cũ và mật khẩu mới không được để trống.");
            // Nên ném exception ở đây thay vì trả về false
            throw new AuthenticationException("Thông tin đổi mật khẩu không được để trống.");
        }

        Optional<User> userOpt = userDAO.getUserByUsernameOptional(username);
        if (userOpt.isEmpty()) {
            System.err.println("AuthService: Không tìm thấy người dùng '" + username + "' để đổi mật khẩu.");
            // Nên ném exception
            throw new AuthenticationException("Người dùng '" + username + "' không tồn tại.");
        }

        User user = userOpt.get();

        if (!PasswordHasher.verifyPassword(oldPassword, user.getHashedPassword())) {
            System.err.println("AuthService: Mật khẩu cũ không đúng cho người dùng '" + username + "'.");
            // Có thể KHÔNG tăng số lần login thất bại ở đây, vì đây là hành động đổi mật khẩu
            // Nhưng vẫn ném AuthenticationException để thông báo lỗi
            throw new AuthenticationException("Mật khẩu cũ không đúng.");
        }

        String newHashedPassword = PasswordHasher.hashPassword(newPassword);
        if (newHashedPassword == null) {
            System.err.println("AuthService: Lỗi khi băm mật khẩu mới.");
            // Ném exception cho lỗi nội bộ
            throw new RuntimeException("Lỗi hệ thống: Không thể băm mật khẩu mới.");
        }

        try {
            boolean success = userDAO.updateUserPassword(username, newHashedPassword);
            if (success) {
                System.out.println("AuthService: Đổi mật khẩu thành công cho người dùng '" + username + "'.");
                if (this.currentUser != null && this.currentUser.getUsername().equals(username)) {
                    this.currentUser.setHashedPassword(newHashedPassword);
                }
                resetFailedAttempts(username); // Reset số lần login thất bại sau khi đổi MK thành công
            } else {
                System.err.println("AuthService: Không thể cập nhật mật khẩu mới cho người dùng '" + username + "' trong CSDL.");
                // Có thể ném một DataAccessException ở đây
            }
            return success;
        } catch (SQLException e) {
            System.err.println("AuthService: Lỗi SQL khi cập nhật mật khẩu: " + e.getMessage());
            e.printStackTrace();
            // Ném một DataAccessException thay vì trả về false
            throw new RuntimeException("Lỗi cơ sở dữ liệu khi đổi mật khẩu.", e); // Ví dụ
        }
    }
}