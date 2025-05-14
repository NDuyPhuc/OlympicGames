package com.duyphuc.olympics;

import com.duyphuc.olympics.controller.LoginController; // THÊM IMPORT
import com.duyphuc.olympics.controller.MainDashboardController; // THÊM IMPORT (nếu cần cleanup cho dashboard)
import com.duyphuc.olympics.db.DBConnectionManager;
// Bỏ FxmlLoaderUtil nếu không dùng trực tiếp trong MainApp
// import com.duyphuc.olympics.util.FxmlLoaderUtil;
import com.duyphuc.olympics.service.AuthService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects; // THÊM IMPORT

public class MainApp extends Application {

    private static Stage primaryStageHolder;
    private static Object currentController; // Để lưu controller hiện tại

    @Override
    public void start(Stage primaryStage) {
        MainApp.primaryStageHolder = primaryStage;
        try {
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/duyphuc/olympics/images/Olympic_rings.png")));
            primaryStage.getIcons().add(appIcon); // Thêm icon cho stage

            showLoginScene(primaryStage);

        } catch(Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi Khởi Động Ứng Dụng");
                alert.setHeaderText("Không thể khởi động ứng dụng.");
                alert.setContentText("Đã xảy ra lỗi nghiêm trọng: " + e.getMessage());
                alert.showAndWait();
                Platform.exit();
            });
        }
    }

    public static void showLoginScene(Stage stage) throws IOException {
        // Dọn dẹp controller cũ trước khi load scene mới
        cleanupCurrentController();

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/duyphuc/olympics/fxml/LoginView.fxml"));
        Parent root = loader.load();
        currentController = loader.getController(); // Lưu controller mới

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 580);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        stage.setTitle("Olympic Analyzer - Đăng nhập");
        stage.centerOnScreen();
        if (!stage.isShowing()) {
            stage.show();
        }
    }

    // Phương thức mới để hiển thị MainDashboard và truyền AuthService
    public static void showMainDashboardScene(Stage stage, AuthService authService) throws IOException {
        // Dọn dẹp controller cũ (LoginController)
        cleanupCurrentController();

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/duyphuc/olympics/fxml/MainDashboardView.fxml"));
        Parent root = loader.load();
        currentController = loader.getController(); // Lưu MainDashboardController

        // Không cần truyền authService nữa nếu MainDashboardController tự lấy instance
        // MainDashboardController dashboardController = loader.getController();
        // dashboardController.setAuthService(authService); // Nếu có phương thức này

        Scene scene = stage.getScene();
        if (scene == null) {
             scene = new Scene(root); // Dashboard có thể tự quyết định kích thước hoặc maximized
             stage.setScene(scene);
             stage.setMaximized(true);
        } else {
            stage.setMaximized(true);
            scene.setRoot(root);
        }

        stage.setTitle("Olympic Games Medal Analyzer - Dashboard");
        stage.setMaximized(true);
        stage.centerOnScreen();
         if (!stage.isShowing()) {
        	   stage.setMaximized(true);        	 
            stage.show();
        }
    }


    private static void cleanupCurrentController() {
        if (currentController instanceof LoginController) {
            ((LoginController) currentController).cleanupAnimationsAndTimer();
        }
        // Thêm else if cho các controller khác nếu chúng cũng cần cleanup
        // else if (currentController instanceof MainDashboardController) {
        //     ((MainDashboardController) currentController).cleanupSomething();
        // }
        currentController = null; // Xóa tham chiếu
    }

    public static Stage getPrimaryStage() {
        return primaryStageHolder;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        cleanupCurrentController(); // Dọn dẹp controller cuối cùng trước khi thoát
        DBConnectionManager.closeInstanceConnection();
        super.stop();
        Platform.exit(); // Đảm bảo ứng dụng thoát hoàn toàn
        System.exit(0); // Thêm dòng này để chắc chắn thoát
    }
}