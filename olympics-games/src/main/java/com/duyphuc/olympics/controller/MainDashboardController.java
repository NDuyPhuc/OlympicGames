// src/main/java/com/duyphuc/olympics/controller/MainDashboardController.java
package com.duyphuc.olympics.controller; // Đảm bảo package name đúng

import com.duyphuc.olympics.MainApp; // Giả sử MainApp để quay lại login
import com.duyphuc.olympics.service.AuthService; // Giả sử AuthService để lấy thông tin user
import com.duyphuc.olympics.model.User;     // Giả sử model User
import com.duyphuc.olympics.util.AlertUtil;
import com.duyphuc.olympics.util.FxmlLoaderUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class MainDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private MenuItem profileMenuItem; // Không nhất thiết phải có fx:id nếu chỉ dùng onAction

    @FXML
    private MenuItem logoutMenuItem;  // Không nhất thiết phải có fx:id

    @FXML
    private MenuItem manageMedalsMenuItem; // fx:id đã có là manageMedalsMenuItem

    @FXML
    private MenuItem viewChartsMenuItem;

    @FXML
    private Menu adminMenu; // Để có thể ẩn/hiện menu này

    @FXML
    private MenuItem manageUsersMenuItem;

    private AuthService authService; // Nên được inject hoặc lấy instance

    @FXML
    public void initialize() {
        authService = AuthService.getInstance(); // Giả sử AuthService là Singleton
        User currentUser = authService.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText("Chào mừng, " + currentUser.getUsername() + "!");
            if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                adminMenu.setVisible(false);
                // adminMenu.setManaged(false); // XÓA HOẶC COMMENT DÒNG NÀY
            } else {
                adminMenu.setVisible(true); // Đảm bảo admin thấy menu
            }
        } else {
            welcomeLabel.setText("Chào mừng!");
            adminMenu.setVisible(false); // Nếu không có user, cũng ẩn admin menu
        }
    }

    @FXML
    void handleShowProfile(ActionEvent event) {
        System.out.println("Show Profile action triggered.");
        // TODO: Mở FXML cho User Profile
        try {
            Stage profileStage = new Stage();
            Parent profileRoot = FxmlLoaderUtil.loadFXML("/com/duyphuc/olympics/fxml/UserProfileView.fxml"); // Cập nhật đường dẫn nếu cần
            profileStage.setTitle("Thông tin cá nhân");
            profileStage.setScene(new Scene(profileRoot));
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.initOwner(welcomeLabel.getScene().getWindow()); // Gắn owner
            profileStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể mở màn hình thông tin cá nhân.");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("Logout action triggered.");
        if (authService != null) {
            authService.logout();
        }

        // Lấy stage hiện tại và đóng nó
        Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
        currentStage.close();

        // Mở lại màn hình đăng nhập
        try {
            // Sử dụng MainApp để khởi động lại (nếu có) hoặc tạo stage mới
            // Ví dụ nếu MainApp có phương thức startLoginScreen(Stage stage)
            // MainApp mainAppInstance = new MainApp();
            // mainAppInstance.startLoginScreen(new Stage());

            // Hoặc tạo trực tiếp stage mới
            Stage loginStage = new Stage();
            Parent loginRoot = FxmlLoaderUtil.loadFXML("/com/duyphuc/olympics/fxml/LoginView.fxml");
            loginStage.setTitle("Olympic Games Analyzer - Đăng nhập");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Đăng Xuất", "Không thể quay lại màn hình đăng nhập.");
        }
    }

    @FXML
    void handleManageMedals(ActionEvent event) { // Tên phương thức khớp với onAction
        System.out.println("Manage Medals action triggered.");
        try {
            Stage medalStage = new Stage();
            // Đảm bảo đường dẫn FXML chính xác với cấu trúc thư mục của bạn
            Parent medalRoot = FxmlLoaderUtil.loadFXML("/com/duyphuc/olympics/fxml/MedalManagementView.fxml");
            medalStage.setTitle("Quản lý Huy chương");
            medalStage.setScene(new Scene(medalRoot));
            medalStage.initModality(Modality.APPLICATION_MODAL);
            // Gắn owner để dialog modal hoạt động đúng cách và focus được trả về
            medalStage.initOwner(welcomeLabel.getScene().getWindow());
            medalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể mở màn hình quản lý huy chương.");
        }
    }

    @FXML
    void handleViewCharts(ActionEvent event) {
        System.out.println("View Charts action triggered.");
        // TODO: Mở FXML cho Chart View
        try {
            Stage chartStage = new Stage();
            Parent chartRoot = FxmlLoaderUtil.loadFXML("/com/duyphuc/olympics/fxml/ChartView.fxml"); // Cập nhật đường dẫn nếu cần
            chartStage.setTitle("Thống kê Biểu đồ");
            chartStage.setScene(new Scene(chartRoot));
            chartStage.initModality(Modality.APPLICATION_MODAL);
            chartStage.initOwner(welcomeLabel.getScene().getWindow());
            chartStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể mở màn hình biểu đồ.");
        }
    }

    @FXML
    void handleManageUsers(ActionEvent event) {
        System.out.println("Manage Users action triggered.");
        // TODO: Mở FXML cho Admin User Management
        try {
            Stage userManagementStage = new Stage();
            Parent userManagementRoot = FxmlLoaderUtil.loadFXML("/com/duyphuc/olympics/fxml/AdminUserManagementView.fxml"); // Cập nhật đường dẫn
            userManagementStage.setTitle("Quản lý Người dùng");
            userManagementStage.setScene(new Scene(userManagementRoot));
            userManagementStage.initModality(Modality.APPLICATION_MODAL);
            userManagementStage.initOwner(welcomeLabel.getScene().getWindow());
            userManagementStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể mở màn hình quản lý người dùng.");
        }
    }
}