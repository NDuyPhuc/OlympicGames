package com.duyphuc.olympics.db;
//DBConnectionManager.java

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
// import java.util.Properties; // Cho việc đọc từ file properties

public class DBConnectionManager {
    // Thông tin kết nối - nên đọc từ file properties để bảo mật và dễ cấu hình
    private static final String DB_URL = "jdbc:mysql://localhost:3306/olympicgames"; // Thay nếu cần
    private static final String DB_USER = "root"; // Thay bằng user của bạn
    private static final String DB_PASSWORD = "123456"; // Thay bằng password của bạn

    private static DBConnectionManager instance;
    private Connection connection;

    private DBConnectionManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            if (this.connection == null) { // Kiểm tra thêm
                 throw new SQLException("Không thể thiết lập kết nối CSDL, connection là null.");
            }
            System.out.println("Kết nối CSDL thành công!"); // Thêm log để biết kết nối thành công
        } catch (SQLException e) {
            System.err.println("SQL Exception khi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
            // QUAN TRỌNG: Báo lỗi nghiêm trọng nếu không kết nối được
            throw new RuntimeException("Không thể khởi tạo DBConnectionManager: Lỗi kết nối CSDL.", e);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver không tìm thấy: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể khởi tạo DBConnectionManager: Driver không tìm thấy.", e);
        }
    }

    public static synchronized DBConnectionManager getInstance() {
        if (instance == null || instance.isConnectionClosed()) {
            instance = new DBConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() {
         // Kiểm tra xem kết nối có còn hợp lệ không trước khi trả về
        try {
            if (connection == null || connection.isClosed()) {
                // Thử kết nối lại nếu cần
                instance = new DBConnectionManager();
                return instance.connection;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Thử kết nối lại
            instance = new DBConnectionManager();
            return instance.connection;
        }
        return connection;
    }

    private boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true; // Giả sử là đóng nếu có lỗi khi kiểm tra
        }
    }

    // Phương thức tiện ích để đóng tài nguyên
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) { e.printStackTrace(); }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
        // Không nên đóng connection ở đây nếu nó là Singleton và được quản lý chung
        // Chỉ đóng connection khi ứng dụng thoát hoặc không cần nữa.
        // try {
        // if (conn != null) conn.close();
        // } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void close(Statement stmt, ResultSet rs) {
        close(null, stmt, rs);
    }

    public static void close(Connection conn) {
         try {
            if (conn != null && !conn.isClosed()) {
                // conn.close(); // Chỉ đóng nếu connection này không phải là connection dùng chung của Singleton
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static synchronized void closeInstanceConnection() {
        if (instance != null && instance.connection != null) {
            try {
                if (!instance.connection.isClosed()) {
                    instance.connection.close();
                    System.out.println("Kết nối CSDL đã được đóng.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            instance = null; // Đặt lại instance để có thể tạo mới nếu cần
        }
    }
}
