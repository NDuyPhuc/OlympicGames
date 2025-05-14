package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.animation.OlympicRingsAnimation;
import com.duyphuc.olympics.animation.ParticleSystem;
import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.service.AuthService;
import com.duyphuc.olympics.util.AlertUtil;
import com.duyphuc.olympics.util.FxmlLoaderUtil;

import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXButton;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;   // <<--- THÊM IMPORT
import javafx.scene.input.KeyEvent;  // <<--- THÊM IMPORT
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {
    @FXML private StackPane rootPane;
    @FXML private Canvas particleCanvas;
    @FXML private Pane olympicLogoContainer;
    @FXML private VBox loginContainer;
    @FXML private MFXTextField usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private MFXButton loginButton;

    private AuthService authService;
    private ParticleSystem particleSystem;
    private OlympicRingsAnimation olympicRingsAnimation;

    public LoginController() {
        this.authService = AuthService.getInstance();
    }

    @FXML
    public void initialize() {
        // Apply depth effect to the login container
        // Di chuyển khởi tạo particleSystem vào đây để đảm bảo nó được tạo sau khi canvas có scene
        // Tuy nhiên, nếu bạn muốn particle system chạy ngay cả khi scene chưa sẵn sàng (ít khả năng),
        // logic listener hiện tại của bạn cho particleCanvas là ổn.
        // Nhưng để nhất quán với olympicRingsAnimation, bạn có thể làm tương tự.
        // Hiện tại, tôi sẽ giữ nguyên logic của bạn cho particleCanvas.sceneProperty().
        particleCanvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && particleSystem == null) { // Thêm kiểm tra particleSystem == null để tránh tạo lại
                particleSystem = new ParticleSystem(particleCanvas);
                particleSystem.startAnimation(); // Bắt đầu animation ở đây nếu muốn
            }
        });

        DropShadow loginContainerShadow = new DropShadow();
        loginContainerShadow.setRadius(20);
        loginContainerShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        loginContainerShadow.setOffsetY(5);
        loginContainer.setEffect(loginContainerShadow);

        // Create and start the Olympic rings animation
        olympicRingsAnimation = new OlympicRingsAnimation(olympicLogoContainer);
        olympicRingsAnimation.startAnimation();

        // Initialize and start the particle system (nếu chưa được start ở listener trên)
        // Nếu bạn đã start trong listener, dòng này có thể không cần thiết hoặc gây start 2 lần.
        // Để an toàn, nếu đã có listener, hãy để listener quản lý việc start.
        // Nếu particleSystem có thể null ở đây, cần kiểm tra:
        // if (particleSystem != null) {
        //     particleSystem.startAnimation();
        // } else {
        //     // Xử lý trường hợp particleSystem chưa được khởi tạo (ví dụ: scene chưa có)
        //     // Hoặc đảm bảo listener sẽ khởi tạo và start nó.
        // }
        // Dựa trên code gốc, bạn khởi tạo mới ở đây, nên dòng listener có thể chỉ cần gán, không start.
        // Để đơn giản, tôi giả sử listener ở trên sẽ lo việc khởi tạo và start.
        // Nếu bạn muốn khởi tạo ở đây:
        if (particleSystem == null && particleCanvas.getScene() != null) { // Khởi tạo nếu chưa có và scene đã sẵn sàng
             particleSystem = new ParticleSystem(particleCanvas);
             particleSystem.startAnimation();
        }


        // Add hover effect to login button
        loginButton.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        loginButton.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        // Create entrance animation for login form
        playEntranceAnimation();

        // Hide message label initially
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        // === THÊM TÍNH NĂNG NHẤN ENTER ===
        // Gán sự kiện onKeyPressed cho các trường nhập liệu
        usernameField.setOnKeyPressed(this::handleEnterKeyPressedOnFields);
        passwordField.setOnKeyPressed(this::handleEnterKeyPressedOnFields);

        // Hoặc, nếu bạn muốn bắt Enter trên toàn bộ loginContainer (khi nó có focus)
        // loginContainer.setOnKeyPressed(event -> {
        //     if (event.getCode() == KeyCode.ENTER && !loginButton.isDisable()) {
        //         loginButton.fire(); // Kích hoạt nút
        //         event.consume();
        //     }
        // });
    }

    // === HÀM XỬ LÝ NHẤN ENTER TRÊN CÁC TRƯỜNG NHẬP LIỆU ===
    private void handleEnterKeyPressedOnFields(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!loginButton.isDisable()) {
                // Tạo hiệu ứng "nhấn nút" trước khi thực hiện đăng nhập
                playButtonPressAnimationAndLogin();
            }
            event.consume(); // Ngăn sự kiện lan truyền
        }
    }

    // === HÀM TẠO ANIMATION NHẤN NÚT VÀ GỌI ĐĂNG NHẬP ===
    private void playButtonPressAnimationAndLogin() {
        // Animation nhấn nút (thu nhỏ rồi trở lại)
        ScaleTransition pressAnimation = new ScaleTransition(Duration.millis(100), loginButton);
        pressAnimation.setToX(0.95);
        pressAnimation.setToY(0.95);
        pressAnimation.setAutoReverse(true);
        pressAnimation.setCycleCount(2); // Đi xuống rồi quay lại

        // Lưu trạng thái disable của nút trước khi chạy animation
        // boolean wasButtonDisabled = loginButton.isDisable();
        // loginButton.setDisable(true); // Tạm thời vô hiệu hóa nút trong khi animation chạy (tùy chọn)

        pressAnimation.setOnFinished(e -> {
            // loginButton.setDisable(wasButtonDisabled); // Khôi phục trạng thái disable
            // Kích hoạt hành động của nút login sau khi animation hoàn tất
            // Điều này sẽ gọi handleLogin(ActionEvent)
            loginButton.fire();
        });

        pressAnimation.play();
    }


    private void playEntranceAnimation() {
        // Initial state
        loginContainer.setScaleX(0.8);
        loginContainer.setScaleY(0.8);
        loginContainer.setOpacity(0);

        // Scale animation
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(600), loginContainer);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        // Fade animation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(600), loginContainer);
        fadeTransition.setToValue(1.0);

        // Play animations
        scaleTransition.play();
        fadeTransition.play();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Tên đăng nhập và mật khẩu không được để trống.");
            shakeLoginForm();
            return;
        }

        loginButton.setDisable(true); // Vẫn giữ disable ở đây để tránh double click/enter
        showInfoMessage("Đang đăng nhập...");

        try {
            final User user = authService.login(username, password);
            if (user != null) {
                messageLabel.setVisible(false);
                messageLabel.setManaged(false);
                playSuccessAnimation(() -> loadMainDashboard(event, user));
            } else {
                showErrorMessage("Tên đăng nhập hoặc mật khẩu không đúng.");
                shakeLoginForm();
                loginButton.setDisable(false); // Enable lại nút nếu đăng nhập thất bại
            }
        } catch (Exception e) {
            showErrorMessage("Đã xảy ra lỗi không xác định khi đăng nhập.");
            AlertUtil.showError("Lỗi Không Xác Định", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
            loginButton.setDisable(false); // Enable lại nút nếu có lỗi
        }
        // finally {
            // loginButton.setDisable(false); // Bỏ finally ở đây, xử lý setDisable(false) trong các nhánh cụ thể
        // }
    }

    private void showErrorMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.valueOf("#DF0024")); // Olympic red
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(200), messageLabel);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void showInfoMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.valueOf("#0085C7")); // Olympic blue
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void shakeLoginForm() {
        Timeline shakeAnimation = createShakeAnimation(loginContainer);
        shakeAnimation.play();
    }

    private Timeline createShakeAnimation(Node node) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(node.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(100),
                new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(200),
                new KeyValue(node.translateXProperty(), 10)),
            new KeyFrame(Duration.millis(300),
                new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(400),
                new KeyValue(node.translateXProperty(), 0))
        );
        timeline.setCycleCount(1);
        return timeline;
    }

    private void playSuccessAnimation(Runnable onFinished) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), loginContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        fadeOut.play();
    }

    private void loadMainDashboard(ActionEvent event, User loggedInUser) {
        try {
            Stage currentStage = (Stage) loginButton.getScene().getWindow(); // Lấy stage từ loginButton (hoặc bất kỳ node nào trong scene)

            Parent root = FxmlLoaderUtil.loadFXML("/com/duyphuc/olympics/fxml/MainDashboardView.fxml");

            Stage mainStage = new Stage();
            mainStage.setTitle("Olympic Games Medal Analyzer - Dashboard");
            mainStage.setScene(new Scene(root));
            mainStage.setMaximized(true);
            mainStage.show();

            // Stop animations before closing the stage
            if (particleSystem != null) { // Kiểm tra null trước khi gọi stop
                particleSystem.stopAnimation();
            }
            if (olympicRingsAnimation != null) { // Kiểm tra null
                olympicRingsAnimation.stopAnimation();
            }

            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Lỗi: Không thể tải màn hình chính.");
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể tải màn hình chính của ứng dụng.");
        }
    }
}