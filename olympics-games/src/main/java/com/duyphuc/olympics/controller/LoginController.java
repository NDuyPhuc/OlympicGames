package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.MainApp;
import com.duyphuc.olympics.animation.OlympicRingsAnimation;
import com.duyphuc.olympics.animation.ParticleSystem;
import com.duyphuc.olympics.exception.AuthenticationException; // Đã có
import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.service.AuthService;
import com.duyphuc.olympics.util.AlertUtil;

import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
// FXMLLoader, Parent, Scene, Stage không thay đổi trực tiếp trong logic này
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
// Color không còn dùng trực tiếp trong showStyledMessage
import javafx.util.Duration;

import java.io.IOException; // Đã có
import java.util.Timer;    // Đã có
import java.util.TimerTask;// Đã có

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
    private Timer countdownTimer;

    private String originalLoginButtonText;
    private MFXProgressSpinner loginSpinner;

    // Enum MessageType đã được thêm ở bước trước
    private enum MessageType {
        ERROR,
        INFO,
        SUCCESS
    }

    public LoginController() {
        this.authService = AuthService.getInstance();
    }

    @FXML
    public void initialize() {
        // ... (Phần initialize giữ nguyên như code đầy đủ bạn đã cung cấp ở câu hỏi trước)
        particleCanvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && particleSystem == null) {
                particleSystem = new ParticleSystem(particleCanvas);
                particleSystem.startAnimation();
            }
        });

        olympicRingsAnimation = new OlympicRingsAnimation(olympicLogoContainer);
        olympicRingsAnimation.startAnimation();

        if (particleSystem == null && particleCanvas.getScene() != null) {
             particleSystem = new ParticleSystem(particleCanvas);
             particleSystem.startAnimation();
        }

        originalLoginButtonText = loginButton.getText();
        loginSpinner = new MFXProgressSpinner();
        loginSpinner.setRadius(10);

        playEntranceAnimation();

        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        usernameField.setOnKeyPressed(this::handleEnterKeyPressedOnFields);
        passwordField.setOnKeyPressed(this::handleEnterKeyPressedOnFields);
    }

    private void handleEnterKeyPressedOnFields(KeyEvent event) {
        // ... (Giữ nguyên)
        if (event.getCode() == KeyCode.ENTER) {
            if (!loginButton.isDisable()) {
                playButtonPressAnimationAndLogin();
            }
            event.consume();
        }
    }

    private void playButtonPressAnimationAndLogin() {
        // ... (Giữ nguyên)
        ScaleTransition pressAnimation = new ScaleTransition(Duration.millis(100), loginButton);
        pressAnimation.setToX(0.97);
        pressAnimation.setToY(0.97);
        pressAnimation.setAutoReverse(true);
        pressAnimation.setCycleCount(2);
        pressAnimation.setOnFinished(e -> loginButton.fire());
        pressAnimation.play();
    }

    private void playEntranceAnimation() {
        // ... (Giữ nguyên)
        loginContainer.setOpacity(0);
        loginContainer.setScaleX(0.9);
        loginContainer.setScaleY(0.9);
        loginContainer.setTranslateY(30);

        FadeTransition ft = new FadeTransition(Duration.millis(500), loginContainer);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition st = new ScaleTransition(Duration.millis(500), loginContainer);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition tt = new TranslateTransition(Duration.millis(450), loginContainer);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(loginContainer, ft, st, tt);
        pt.setDelay(Duration.millis(100));
        pt.play();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showStyledMessage("Tên đăng nhập và mật khẩu không được để trống.", MessageType.ERROR);
            shakeLoginForm();
            return;
        }

        setLoginInProgress(true, "Đang xác thực...");

        Platform.runLater(() -> {
            try {
                User user = authService.login(username, password);
                // Đăng nhập thành công
                Platform.runLater(() -> {
                    showStyledMessage("Đăng nhập thành công!", MessageType.SUCCESS);
                    playSuccessAnimation(() -> {
                        loadMainDashboard(user);
                    });
                });

            } catch (AuthenticationException e) {
                Platform.runLater(() -> { // Cập nhật UI khi có lỗi xác thực
                    setLoginInProgress(false, null); // Reset button và spinner

                    // **PHẦN THAY ĐỔI ĐỂ HIỂN THỊ THÔNG BÁO CHI TIẾT**
                    String errorMessage = e.getMessage(); // Lấy thông báo gốc từ exception

                    if (e.isAccountLocked()) {
                        // Nếu tài khoản đã bị khóa, thông báo sẽ bao gồm thời gian khóa
                        // errorMessage đã chứa thông tin này từ AuthService rồi.
                        // Ví dụ: "Tài khoản đã bị khóa. Vui lòng thử lại sau 30 giây."
                        loginButton.setDisable(true); // Vô hiệu hóa nút login
                        startCountdown(e.getLockDurationMillis() / 1000);
                    } else {
                        // Nếu tài khoản chưa bị khóa, nhưng đăng nhập sai
                        // AuthService có thể đã cung cấp thông tin về số lần thử còn lại
                        // trong message của Exception.
                        // Ví dụ: "Sai tên đăng nhập hoặc mật khẩu. Còn 3 lần thử."
                        // Nếu AuthenticationException không tự động thêm số lần thử vào message,
                        // bạn cần điều chỉnh AuthService để nó làm vậy, hoặc
                        // nếu AuthService có phương thức lấy số lần thử sai hiện tại, bạn có thể gọi ở đây
                        // và tự xây dựng chuỗi errorMessage.
                        // Giả sử e.getMessage() đã đủ chi tiết:
                        loginButton.setDisable(false); // Cho phép thử lại nếu chưa bị khóa
                    }

                    showStyledMessage(errorMessage, MessageType.ERROR);
                    shakeLoginForm();
                });
            } catch (Exception e) { // Các lỗi không mong muốn khác
                Platform.runLater(() -> {
                    setLoginInProgress(false, null);
                    showStyledMessage("Đã xảy ra lỗi không xác định. Vui lòng thử lại.", MessageType.ERROR);
                    AlertUtil.showError("Lỗi Hệ Thống", "Lỗi: " + e.getMessage());
                    e.printStackTrace();
                    loginButton.setDisable(false); // Enable lại nút
                });
            }
        });
    }


    private void showStyledMessage(String message, MessageType type) {
        // ... (Giữ nguyên)
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error", "info", "success");

        switch (type) {
            case ERROR:
                messageLabel.getStyleClass().add("error");
                break;
            case INFO:
                messageLabel.getStyleClass().add("info");
                break;
            case SUCCESS:
                messageLabel.getStyleClass().add("success");
                break;
        }
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(250), messageLabel);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void setLoginInProgress(boolean inProgress, String statusMessage) {
        // ... (Giữ nguyên)
        usernameField.setDisable(inProgress);
        passwordField.setDisable(inProgress);

        if (inProgress) {
            loginButton.setText("");
            loginButton.setGraphic(loginSpinner);
            loginButton.setDisable(true);
            if (statusMessage != null) {
                showStyledMessage(statusMessage, MessageType.INFO);
            }
        } else {
            loginButton.setText(originalLoginButtonText);
            loginButton.setGraphic(null);
            // Việc enable/disable nút được xử lý trong các nhánh của handleLogin
        }
    }

    private void shakeLoginForm() {
        // ... (Giữ nguyên)
        Timeline shakeAnimation = createShakeAnimation(loginContainer);
        shakeAnimation.play();
    }

    private Timeline createShakeAnimation(Node node) {
        // ... (Giữ nguyên)
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0), new KeyValue(node.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(80), new KeyValue(node.translateXProperty(), -8)),
            new KeyFrame(Duration.millis(160), new KeyValue(node.translateXProperty(), 8)),
            new KeyFrame(Duration.millis(240), new KeyValue(node.translateXProperty(), -8)),
            new KeyFrame(Duration.millis(320), new KeyValue(node.translateXProperty(), 0))
        );
        timeline.setCycleCount(1); return timeline;
    }

    private void playSuccessAnimation(Runnable onFinished) {
        // ... (Giữ nguyên)
        ScaleTransition st = new ScaleTransition(Duration.millis(400), loginContainer);
        st.setToX(0.85);
        st.setToY(0.85);
        st.setInterpolator(Interpolator.EASE_IN);

        FadeTransition ft = new FadeTransition(Duration.millis(450), loginContainer);
        ft.setToValue(0);
        ft.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition pt = new ParallelTransition(loginContainer, st, ft);
        pt.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        pt.play();
    }

    private void loadMainDashboard(User loggedInUser) {
        // ... (Giữ nguyên)
        try {
            Stage currentStage = MainApp.getPrimaryStage();
            if (currentStage == null) {
                 currentStage = (Stage) loginButton.getScene().getWindow();
            }
            MainApp.showMainDashboardScene(currentStage, authService);
        } catch (IOException e) {
            e.printStackTrace();
            setLoginInProgress(false, null);
            showStyledMessage("Lỗi: Không thể tải màn hình chính.", MessageType.ERROR);
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể tải màn hình chính của ứng dụng.");
        }
    }

    private void startCountdown(long seconds) {
        // ... (Giữ nguyên, nhưng đảm bảo message là từ exception nếu cần)
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        countdownTimer = new Timer(true);
        long[] remainingSeconds = {seconds}; // seconds này là lockDuration

        // Thông báo ban đầu khi khóa
        // Nếu `AuthenticationException` khi isAccountLocked() trả về message dạng
        // "Tài khoản đã bị khóa. Vui lòng thử lại sau X giây." thì dùng trực tiếp.
        // Nếu không, bạn cần tự tạo message ở đây.
        // Giả sử e.getMessage() khi isAccountLocked() đã bao gồm thông tin này.
        // (Đoạn này được gọi từ catch, nên message cụ thể sẽ là e.getMessage())

        // Lặp lại thông báo đếm ngược
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    remainingSeconds[0]--;
                    if (remainingSeconds[0] > 0) {
                        // Thông báo lỗi nên giữ nguyên thông điệp "Tài khoản bị khóa..."
                        showStyledMessage("Tài khoản bị khóa. Thử lại sau: " + remainingSeconds[0] + "s", MessageType.ERROR);
                    } else {
                        showStyledMessage("Thời gian khóa đã hết. Bạn có thể thử đăng nhập lại.", MessageType.INFO);
                        loginButton.setDisable(false);
                        if (countdownTimer != null) {
                            countdownTimer.cancel();
                            countdownTimer = null;
                        }
                    }
                });
            }
        }, 1000, 1000); // Bắt đầu sau 1s, lặp lại mỗi 1s
    }

    public void cleanupAnimationsAndTimer() {
        // ... (Giữ nguyên)
        if (particleSystem != null) {
            particleSystem.stopAnimation();
            particleSystem = null;
        }
        if (olympicRingsAnimation != null) {
            olympicRingsAnimation.stopAnimation();
            olympicRingsAnimation = null;
        }
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }
}