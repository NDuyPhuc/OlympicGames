package com.duyphuc.olympics;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.util.FxmlLoaderUtil; // Thêm import này

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
// import javafx.stage.StageStyle; // Import này không được sử dụng, có thể xóa

import java.io.IOException; // Thêm import này

public class MainApp extends Application {
	
    private static Stage primaryStageHolder; // Giữ tham chiếu đến stage chính

    @Override
    public void start(Stage primaryStage) {
        MainApp.primaryStageHolder = primaryStage; // Lưu lại stage chính
        try {
            // Load application icon
            Image appIcon = new Image(getClass().getResourceAsStream("/com/duyphuc/olympics/images/Olympic_rings.png"));
            if (appIcon != null) {
                primaryStage.getIcons().add(appIcon);
            } else {
                System.err.println("Không tìm thấy icon ứng dụng.");
            }
            
            // Load the login view
            showLoginScene(primaryStage); // Gọi phương thức chung

        } catch(Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi Khởi Động Ứng Dụng");
                alert.setHeaderText("Không thể khởi động ứng dụng.");
                alert.setContentText("Đã xảy ra lỗi nghiêm trọng: " + e.getMessage());
                alert.showAndWait();
                Platform.exit(); // Thoát ứng dụng nếu không khởi động được
            });
        }
    }
    

    // Phương thức mới để hiển thị scene đăng nhập
    public static void showLoginScene(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/duyphuc/olympics/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 580); // Kích thước mặc định nếu scene chưa có
            stage.setScene(scene);
        } else {
            scene.setRoot(root); // Thay đổi root của scene hiện tại
        }
        
        stage.setTitle("Olympic Analyzer - Đăng nhập");
        stage.centerOnScreen(); // Đảm bảo cửa sổ ở giữa màn hình
        if (!stage.isShowing()) { // Chỉ show nếu chưa hiển thị
            stage.show();
        }
    }

    // (Tùy chọn) Phương thức để lấy stage chính nếu cần
    public static Stage getPrimaryStage() {
        return primaryStageHolder;
    }


    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() throws Exception {
        DBConnectionManager.closeInstanceConnection();
        super.stop();
    }
}