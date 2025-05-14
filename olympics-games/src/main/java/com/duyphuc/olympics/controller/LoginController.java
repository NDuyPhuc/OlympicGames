package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.MainApp; // Sẽ cần để MainApp gọi cleanup
import com.duyphuc.olympics.animation.OlympicRingsAnimation;
import com.duyphuc.olympics.animation.ParticleSystem;
import com.duyphuc.olympics.exception.AuthenticationException; // THÊM IMPORT
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
import javafx.application.Platform; // THÊM IMPORT
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // THÊM IMPORT (nếu dùng để load MainDashboard)
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Timer; // THÊM IMPORT
import java.util.TimerTask; // THÊM IMPORT

public class LoginController {
    @FXML private StackPane rootPane;
    @FXML private Canvas particleCanvas;
    @FXML private Pane olympicLogoContainer;
    @FXML private VBox loginContainer;
    @FXML private MFXTextField usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private Label messageLabel; // Đổi tên từ errorLabel để chung chung hơn
    @FXML private MFXButton loginButton;

    private AuthService authService;
    private ParticleSystem particleSystem;
    private OlympicRingsAnimation olympicRingsAnimation;
    private Timer countdownTimer; // Timer cho việc đếm ngược

    public LoginController() {
        this.authService = AuthService.getInstance();
    }

    @FXML
    public void initialize() {
        particleCanvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && particleSystem == null) {
                particleSystem = new ParticleSystem(particleCanvas);
                particleSystem.startAnimation();
            }
        });

        DropShadow loginContainerShadow = new DropShadow();
        loginContainerShadow.setRadius(20);
        loginContainerShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        loginContainerShadow.setOffsetY(5);
        loginContainer.setEffect(loginContainerShadow);

        olympicRingsAnimation = new OlympicRingsAnimation(olympicLogoContainer);
        olympicRingsAnimation.startAnimation();

        if (particleSystem == null && particleCanvas.getScene() != null) {
             particleSystem = new ParticleSystem(particleCanvas);
             particleSystem.startAnimation();
        }

        loginButton.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
            scale.setToX(1.05); scale.setToY(1.05); scale.play();
        });
        loginButton.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
            scale.setToX(1.0); scale.setToY(1.0); scale.play();
        });

        playEntranceAnimation();

        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        usernameField.setOnKeyPressed(this::handleEnterKeyPressedOnFields);
        passwordField.setOnKeyPressed(this::handleEnterKeyPressedOnFields);
    }

    private void handleEnterKeyPressedOnFields(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!loginButton.isDisable()) {
                playButtonPressAnimationAndLogin();
            }
            event.consume();
        }
    }

    private void playButtonPressAnimationAndLogin() {
        ScaleTransition pressAnimation = new ScaleTransition(Duration.millis(100), loginButton);
        pressAnimation.setToX(0.95); pressAnimation.setToY(0.95);
        pressAnimation.setAutoReverse(true); pressAnimation.setCycleCount(2);
        pressAnimation.setOnFinished(e -> loginButton.fire());
        pressAnimation.play();
    }

    private void playEntranceAnimation() {
        loginContainer.setScaleX(0.8); loginContainer.setScaleY(0.8); loginContainer.setOpacity(0);
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(600), loginContainer);
        scaleTransition.setToX(1.0); scaleTransition.setToY(1.0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(600), loginContainer);
        fadeTransition.setToValue(1.0);
        scaleTransition.play(); fadeTransition.play();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showStyledMessage("Tên đăng nhập và mật khẩu không được để trống.", true);
            shakeLoginForm();
            return;
        }

        // Vô hiệu hóa các control và hiển thị thông báo "Đang đăng nhập..."
        setLoginInProgress(true, "Đang đăng nhập...");

        // Sử dụng Platform.runLater để đảm bảo authService.login chạy sau khi UI đã cập nhật
        // (Hoặc thực hiện authService.login trên một luồng nền nếu nó tốn thời gian)
        // Ở đây, giả định authService.login đủ nhanh để chạy trên UI thread sau khi UI cập nhật
        // Nếu không, bạn cần Task<User>
        Platform.runLater(() -> { // Để đảm bảo UI được cập nhật trước khi gọi login
            try {
                User user = authService.login(username, password);
                // Nếu không có exception, đăng nhập thành công
                Platform.runLater(() -> { // Cập nhật UI sau khi login thành công
                    setLoginInProgress(false, null); // Ẩn thông báo "Đang đăng nhập"
                    messageLabel.setVisible(false);
                    messageLabel.setManaged(false);
                    playSuccessAnimation(() -> loadMainDashboard(user)); // Truyền user vào
                });

            } catch (AuthenticationException e) {
                Platform.runLater(() -> { // Cập nhật UI khi có lỗi xác thực
                    setLoginInProgress(false, null);
                    showStyledMessage(e.getMessage(), true); // Hiển thị thông báo lỗi từ exception
                    shakeLoginForm();
                    if (e.isAccountLocked()) {
                        loginButton.setDisable(true); // Vô hiệu hóa nút login
                        startCountdown(e.getLockDurationMillis() / 1000);
                    } else {
                        loginButton.setDisable(false); // Chỉ enable lại nếu không bị khóa
                    }
                });
            } catch (Exception e) { // Các lỗi không mong muốn khác
                Platform.runLater(() -> {
                    setLoginInProgress(false, null);
                    showStyledMessage("Đã xảy ra lỗi không xác định. Vui lòng thử lại.", true);
                    AlertUtil.showError("Lỗi Hệ Thống", "Lỗi: " + e.getMessage());
                    e.printStackTrace();
                    loginButton.setDisable(false); // Enable lại nút
                });
            }
        });
    }


    private void showStyledMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setTextFill(isError ? Color.valueOf("#DF0024") : Color.valueOf("#0085C7")); // Đỏ cho lỗi, xanh cho thông tin
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(200), messageLabel);
        fade.setFromValue(0.0); fade.setToValue(1.0); fade.play();
    }

    private void setLoginInProgress(boolean inProgress, String statusMessage) {
        loginButton.setDisable(inProgress);
        usernameField.setDisable(inProgress);
        passwordField.setDisable(inProgress);
        if (inProgress && statusMessage != null) {
            showStyledMessage(statusMessage, false); // Hiển thị thông báo trạng thái (không phải lỗi)
        } else if (!inProgress) {
            // Khi không còn inProgress, có thể ẩn messageLabel nếu không có lỗi nào khác được hiển thị
            // Hoặc để messageLabel hiển thị lỗi cuối cùng/thông báo thành công (tùy logic)
            // Hiện tại, các nhánh lỗi/thành công sẽ tự quản lý messageLabel
        }
    }

    private void shakeLoginForm() {
        Timeline shakeAnimation = createShakeAnimation(loginContainer);
        shakeAnimation.play();
    }

    private Timeline createShakeAnimation(Node node) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0), new KeyValue(node.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 10)),
            new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(400), new KeyValue(node.translateXProperty(), 0))
        );
        timeline.setCycleCount(1); return timeline;
    }

    private void playSuccessAnimation(Runnable onFinished) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), loginContainer);
        fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (onFinished != null) { onFinished.run(); }
        });
        fadeOut.play();
    }

    private void loadMainDashboard(User loggedInUser) { // Bỏ ActionEvent, nhận User
        try {
            // Stage currentStage = (Stage) loginButton.getScene().getWindow();
            // Lấy stage từ MainApp để đảm bảo tính nhất quán
            Stage currentStage = MainApp.getPrimaryStage();
            if (currentStage == null) { // Fallback nếu MainApp.getPrimaryStage() trả về null
                 currentStage = (Stage) loginButton.getScene().getWindow();
            }


            // Sử dụng FXMLLoader để có thể truyền AuthService cho MainDashboardController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/duyphuc/olympics/fxml/MainDashboardView.fxml"));
            Parent root = loader.load();

            // MainDashboardController dashboardController = loader.getController();
            // dashboardController.setAuthService(authService); // Giả sử có phương thức này

            // HOẶC, MainDashboardController sẽ tự lấy instance của AuthService
            // Việc này đã được làm trong initialize của MainDashboardController

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene); // Sử dụng lại stage hiện tại
            currentStage.setTitle("Olympic Games Medal Analyzer - Dashboard");
            currentStage.setMaximized(true); // Mở rộng tối đa
            currentStage.centerOnScreen();
            // Không cần currentStage.show() nữa nếu đã show từ MainApp
            // và chúng ta chỉ thay đổi scene.

            // Stop animations
            cleanupAnimations(); // Gọi hàm dọn dẹp animation

        } catch (IOException e) {
            e.printStackTrace();
            showStyledMessage("Lỗi: Không thể tải màn hình chính.", true);
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể tải màn hình chính của ứng dụng.");
        }
    }

    private void startCountdown(long seconds) {
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        countdownTimer = new Timer(true);
        long[] remainingSeconds = {seconds};

        // Cập nhật messageLabel ngay lập tức
        Platform.runLater(() -> showStyledMessage("Tài khoản bị khóa. Thử lại sau: " + remainingSeconds[0] + "s", true));


        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    remainingSeconds[0]--;
                    if (remainingSeconds[0] > 0) {
                        showStyledMessage("Tài khoản bị khóa. Thử lại sau: " + remainingSeconds[0] + "s", true);
                    } else {
                        showStyledMessage("Thời gian khóa đã hết. Bạn có thể thử đăng nhập lại.", false);
                        loginButton.setDisable(false);
                        if (countdownTimer != null) countdownTimer.cancel();
                    }
                });
            }
        }, 1000, 1000);
    }

    // Phương thức để dừng animations và timer
    public void cleanupAnimationsAndTimer() {
        if (particleSystem != null) {
            particleSystem.stopAnimation();
        }
        if (olympicRingsAnimation != null) {
            olympicRingsAnimation.stopAnimation();
        }
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null; // Quan trọng: đặt lại thành null
        }
    }

    // Sửa tên cho rõ ràng hơn
    private void cleanupAnimations() {
        if (particleSystem != null) {
            particleSystem.stopAnimation();
        }
        if (olympicRingsAnimation != null) {
            olympicRingsAnimation.stopAnimation();
        }
    }
}