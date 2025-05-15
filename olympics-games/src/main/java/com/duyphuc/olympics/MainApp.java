package com.duyphuc.olympics;

import com.duyphuc.olympics.controller.LoginController;
import com.duyphuc.olympics.controller.MainDashboardController;
import com.duyphuc.olympics.db.DBConnectionManager;
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
import java.util.Objects;

public class MainApp extends Application {

    private static Stage primaryStageHolder;
    private static Object currentController; // Để lưu controller hiện tại

    @Override
    public void start(Stage primaryStage) {
        MainApp.primaryStageHolder = primaryStage;
        try {
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/duyphuc/olympics/images/Olympic_rings.png")));
            primaryStage.getIcons().add(appIcon);

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
        cleanupCurrentController();

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/duyphuc/olympics/fxml/LoginView.fxml"));
        Parent root = loader.load();
        currentController = loader.getController();

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 900, 600); // Điều chỉnh kích thước nếu cần
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

    public static void showMainDashboardScene(Stage stage, AuthService authService) throws IOException {
        cleanupCurrentController();

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/duyphuc/olympics/fxml/MainDashboardView.fxml"));
        Parent root = loader.load();
        currentController = loader.getController();

        Scene scene = stage.getScene();
        if (scene == null) {
             scene = new Scene(root);
             stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        stage.setTitle("Olympic Games Medal Analyzer - Dashboard");
        stage.setMaximized(true);
        stage.setResizable(true); // Cho phép thay đổi kích thước dashboard
        stage.centerOnScreen();
         if (!stage.isShowing()) {
            stage.show();
        }
    }


    private static void cleanupCurrentController() {
    	if (currentController instanceof LoginController) {
            ((LoginController) currentController).cleanupAnimationsAndTimer();
        } else if (currentController instanceof MainDashboardController) {
            // Đảm bảo dòng này được gọi để dọn dẹp dashboard cũ
            ((MainDashboardController) currentController).cleanupDashboard(); 
        }
        currentController = null;
    }

    public static Stage getPrimaryStage() {
        return primaryStageHolder;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        cleanupCurrentController();
        DBConnectionManager.closeInstanceConnection();
        super.stop();
        Platform.exit();
        System.exit(0);
    }
}