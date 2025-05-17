package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO { // Thêm implements

    @Override
    public Optional<User> getUserByUsernameOptional(String username) {
        String sql = "SELECT id, username, hashed_password, email, role FROM Users WHERE username = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
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
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean addUser(User user) throws SQLException { // Thêm throws SQLException
        String sql = "INSERT INTO Users (username, hashed_password, email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getHashedPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole());
            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        } // SQLException sẽ được ném ra ngoài nếu có lỗi
    }

    @Override
    public boolean updateUserPassword(String username, String newHashedPassword) throws SQLException {
        String sql = "UPDATE Users SET hashed_password = ? WHERE username = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        }
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email, role FROM Users ORDER BY username";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        String sql;
        if (user.getHashedPassword() != null && !user.getHashedPassword().isEmpty()) {
            sql = "UPDATE Users SET email = ?, role = ?, hashed_password = ? WHERE id = ?";
        } else {
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

    @Override
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        }
    }
    // Phương thức getUserByUsername(String) đã được cung cấp default trong Interface
}