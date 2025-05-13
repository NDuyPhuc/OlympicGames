package com.duyphuc.olympics.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.net.URL;

public class FxmlLoaderUtil {

    /**
     * Tải một file FXML và trả về node gốc của nó.
     * @param fxmlPath Đường dẫn đến file FXML, bắt đầu từ thư mục resources (ví dụ: "/com/duyphuc/olympics/fxml/LoginView.fxml")
     * @return Node gốc (Parent) của FXML đã tải.
     * @throws IOException Nếu có lỗi khi tải FXML.
     */
    public static Parent loadFXML(String fxmlPath) throws IOException {
        URL fxmlUrl = FxmlLoaderUtil.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("Cannot load FXML file: " + fxmlPath + ". Resource not found.");
        }
        return FXMLLoader.load(fxmlUrl);
    }

    /**
     * Tải một file FXML và trả về một đối tượng FXMLLoader, cho phép truy cập controller.
     * @param fxmlPath Đường dẫn đến file FXML.
     * @return Đối tượng FXMLLoader.
     * @throws IOException Nếu có lỗi khi tải FXML.
     */
    public static FXMLLoader getLoader(String fxmlPath) throws IOException {
        URL fxmlUrl = FxmlLoaderUtil.class.getResource(fxmlPath);
         if (fxmlUrl == null) {
            throw new IOException("Cannot load FXML file: " + fxmlPath + ". Resource not found.");
        }
        return new FXMLLoader(fxmlUrl);
    }
}
