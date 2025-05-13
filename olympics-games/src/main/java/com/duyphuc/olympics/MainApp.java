package com.duyphuc.olympics;

import com.duyphuc.olympics.db.DBConnectionManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/duyphuc/olympics/fxml/LoginView.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Olympic Analyzer - Đăng nhập");
            primaryStage.setScene(new Scene(root, 450, 350)); // Tăng kích thước một chút
            primaryStage.setResizable(false); // Không cho thay đổi kích thước cửa sổ login
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
            // Hiển thị lỗi nghiêm trọng cho người dùng nếu cần
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi Khởi Động Ứng Dụng");
            alert.setHeaderText("Không thể khởi động ứng dụng.");
            alert.setContentText("Đã xảy ra lỗi nghiêm trọng: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void stop() throws Exception {
        DBConnectionManager.closeInstanceConnection(); // Đóng kết nối CSDL khi ứng dụng dừng
        super.stop();
    }
}