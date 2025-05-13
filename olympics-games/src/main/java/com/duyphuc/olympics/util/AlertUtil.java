package com.duyphuc.olympics.util;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class AlertUtil {

    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // Hoặc "Lỗi"
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    /**
     * Hiển thị một dialog xác nhận với tiêu đề và nội dung cho trước.
     * @param title Tiêu đề của dialog.
     * @param message Nội dung câu hỏi xác nhận.
     * @return true nếu người dùng nhấn OK, false nếu nhấn Cancel hoặc đóng dialog.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // Hoặc "Confirmation Needed"
        alert.setContentText(message);

        // Thêm các nút OK và Cancel (JavaFX tự thêm theo mặc định cho CONFIRMATION)
         alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
         
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}