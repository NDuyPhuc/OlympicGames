package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.User;
// import com.duyphuc.olympicsgames.exception.DataAccessException; // Custom exception

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User getUserByUsername(String username) /*throws DataAccessException*/ {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;
        String sql = "SELECT id, username, hashed_password, email, role FROM Users WHERE username = ?";

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            if (conn == null) {
                // throw new DataAccessException("Không thể kết nối đến cơ sở dữ liệu.");
                System.err.println("Không thể kết nối đến cơ sở dữ liệu trong UserDAO.");
                return null;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setHashedPassword(rs.getString("hashed_password"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // throw new DataAccessException("Lỗi khi truy vấn người dùng: " + e.getMessage(), e);
        } finally {
            DBConnectionManager.close(null, pstmt, rs); // Không đóng conn ở đây vì nó là Singleton
        }
        return user;
    }

    public boolean addUser(User user) /*throws DataAccessException*/ {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO Users (username, hashed_password, email, role) VALUES (?, ?, ?, ?)";
        boolean success = false;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
             if (conn == null) {
                // throw new DataAccessException("Không thể kết nối đến cơ sở dữ liệu.");
                System.err.println("Không thể kết nối đến cơ sở dữ liệu trong UserDAO.");
                return false;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getHashedPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole());

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
        } catch (SQLException e) {
            e.printStackTrace();
             // throw new DataAccessException("Lỗi khi thêm người dùng: " + e.getMessage(), e);
        } finally {
            DBConnectionManager.close(null, pstmt, null);
        }
        return success;
    }
    // Thêm các phương thức khác nếu cần (updateUser, deleteUser, etc.)
}