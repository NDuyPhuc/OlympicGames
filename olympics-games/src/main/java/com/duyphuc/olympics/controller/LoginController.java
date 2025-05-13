package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.service.AuthService;
// import com.duyphuc.olympicsgames.exception.AuthenticationException;
// import com.duyphuc.olympicsgames.exception.DataAccessException;
import com.duyphuc.olympics.util.AlertUtil; 

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    private AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText(); // Không trim password

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Tên đăng nhập và mật khẩu không được để trống.");
            messageLabel.setVisible(true);
            return;
        }

        loginButton.setDisable(true);
        messageLabel.setText("Đang đăng nhập...");
        messageLabel.setVisible(true);

        try {
            User user = authService.login(username, password);
            if (user != null) {
                // Đăng nhập thành công, không cần hiển thị message ở đây nữa
                // messageLabel.setText("Đăng nhập thành công!");
                loadMainDashboard(event);
            } else {
                // AuthService đã xử lý và in lỗi, hoặc có thể throw exception
                // Nếu AuthService trả về null mà không throw exception:
                messageLabel.setText("Tên đăng nhập hoặc mật khẩu không đúng.");
                // Hoặc dùng AlertUtil
                // AlertUtil.showError("Lỗi Đăng Nhập", "Tên đăng nhập hoặc mật khẩu không đúng.");
            }
        // } catch (AuthenticationException e) {
        //     messageLabel.setText(e.getMessage());
        //     AlertUtil.showError("Lỗi Đăng Nhập", e.getMessage());
        // } catch (DataAccessException e) {
        //     messageLabel.setText("Lỗi kết nối CSDL. Vui lòng thử lại.");
        //     AlertUtil.showError("Lỗi Hệ Thống", "Lỗi kết nối cơ sở dữ liệu.");
        //     e.printStackTrace(); // Ghi log chi tiết
        } catch (Exception e) { // Bắt các lỗi không mong muốn khác
            messageLabel.setText("Đã xảy ra lỗi không xác định.");
            AlertUtil.showError("Lỗi Không Xác Định", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            loginButton.setDisable(false);
            if (AuthService.currentUser == null) { // Chỉ ẩn message nếu đăng nhập thất bại và không có lỗi nào khác
                 // messageLabel.setVisible(true); // Giữ lại message lỗi nếu có
            } else {
                messageLabel.setVisible(false); // Ẩn nếu đăng nhập thành công
            }
        }
    }

    private void loadMainDashboard(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/duyphuc/olympics/fxml/MainDashboardView.fxml")); // Đảm bảo đường dẫn đúng
            Parent root = loader.load();

            // Có thể lấy controller của MainDashboard để truyền dữ liệu nếu cần
            // MainDashboardController dashboardController = loader.getController();
            // dashboardController.initData(AuthService.getCurrentUser());

            Stage mainStage = new Stage();
            mainStage.setTitle("Olympic Games Medal Analyzer - Dashboard");
            mainStage.setScene(new Scene(root));
            mainStage.setMaximized(true); // Mở full màn hình
            mainStage.show();

            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Lỗi: Không thể tải màn hình chính.");
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể tải màn hình chính của ứng dụng.");
        }
    }
}