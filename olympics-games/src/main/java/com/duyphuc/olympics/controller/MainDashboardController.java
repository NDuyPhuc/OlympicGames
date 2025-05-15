package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.MainApp;
import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.service.AuthService;
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
import java.util.Objects;

public class MainDashboardController {

    @FXML private BorderPane mainDashboardPane;
    @FXML private BorderPane contentArea;
    @FXML private Menu adminMenu;
    // Không cần các @FXML cho MenuItem nếu bạn không tương tác trực tiếp với chúng từ code
    // (ngoài việc setVisible cho adminMenu). Các onAction đã được định nghĩa trong FXML.

    private Node initialWelcomeView; // Sẽ chứa VBox chào mừng
    private Label welcomeMessageLabel; // Label để hiển thị thông điệp chào mừng, sẽ nằm trong initialWelcomeView

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = AuthService.getInstance(); // Đảm bảo AuthService đã được khởi tạo

        // Tạo nội dung chào mừng ban đầu một cách chủ động
        createInitialWelcomeView();
        contentArea.setCenter(initialWelcomeView); // Hiển thị ngay khi khởi tạo

        updateWelcomeMessage();
        configureAdminMenu();
    }

    /**
     * Tạo view chào mừng ban đầu và gán cho initialWelcomeView và welcomeMessageLabel.
     */
    private void createInitialWelcomeView() {
        welcomeMessageLabel = new Label(); // Khởi tạo label chào mừng
        welcomeMessageLabel.setId("welcomeMessageLabel"); // Đặt fx:id nếu muốn style qua CSS
        welcomeMessageLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Label instructionLabel = new Label("Chọn một chức năng từ thanh menu hoặc bảng điều khiển.");
        instructionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        VBox welcomeBox = new VBox(30); // Tăng khoảng cách
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.getChildren().addAll(welcomeMessageLabel, instructionLabel);
        welcomeBox.setPadding(new Insets(50)); // Tăng padding
        welcomeBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;"); // Thêm chút style

        initialWelcomeView = welcomeBox;
    }

    /**
     * Cập nhật nội dung của welcomeMessageLabel dựa trên người dùng hiện tại.
     */
    private void updateWelcomeMessage() {
        User currentUser = authService.getCurrentUser();
        String welcomeText = "Chào mừng đến với Olympic Games Medal Analyzer!"; // Thông điệp chung hơn
        if (currentUser != null) {
            welcomeText = "Chào mừng, " + currentUser.getUsername() + "!";
        }

        if (welcomeMessageLabel != null) {
            welcomeMessageLabel.setText(welcomeText);
        }
    }

    /**
     * Cấu hình hiển thị của menu admin dựa trên vai trò người dùng.
     */
    private void configureAdminMenu() {
        User currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        adminMenu.setVisible(isAdmin);
        // Nếu menu item "Quản lý người dùng" là một phần của menu admin thì không cần setVisible riêng
        // Nếu nó là một menu item riêng biệt thì bạn cần @FXML cho nó và setVisible ở đây
    }

    /**
     * Tải một FXML view vào khu vực trung tâm của dashboard.
     * @param fxmlPath Đường dẫn đến file FXML.
     */
    private void loadViewIntoContentArea(String fxmlPath) {
        try {
            // Sử dụng Objects.requireNonNull để kiểm tra fxmlPath không null, giúp debug dễ hơn
            Node view = FxmlLoaderUtil.loadFXML(Objects.requireNonNull(fxmlPath, "FXML path cannot be null"));
            contentArea.setCenter(view);
        } catch (IOException | NullPointerException e) { // Bắt cả NullPointerException từ requireNonNull
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện",
                    "Không thể tải giao diện: " + fxmlPath + "\nLỗi: " + e.getMessage());
            // Quay lại màn hình dashboard home khi có lỗi
            handleShowDashboardHome(null);
        }
    }

    @FXML
    void handleShowDashboardHome(ActionEvent event) {
        if (initialWelcomeView != null) {
            contentArea.setCenter(initialWelcomeView);
            updateWelcomeMessage(); // Đảm bảo thông điệp chào mừng được cập nhật
        } else {
            // Trường hợp này không nên xảy ra nếu createInitialWelcomeView() được gọi trong initialize()
            createInitialWelcomeView();
            contentArea.setCenter(initialWelcomeView);
            updateWelcomeMessage();
        }
    }
    
    @FXML
    void handleViewReports(ActionEvent event) {
        loadViewIntoContentArea("/com/duyphuc/olympics/fxml/ReportView.fxml");
    }
    
    @FXML
    void handleShowProfile(ActionEvent event) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                AlertUtil.showError("Lỗi Người Dùng", "Không tìm thấy thông tin người dùng hiện tại. Vui lòng đăng nhập lại.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/duyphuc/olympics/fxml/UserProfileView.fxml"));
            Parent profileRoot = loader.load();

            UserProfileController controller = loader.getController();
            // Truyền cả đối tượng User và AuthService cho UserProfileController
            controller.initData(currentUser, authService);

            Stage profileStage = new Stage();
            profileStage.setTitle("Hồ Sơ Người Dùng");
            profileStage.setScene(new Scene(profileRoot));
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.initOwner(mainDashboardPane.getScene().getWindow());
            profileStage.setResizable(false);
            // Thêm icon nếu có:
            // profileStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/duyphuc/olympics/images/app_icon.png"))));
            profileStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể mở màn hình hồ sơ người dùng: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Dữ Liệu", "Lỗi khi truy cập tài nguyên: " + e.getMessage() + ". Kiểm tra đường dẫn file FXML hoặc icon.");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        if (authService != null) {
            authService.logout();
        }
        Stage currentStage = (Stage) mainDashboardPane.getScene().getWindow();
        try {
            // MainApp sẽ chịu trách nhiệm hiển thị lại màn hình login trên currentStage
            MainApp.showLoginScene(currentStage);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Đăng Xuất", "Không thể quay lại màn hình đăng nhập. Vui lòng khởi động lại ứng dụng.");
            // Trong trường hợp nghiêm trọng, có thể đóng ứng dụng
            // Platform.exit();
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

 // Trong MainDashboardController.java
    @FXML
    void handleManageUsers(ActionEvent event) {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            loadViewIntoContentArea("/com/duyphuc/olympics/fxml/AdminUserManagementView.fxml");
        } else {
            AlertUtil.showError("Truy Cập Bị Từ Chối", "Bạn không có quyền truy cập chức năng quản lý người dùng.");
            handleShowDashboardHome(null); // Hoặc không làm gì
        }
    }
}