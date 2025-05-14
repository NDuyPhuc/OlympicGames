package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.service.AuthService;
import com.duyphuc.olympics.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField; // Thay vì Label
import javafx.stage.Stage;

public class UserProfileController {
    // Các @FXML cho TextField thay vì Label
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtRole;

    @FXML private PasswordField pfOldPassword;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Button btnChangePassword;
    @FXML private Button btnClose;

    private AuthService authService;
    private User currentUser;

    public void initialize() {
        // authService = AuthService.getInstance(); // Hoặc inject
        // Vì AuthService có thể chưa được khởi tạo khi FXML này được load lần đầu
        // (ví dụ khi ứng dụng mới mở, chưa login), việc lấy currentUser
        // nên được thực hiện sau khi màn hình này được hiển thị và có user login.
        // Tốt hơn là truyền AuthService và User vào controller này khi mở cửa sổ.

        // Giả sử UserProfileController được tạo và User được truyền vào:
        // public void setUserData(User user, AuthService authService) {
        //     this.currentUser = user;
        //     this.authService = authService;
        //     if (currentUser != null) {
        //         txtUsername.setText(currentUser.getUsername());
        //         txtEmail.setText(currentUser.getEmail());
        //         txtRole.setText(currentUser.getRole());
        //     }
        // }
    }

    // Phương thức này sẽ được gọi từ controller mở cửa sổ UserProfile
    public void initData(User user, AuthService service) {
        this.currentUser = user;
        this.authService = service; // Nhận AuthService từ bên ngoài

        if (currentUser != null) {
            txtUsername.setText(currentUser.getUsername());
            txtEmail.setText(currentUser.getEmail());
            txtRole.setText(currentUser.getRole());
        } else {
            // Xử lý trường hợp không có user (ví dụ: đóng cửa sổ, hiển thị lỗi)
            AlertUtil.showError("Lỗi", "Không có thông tin người dùng.");
            handleClose();
        }
    }


    @FXML
    private void handleChangePassword() {
        if (currentUser == null || authService == null) {
            AlertUtil.showError("Lỗi", "Không thể thực hiện. Dữ liệu người dùng chưa được tải.");
            return;
        }

        String oldPassword = pfOldPassword.getText();
        String newPassword = pfNewPassword.getText();
        String confirmPassword = pfConfirmPassword.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin mật khẩu.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            AlertUtil.showError("Lỗi", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return;
        }

        // Validate độ dài mật khẩu mới, độ phức tạp (nếu cần)
        if (newPassword.length() < 6) { // Ví dụ
            AlertUtil.showError("Lỗi", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        try {
            boolean success = authService.changePassword(currentUser.getUsername(), oldPassword, newPassword);
            if (success) {
                AlertUtil.showInfo("Thành công", "Đổi mật khẩu thành công.");
                clearPasswordFields();
            } else {
                AlertUtil.showError("Lỗi", "Mật khẩu cũ không đúng hoặc có lỗi xảy ra khi cập nhật.");
            }
        } catch (Exception e) { // Bắt các exception chung từ AuthService
            AlertUtil.showError("Lỗi", "Lỗi khi đổi mật khẩu: " + e.getMessage());
            e.printStackTrace(); // Log lỗi chi tiết ra console
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    private void clearPasswordFields() {
        pfOldPassword.clear();
        pfNewPassword.clear();
        pfConfirmPassword.clear();
    }
}