package com.duyphuc.olympics.controller;


import com.duyphuc.olympics.service.AuthService;
import com.duyphuc.olympics.util.AlertUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainDashboardController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuItem manageMedalsMenuItem;
    @FXML private MenuItem viewChartsMenuItem;
    @FXML private Menu adminMenu; // Để ẩn/hiện menu admin
    @FXML private MenuItem manageUsersMenuItem;

    private AuthService authService; // Không cần new ở đây nếu AuthService.currentUser là static

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.authService = new AuthService(); // Khởi tạo nếu cần
        if (AuthService.getCurrentUser() != null) {
            welcomeLabel.setText("Chào mừng, " + AuthService.getCurrentUser().getUsername() + " (" + AuthService.getCurrentUser().getRole() + ")!");
            // Ẩn/hiện menu Admin dựa trên vai trò
            if (!"ADMIN".equalsIgnoreCase(AuthService.getCurrentUser().getRole())) {
                adminMenu.setVisible(false);
            }
        } else {
            // Trường hợp này không nên xảy ra nếu đã đăng nhập thành công
            welcomeLabel.setText("Lỗi: Không tìm thấy thông tin người dùng.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        authService.logout();
        try {
            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/duyphuc/olympics/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Olympic Analyzer - Đăng nhập");
            loginStage.setScene(new Scene(root, 450, 350));
            loginStage.setResizable(false);
            loginStage.show();

            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi", "Không thể quay lại màn hình đăng nhập.");
        }
    }

    @FXML
    private void handleManageMedals(ActionEvent event) {
        openModalWindow("/com/duyphuc/olympics/fxml/MedalManagementView.fxml", "Quản lý Huy Chương Olympic", event);
    }

    @FXML
    private void handleViewCharts(ActionEvent event) {
        // Tạo file ChartView.fxml và ChartController.java sau
        openModalWindow("/com/duyphuc/olympics/fxml/ChartView.fxml", "Xem Biểu đồ Thống kê", event);
    }

    @FXML
    private void handleShowProfile(ActionEvent event) {
        // Tạo file UserProfileView.fxml và UserProfileController.java sau
        openModalWindow("/com/duyphuc/olympicsgames/fxml/UserProfileView.fxml", "Thông tin cá nhân", event);
    }

    @FXML
    private void handleManageUsers(ActionEvent event) {
        // Tạo file AdminUserManagementView.fxml và AdminUserManagementController.java sau (Extra feature)
        if ("ADMIN".equalsIgnoreCase(AuthService.getCurrentUser().getRole())) {
             openModalWindow("/com/duyphuc/olympics/fxml/AdminUserManagementView.fxml", "Quản lý Người dùng", event);
        } else {
            AlertUtil.showWarning("Truy cập bị từ chối", "Bạn không có quyền truy cập chức năng này.");
        }
    }


    private void openModalWindow(String fxmlPath, String title, ActionEvent originatingEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent viewRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(viewRoot));
            stage.initModality(Modality.APPLICATION_MODAL); // Block các cửa sổ khác khi mở

            // Lấy Stage cha (cửa sổ dashboard) để đặt owner, giúp cửa sổ con luôn ở trên
            if (originatingEvent != null && originatingEvent.getSource() instanceof Node) {
                stage.initOwner(((Node) originatingEvent.getSource()).getScene().getWindow());
            } else if (welcomeLabel != null && welcomeLabel.getScene() != null) { // Fallback
                 stage.initOwner(welcomeLabel.getScene().getWindow());
            }

            stage.showAndWait(); // Chờ đến khi cửa sổ này đóng
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi", "Không thể mở cửa sổ: " + title);
        }
    }
}
