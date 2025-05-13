// src/main/java/com/duyphuc/olympics/controller/LoginController.java
package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.service.AuthService;
import com.duyphuc.olympics.util.AlertUtil;
import com.duyphuc.olympics.util.FxmlLoaderUtil; // THÊM IMPORT NÀY

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader; // Không cần import FXMLLoader trực tiếp nếu dùng FxmlLoaderUtil
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;

    private AuthService authService; // Giữ nguyên khai báo

    // Constructor không còn cần thiết nếu bạn khởi tạo authService trong initialize
    // Hoặc bạn có thể khởi tạo nó ngay tại đây nếu muốn
     public LoginController() {
        this.authService = AuthService.getInstance();
     }

    @FXML
    public void initialize() { // THÊM PHƯƠNG THỨC NÀY HOẶC KHỞI TẠO TRONG CONSTRUCTOR
        this.authService = AuthService.getInstance();
        messageLabel.setVisible(false); // Ẩn message ban đầu
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Tên đăng nhập và mật khẩu không được để trống.");
            messageLabel.setVisible(true);
            return;
        }

        loginButton.setDisable(true);
        messageLabel.setText("Đang đăng nhập...");
        messageLabel.setVisible(true);

        User user = null; // Khởi tạo user để kiểm tra sau finally
        try {
            user = authService.login(username, password); // authService đã là instance
            if (user != null) {
                // Đăng nhập thành công
                messageLabel.setVisible(false); // Ẩn message "Đang đăng nhập"
                loadMainDashboard(event, user); // Truyền user vào để Dashboard có thể sử dụng
            } else {
                // AuthService đã in lỗi ra console
                messageLabel.setText("Tên đăng nhập hoặc mật khẩu không đúng.");
                // AlertUtil.showError("Lỗi Đăng Nhập", "Tên đăng nhập hoặc mật khẩu không đúng.");
            }
        } catch (Exception e) { // Bắt các lỗi không mong muốn khác
            messageLabel.setText("Đã xảy ra lỗi không xác định khi đăng nhập.");
            AlertUtil.showError("Lỗi Không Xác Định", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            loginButton.setDisable(false);
            // Chỉ hiển thị messageLabel nếu đăng nhập thất bại
            if (user == null) { // Nếu user vẫn là null (đăng nhập thất bại)
                messageLabel.setVisible(true);
            } else {
                 messageLabel.setVisible(false); // Ẩn nếu đăng nhập thành công
            }
        }
    }

    private void loadMainDashboard(ActionEvent event, User loggedInUser) { // Thêm tham số User
        try {
            Stage currentStage = (Stage) ((Node)event.getSource()).getScene().getWindow();

            // Sử dụng FxmlLoaderUtil để tải và có thể lấy controller
            // FXMLLoader loader = FxmlLoaderUtil.getLoader("/com/duyphuc/olympics/fxml/MainDashboardView.fxml");
            // Parent root = loader.load();
            // MainDashboardController dashboardController = loader.getController();
            // dashboardController.setCurrentUser(loggedInUser); // Giả sử có phương thức này để init data

            // Hoặc tải trực tiếp nếu MainDashboardController tự lấy user từ AuthService
            Parent root = FxmlLoaderUtil.loadFXML("/com/duyphuc/olympics/fxml/MainDashboardView.fxml");


            Stage mainStage = new Stage();
            mainStage.setTitle("Olympic Games Medal Analyzer - Dashboard");
            mainStage.setScene(new Scene(root));
            mainStage.setMaximized(true);
            mainStage.show();

            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Lỗi: Không thể tải màn hình chính.");
            messageLabel.setVisible(true); // Hiển thị lỗi này
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể tải màn hình chính của ứng dụng.");
        }
    }
}