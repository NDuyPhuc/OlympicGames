package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional; // Thêm import này
import java.util.ArrayList; // Thêm import
import java.util.List;    // Thêm import

public class UserDAO {

    /**
     * Retrieves a user by username.
     * @param username The username to search for.
     * @return An Optional containing the User if found, or an empty Optional otherwise.
     */
    public Optional<User> getUserByUsernameOptional(String username) {
        String sql = "SELECT id, username, hashed_password, email, role FROM Users WHERE username = ?";
        // Sử dụng try-with-resources để đảm bảo connection và statement được đóng đúng cách
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) { // Cũng nên dùng try-with-resources cho ResultSet
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setHashedPassword(rs.getString("hashed_password"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO: Lỗi khi truy vấn người dùng '" + username + "': " + e.getMessage());
            e.printStackTrace(); // Quan trọng: log stack trace để debug
        }
        return Optional.empty(); // Trả về empty nếu không tìm thấy hoặc có lỗi
    }

    /**
     * Adds a new user to the database.
     * @param user The User object to add.
     * @return true if the user was added successfully, false otherwise.
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO Users (username, hashed_password, email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getHashedPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole());

            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        } catch (SQLException e) {
            System.err.println("UserDAO: Lỗi khi thêm người dùng '" + user.getUsername() + "': " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the password for a given user.
     * @param username The username of the user whose password is to be updated.
     * @param newHashedPassword The new hashed password.
     * @return true if the password was updated successfully, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean updateUserPassword(String username, String newHashedPassword) throws SQLException {
        String sql = "UPDATE Users SET hashed_password = ? WHERE username = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

           
            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        }
        // SQLException từ PreparedStatement hoặc executeUpdate sẽ được ném ra ngoài
    }

    // Phương thức getUserByUsername cũ (nếu bạn vẫn muốn giữ lại để tương thích)
    // Tuy nhiên, nên chuyển sang dùng Optional ở mọi nơi.
    public User getUserByUsername(String username) {
        return getUserByUsernameOptional(username).orElse(null);
    }

    // Các phương thức khác nếu cần (ví dụ: lấy tất cả user, update thông tin user, xóa user)
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email, role FROM Users ORDER BY username"; // Không lấy hashed_password trừ khi cần
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

         

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                // Không setHashedPassword ở đây để tránh lộ mật khẩu không cần thiết
                users.add(user);
            }
        }
        // SQLException sẽ được ném ra ngoài nếu có lỗi
        return users;
    }

    public boolean updateUser(User user) throws SQLException {
        // Quyết định xem có cho phép cập nhật mật khẩu qua phương thức này không,
        // hay tách riêng updateUserProfile và updateUserPassword.
        // Ở đây giả sử updateUser có thể cập nhật cả thông tin cơ bản và mật khẩu (nếu hashedPassword được set)
        String sql;
        if (user.getHashedPassword() != null && !user.getHashedPassword().isEmpty()) {
            // Nếu có hashedPassword mới (đã được hash ở controller)
            sql = "UPDATE Users SET email = ?, role = ?, hashed_password = ? WHERE id = ?";
        } else {
            // Chỉ cập nhật email và role
            sql = "UPDATE Users SET email = ?, role = ? WHERE id = ?";
        }

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

          
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getRole());
            if (user.getHashedPassword() != null && !user.getHashedPassword().isEmpty()) {
                pstmt.setString(3, user.getHashedPassword());
                pstmt.setInt(4, user.getId());
            } else {
                pstmt.setInt(3, user.getId());
            }

            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        }
    }


    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        }
    }
}