package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.dao.UserDAO;
import com.duyphuc.olympics.service.AuthService; // Nếu cần hash password
import com.duyphuc.olympics.util.AlertUtil;
import com.duyphuc.olympics.util.PasswordHasher; // Sử dụng trực tiếp nếu AuthService không có hàm hash

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class AdminUserManagementController {

    @FXML private TableView<User> usersTableView;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;

    @FXML private Button btnAddUser;
    @FXML private Button btnEditUser;
    @FXML private Button btnDeleteUser;
    @FXML private Button btnRefresh;
    @FXML private Label lblStatus;

    private UserDAO userDAO;
    private PasswordHasher passwordHasher; // Để hash mật khẩu
    private ObservableList<User> userList;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );


    @FXML
    public void initialize() {
        userDAO = new UserDAO(); // Khởi tạo DAO

        // Thiết lập các cột cho TableView
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Listener cho TableView selection để enable/disable nút Sửa/Xóa
        usersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean selected = (newSelection != null);
            btnEditUser.setDisable(!selected);
            btnDeleteUser.setDisable(!selected);
        });

        loadUsers(); // Tải danh sách người dùng
    }

    private void loadUsers() {
        try {
            List<User> users = userDAO.getAllUsers(); // UserDAO cần phương thức này
            userList = FXCollections.observableArrayList(users);
            usersTableView.setItems(userList);
            lblStatus.setText("Đã tải " + userList.size() + " người dùng.");
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Cơ Sở Dữ Liệu", "Không thể tải danh sách người dùng: " + e.getMessage());
            lblStatus.setText("Lỗi tải dữ liệu.");
        }
    }

    @FXML
    void handleRefreshAction(ActionEvent event) {
        loadUsers();
        usersTableView.getSelectionModel().clearSelection(); // Xóa selection cũ
    }

    @FXML
    void handleAddUserAction(ActionEvent event) {
        showUserFormDialog(null); // null cho biết đây là thêm mới
    }

    @FXML
    void handleEditUserAction(ActionEvent event) {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            showUserFormDialog(selectedUser);
        } else {
            AlertUtil.showWarning("Chưa Chọn Người Dùng", "Vui lòng chọn một người dùng để sửa.");
        }
    }

    private void showUserFormDialog(User userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/duyphuc/olympics/fxml/UserFormDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Lấy controller của dialog (nếu bạn tạo một controller riêng cho dialog)
            // UserFormDialogController controller = loader.getController();
            // controller.setData(...);

            // Hoặc truy cập trực tiếp các control nếu không có controller riêng
            TextField txtUsernameDialog = (TextField) dialogPane.lookup("#txtUsernameDialog");
            TextField txtEmailDialog = (TextField) dialogPane.lookup("#txtEmailDialog");
            PasswordField pfPasswordDialog = (PasswordField) dialogPane.lookup("#pfPasswordDialog");
            PasswordField pfConfirmPasswordDialog = (PasswordField) dialogPane.lookup("#pfConfirmPasswordDialog");
            ComboBox<String> cmbRoleDialog = (ComboBox<String>) dialogPane.lookup("#cmbRoleDialog");
            Label lblPasswordDialog = (Label) dialogPane.lookup("#lblPasswordDialog"); // Để thay đổi text

            cmbRoleDialog.setItems(FXCollections.observableArrayList("ADMIN", "STAFF")); // Hoặc một enum Roles

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(userToEdit == null ? "Thêm Người Dùng Mới" : "Chỉnh Sửa Người Dùng");

            // Thiết lập thông tin cho form sửa
            if (userToEdit != null) {
                txtUsernameDialog.setText(userToEdit.getUsername());
                txtUsernameDialog.setDisable(true); // Không cho sửa username
                txtEmailDialog.setText(userToEdit.getEmail());
                cmbRoleDialog.setValue(userToEdit.getRole());
                lblPasswordDialog.setText("Mật khẩu mới:"); // Thay đổi label cho dễ hiểu
                pfPasswordDialog.setPromptText("Để trống nếu không muốn đổi");
                pfConfirmPasswordDialog.setPromptText("Nhập lại mật khẩu mới");
            } else {
                 txtUsernameDialog.setDisable(false);
                 lblPasswordDialog.setText("Mật khẩu:");
            }

            // Thêm các nút vào dialog
            ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Validate input trước khi nút Lưu được enable (nếu là thêm mới)
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            if (userToEdit == null) { // Chỉ áp dụng cho thêm mới
                saveButton.setDisable(true);
                // Listener để enable nút Lưu khi các trường bắt buộc được điền
                Runnable validateFields = () -> {
                    boolean disabled = txtUsernameDialog.getText().trim().isEmpty() ||
                                       txtEmailDialog.getText().trim().isEmpty() ||
                                       pfPasswordDialog.getText().isEmpty() ||
                                       pfConfirmPasswordDialog.getText().isEmpty() ||
                                       cmbRoleDialog.getValue() == null;
                    saveButton.setDisable(disabled);
                };
                txtUsernameDialog.textProperty().addListener((obs, oldV, newV) -> validateFields.run());
                txtEmailDialog.textProperty().addListener((obs, oldV, newV) -> validateFields.run());
                pfPasswordDialog.textProperty().addListener((obs, oldV, newV) -> validateFields.run());
                pfConfirmPasswordDialog.textProperty().addListener((obs, oldV, newV) -> validateFields.run());
                cmbRoleDialog.valueProperty().addListener((obs, oldV, newV) -> validateFields.run());
            }


            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButtonType) {
                processSaveUser(userToEdit, txtUsernameDialog.getText(), txtEmailDialog.getText(),
                                pfPasswordDialog.getText(), pfConfirmPasswordDialog.getText(),
                                cmbRoleDialog.getValue());
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Giao Diện", "Không thể mở form người dùng: " + e.getMessage());
        }
    }

    private void processSaveUser(User userToEdit, String username, String email,
                                 String password, String confirmPassword, String role) {
        // --- VALIDATION ---
        if (username.trim().isEmpty()) {
            AlertUtil.showError("Lỗi Nhập Liệu", "Tên đăng nhập không được để trống.");
            return;
        }
        if (email.trim().isEmpty() || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            AlertUtil.showError("Lỗi Nhập Liệu", "Email không hợp lệ.");
            return;
        }
        if (role == null) {
            AlertUtil.showError("Lỗi Nhập Liệu", "Vui lòng chọn vai trò.");
            return;
        }

        User user = (userToEdit == null) ? new User() : userToEdit;
        user.setUsername(username.trim()); // Username đã được validate là không trống
        user.setEmail(email.trim());
        user.setRole(role);

        boolean isNewUser = (userToEdit == null);

        // Xử lý mật khẩu
        if (isNewUser) { // Thêm mới: Mật khẩu là bắt buộc
            if (password.isEmpty()) {
                AlertUtil.showError("Lỗi Nhập Liệu", "Mật khẩu không được để trống khi thêm mới.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                AlertUtil.showError("Lỗi Nhập Liệu", "Mật khẩu và xác nhận mật khẩu không khớp.");
                return;
            }
            // (Thêm) Validate độ phức tạp mật khẩu ở đây nếu muốn
            user.setHashedPassword(PasswordHasher.hashPassword(password));
        } else { // Sửa: Mật khẩu là tùy chọn
            if (!password.isEmpty()) { // Chỉ xử lý nếu người dùng nhập mật khẩu mới
                if (!password.equals(confirmPassword)) {
                    AlertUtil.showError("Lỗi Nhập Liệu", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
                    return;
                }
                // (Thêm) Validate độ phức tạp mật khẩu ở đây nếu muốn
                user.setHashedPassword(PasswordHasher.hashPassword(password));
            }
            // Nếu password rỗng, không thay đổi hashedPassword hiện tại của user (đã được set khi load userToEdit)
        }


        // --- SAVE TO DATABASE ---
        try {
            boolean success;
            String actionMessage;
            if (isNewUser) {
                // Kiểm tra username đã tồn tại chưa (chỉ khi thêm mới)
                if (userDAO.getUserByUsernameOptional(username.trim()).isPresent()) {
                     AlertUtil.showError("Lỗi Trùng Lặp", "Tên đăng nhập '" + username.trim() + "' đã tồn tại.");
                     return;
                }
                success = userDAO.addUser(user);
                actionMessage = "thêm mới";
            } else {
                success = userDAO.updateUser(user); // UserDAO cần phương thức này
                actionMessage = "cập nhật";
            }

            if (success) {
                AlertUtil.showInfo("Thành Công", "Đã " + actionMessage + " người dùng '" + user.getUsername() + "' thành công.");
                loadUsers(); // Refresh TableView
            } else {
                AlertUtil.showError("Thất Bại", "Không thể " + actionMessage + " người dùng. Có lỗi xảy ra.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Cơ Sở Dữ Liệu", "Lỗi khi lưu thông tin người dùng: " + e.getMessage());
        }
    }


    @FXML
    void handleDeleteUserAction(ActionEvent event) {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            AlertUtil.showWarning("Chưa Chọn Người Dùng", "Vui lòng chọn một người dùng để xóa.");
            return;
        }

        // Không cho xóa chính mình (nếu cần)
        User currentUser = AuthService.getInstance().getCurrentUser(); // Lấy user admin đang đăng nhập
        if (currentUser != null && currentUser.getId() == selectedUser.getId()) {
            AlertUtil.showError("Không Thể Xóa", "Bạn không thể xóa tài khoản của chính mình.");
            return;
        }


        boolean confirmed = AlertUtil.showConfirmation("Xác Nhận Xóa",
                "Bạn có chắc chắn muốn xóa người dùng '" + selectedUser.getUsername() + "' không?\n" +
                "Hành động này không thể hoàn tác.");

        if (confirmed) {
            try {
                boolean success = userDAO.deleteUser(selectedUser.getId()); // UserDAO cần phương thức này
                if (success) {
                    AlertUtil.showInfo("Thành Công", "Đã xóa người dùng '" + selectedUser.getUsername() + "'.");
                    userList.remove(selectedUser); // Xóa khỏi ObservableList để TableView cập nhật ngay
                    lblStatus.setText("Đã xóa người dùng: " + selectedUser.getUsername());
                } else {
                    AlertUtil.showError("Thất Bại", "Không thể xóa người dùng. Người dùng có thể không tồn tại hoặc có lỗi CSDL.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Lỗi Cơ Sở Dữ Liệu", "Lỗi khi xóa người dùng: " + e.getMessage());
            }
        }
    }
}