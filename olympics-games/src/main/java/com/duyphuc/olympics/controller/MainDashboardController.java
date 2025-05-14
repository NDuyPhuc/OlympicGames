package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.MainApp; // Giả sử MainApp để quay lại login
import com.duyphuc.olympics.service.AuthService; // Giả sử AuthService để lấy thông tin user
import com.duyphuc.olympics.model.User;     // Giả sử model User
import com.duyphuc.olympics.util.AlertUtil;
import com.duyphuc.olympics.util.FxmlLoaderUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainDashboardController {

    @FXML private BorderPane mainDashboardPane; // Tham chiếu đến BorderPane gốc
    @FXML private BorderPane contentArea;       // Khu vực trung tâm để tải các view
    @FXML private Label welcomeLabelInInitialView; // Đổi tên để phân biệt với welcomeLabel động
    @FXML private Menu adminMenu;
    @FXML private MenuItem dashboardHomeMenuItem;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuItem manageMedalsMenuItem;
    @FXML private MenuItem viewChartsMenuItem;
    @FXML private MenuItem manageUsersMenuItem;

    private Node initialWelcomeContent; // Để lưu trữ VBox chào mừng ban đầu
    private AuthService authService;

    @FXML
    public void initialize() {
        authService = AuthService.getInstance(); // Giả sử AuthService là Singleton

        // Lưu lại nội dung chào mừng ban đầu
        if (contentArea.getCenter() != null) {
            initialWelcomeContent = contentArea.getCenter();
            // Tìm Label chào mừng bên trong initialWelcomeContent nếu nó được định nghĩa trong FXML
            if (initialWelcomeContent instanceof VBox) {
                VBox initialVBox = (VBox) initialWelcomeContent;
                for (Node node : initialVBox.getChildren()) {
                    if (node instanceof Label && "welcomeLabel".equals(node.getId())) { // Kiểm tra fx:id của Label trong VBox chào mừng
                        this.welcomeLabelInInitialView = (Label) node;
                        break;
                    }
                }
            }
        } else {
            // Nếu không có gì trong FXML, tạo nội dung chào mừng mặc định
            createAndSetDefaultWelcomeContent();
        }

        updateWelcomeMessage(); // Cập nhật thông điệp chào mừng
        configureAdminMenu(); // Cấu hình menu admin
    }

    private void createAndSetDefaultWelcomeContent() {
        VBox welcomeBox = new VBox();
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setSpacing(20.0);
        // Tạo Label động mới cho welcome message
        this.welcomeLabelInInitialView = new Label();
        this.welcomeLabelInInitialView.setId("welcomeLabel"); // Đặt fx:id nếu bạn muốn tham chiếu sau này
        this.welcomeLabelInInitialView.setStyle("-fx-font-size: 24px;");

        Label instructionLabel = new Label("Chọn một chức năng từ thanh menu.");
        welcomeBox.getChildren().addAll(this.welcomeLabelInInitialView, instructionLabel);
        welcomeBox.setPadding(new Insets(20.0));
        initialWelcomeContent = welcomeBox;
        contentArea.setCenter(initialWelcomeContent);
    }

    private void updateWelcomeMessage() {
        User currentUser = authService.getCurrentUser();
        String welcomeText = "Chào mừng!";
        if (currentUser != null) {
            welcomeText = "Chào mừng, " + currentUser.getUsername() + "!";
        }

        if (welcomeLabelInInitialView != null) {
            welcomeLabelInInitialView.setText(welcomeText);
        } else if (initialWelcomeContent instanceof VBox) {
            // Cố gắng tìm và cập nhật label nếu welcomeLabelInInitialView chưa được gán đúng
            VBox vb = (VBox) initialWelcomeContent;
            if (!vb.getChildren().isEmpty() && vb.getChildren().get(0) instanceof Label) {
                 ((Label) vb.getChildren().get(0)).setText(welcomeText);
            }
        }
    }

    private void configureAdminMenu() {
        User currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        adminMenu.setVisible(isAdmin);
    }

    private void loadViewIntoContentArea(String fxmlPath) {
        try {
            Node view = FxmlLoaderUtil.loadFXML(fxmlPath);
            contentArea.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể tải giao diện: " + fxmlPath + "\nLỗi: " + e.getMessage());
            // Có thể quay lại màn hình chính nếu tải lỗi
            handleShowDashboardHome(null);
        }
    }

    @FXML
    void handleShowDashboardHome(ActionEvent event) { // Thêm ActionEvent cho FXML
        if (initialWelcomeContent != null) {
            contentArea.setCenter(initialWelcomeContent);
            updateWelcomeMessage(); // Cập nhật lại welcome text
        } else {
            createAndSetDefaultWelcomeContent();
            updateWelcomeMessage();
        }
    }

    @FXML
    void handleShowProfile(ActionEvent event) {
        try {
            // Sử dụng FXMLLoader để có thể truyền dữ liệu cho controller nếu cần
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/duyphuc/olympics/fxml/UserProfileView.fxml"));
            Parent profileRoot = loader.load();

            // Nếu UserProfileController cần dữ liệu người dùng:
            // UserProfileController controller = loader.getController();
            // controller.initData(authService.getCurrentUser());

            Stage profileStage = new Stage();
            profileStage.setTitle("Thông tin cá nhân");
            profileStage.setScene(new Scene(profileRoot));
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.initOwner(mainDashboardPane.getScene().getWindow()); // Gắn owner
            profileStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể mở màn hình thông tin cá nhân.");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        if (authService != null) {
            authService.logout();
        }
        Stage currentStage = (Stage) mainDashboardPane.getScene().getWindow();
        // Thay vì đóng stage hiện tại và tạo mới, ta gọi lại phương thức của MainApp
        // để hiển thị lại scene login trên stage hiện tại.
        try {
            // Giả sử MainApp có một phương thức để hiển thị lại login scene
            MainApp.showLoginScene(currentStage);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Đăng Xuất", "Không thể quay lại màn hình đăng nhập.");
            // Fallback: tạo stage mới nếu MainApp.showLoginScene thất bại hoặc không tồn tại
            try {
                Stage loginStage = new Stage();
                Parent loginRoot = FxmlLoaderUtil.loadFXML("/com/duyphuc/olympics/fxml/LoginView.fxml");
                loginStage.setTitle("Olympic Games Analyzer - Đăng nhập");
                loginStage.setScene(new Scene(loginRoot));
                currentStage.close(); // Đóng stage cũ sau khi stage mới sẵn sàng
                loginStage.show();
            } catch (IOException ioEx) {
                 ioEx.printStackTrace();
            }
        }
    }

    @FXML
    void handleManageMedals(ActionEvent event) {
        loadViewIntoContentArea("/com/duyphuc/olympics/fxml/MedalManagementView.fxml");
    }

    @FXML
    void handleViewCharts(ActionEvent event) {
        loadViewIntoContentArea("/com/duyphuc/olympics/fxml/ChartView.fxml");
    }

    @FXML
    void handleManageUsers(ActionEvent event) {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            loadViewIntoContentArea("/com/duyphuc/olympics/fxml/AdminUserManagementView.fxml");
        } else {
            AlertUtil.showWarning("Truy Cập Bị Từ Chối", "Bạn không có quyền truy cập chức năng này.");
        }
    }
}