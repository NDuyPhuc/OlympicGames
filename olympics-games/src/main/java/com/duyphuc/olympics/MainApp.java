package com.duyphuc.olympics;

import com.duyphuc.olympics.db.DBConnectionManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load application icon
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/duyphuc/olympics/images/Olympic_rings.png")));
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/duyphuc/olympics/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            // Create and configure the scene
            Scene scene = new Scene(root, 800, 580);
            
            // Configure the stage
            primaryStage.setTitle("Olympic Analyzer - Đăng nhập");
            primaryStage.setScene(scene);
            
            // Center on screen
            primaryStage.centerOnScreen();
            
            // Show the stage
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
            // Display serious error to user if needed
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi Khởi Động Ứng Dụng");
                alert.setHeaderText("Không thể khởi động ứng dụng.");
                alert.setContentText("Đã xảy ra lỗi nghiêm trọng: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() throws Exception {
        DBConnectionManager.closeInstanceConnection(); // Close DB connection when app stops
        super.stop();
    }
}
