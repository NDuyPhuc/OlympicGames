package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.MainApp;
import com.duyphuc.olympics.animation.OlympicRingsAnimation;
import com.duyphuc.olympics.model.User;
import com.duyphuc.olympics.service.AuthService;
import com.duyphuc.olympics.service.IAuthService;
import com.duyphuc.olympics.util.AlertUtil;
import com.duyphuc.olympics.util.FxmlLoaderUtil;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class MainDashboardController {

    @FXML private BorderPane mainDashboardPane;
    @FXML private BorderPane contentArea;
    @FXML private ScrollPane contentScrollPane;
    @FXML private VBox sidebar;
    @FXML private HBox appTitleHBoxInSidebar;
    @FXML private VBox adminSection;
    @FXML private Button toggleSidebarButton;
    @FXML private Button dashboardHomeButton;
    @FXML private Button manageMedalsButton;
    @FXML private Button viewChartsButton;
    @FXML private Button viewReportsButton;
    @FXML private Button manageUsersButton;
    @FXML private Button manageOlympicEventsButton; // <<< NEW FXML FIELD
    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    @FXML private Pane olympicAnimationPlaceholder;

    private Node initialDashboardHomeView;
    private Label welcomeMessageLabel;
    private IAuthService authService;
    private OlympicRingsAnimation olympicAnimation;

    private boolean sidebarCollapsed = false;
    private final double SIDEBAR_EXPANDED_WIDTH = 250.0;
    private final double SIDEBAR_COLLAPSED_WIDTH = 65.0;
    private final String ORIGINAL_TEXT_KEY = "originalText";

    // To hold reference to MedalManagementController for refreshing its ComboBox
    private MedalManagementController medalManagementControllerInstance;

    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        createInitialDashboardHomeView();
        loadViewIntoContentArea(initialDashboardHomeView, false); // Load home view initially

        if (olympicAnimationPlaceholder != null) {
            try {
                olympicAnimation = new OlympicRingsAnimation(olympicAnimationPlaceholder);
                olympicAnimation.startAnimation();
            } catch (Exception e) {
                System.err.println("Could not initialize or start Olympic Animation: " + e.getMessage());
            }
        } else {
            System.err.println("Olympic Animation Placeholder is null. Check FXML or injection.");
        }
        
        updateWelcomeMessage();
        configureAdminControls();
        updateSidebarState(); 

        if (dashboardHomeButton != null) {
            dashboardHomeButton.requestFocus();
        }
    }

    @FXML
    private void handleToggleSidebar(ActionEvent event) {
        sidebarCollapsed = !sidebarCollapsed;
        updateSidebarState();
    }

    private void updateSidebarState() {
        if (toggleSidebarButton == null || sidebar == null) return;

        toggleSidebarButton.setText(sidebarCollapsed ? "☰" : "❮");
        sidebar.setPrefWidth(sidebarCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH);
        setSidebarButtonsDisplay(sidebarCollapsed);
    }

    private void setSidebarButtonsDisplay(boolean collapsed) {
        if (appTitleHBoxInSidebar != null) {
            appTitleHBoxInSidebar.setVisible(!collapsed);
            appTitleHBoxInSidebar.setManaged(!collapsed);
        }
        processButtonsInContainer(sidebar, collapsed);
        // adminSection is part of sidebar, so processButtonsInContainer(sidebar, collapsed) will handle its children.
        // No need to call processButtonsInContainer(adminSection, collapsed) separately if adminSection itself is a direct child of 'sidebar'.
        // If adminSection's buttons needed special handling different from other sidebar buttons, then a separate call would be justified.
    }

    private void processButtonsInContainer(Pane container, boolean collapsed) {
        if (container == null) return;
        for (Node node : container.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                if (button == toggleSidebarButton) continue; 

                String originalText = getOriginalButtonText(button);

                if (collapsed) {
                    if (button.getText() != null && !button.getText().isEmpty() && button.getProperties().get(ORIGINAL_TEXT_KEY) == null) {
                        button.getProperties().put(ORIGINAL_TEXT_KEY, button.getText());
                    }
                    button.setText(null);
                    button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    button.setAlignment(Pos.CENTER);
                    button.setPrefWidth(SIDEBAR_COLLAPSED_WIDTH - 10); 
                    button.getStyleClass().add("nav-button-collapsed");
                    button.getStyleClass().remove("nav-button-expanded");
                } else {
                    String textToShow = (String) button.getProperties().getOrDefault(ORIGINAL_TEXT_KEY, originalText);
                     if (textToShow == null || textToShow.isEmpty()) {
                        textToShow = (button.getGraphic() != null) ? "" : "N/A"; // Provide a fallback if text is somehow lost
                    }
                    button.setText(textToShow);
                    button.setContentDisplay(ContentDisplay.LEFT);
                    button.setAlignment(Pos.CENTER_LEFT);
                    button.setPrefWidth(VBox.USE_COMPUTED_SIZE); 
                    button.getStyleClass().remove("nav-button-collapsed");
                    button.getStyleClass().add("nav-button-expanded");
                }
            } else if (node instanceof VBox) { // Recursively process VBox children
                processButtonsInContainer((VBox) node, collapsed);
            }
        }
    }
    
    private String getOriginalButtonText(Button button) {
        if (button.getProperties().containsKey(ORIGINAL_TEXT_KEY)) {
            return (String) button.getProperties().get(ORIGINAL_TEXT_KEY);
        }
        String buttonId = button.getId();
        if (buttonId == null) return (button.getText() != null) ? button.getText() : ""; // Fallback to current text

        switch (buttonId) {
            case "dashboardHomeButton": return "Trang chủ Dashboard";
            case "manageMedalsButton": return "Quản lý Huy chương";
            case "viewChartsButton": return "Xem Biểu đồ";
            case "viewReportsButton": return "Xem Báo cáo";
            case "manageUsersButton": return "Quản lý Người dùng";
            case "manageOlympicEventsButton": return "Quản lý Sự kiện Olympic"; // <<< ADDED
            case "profileButton": return "Thông tin cá nhân";
            case "logoutButton": return "Đăng xuất";
            default: return (button.getText() != null) ? button.getText() : "";
        }
    }
    
    private void createInitialDashboardHomeView() {
        VBox homeContent = new VBox(30);
        homeContent.setAlignment(Pos.TOP_CENTER);
        homeContent.setPadding(new Insets(40, 50, 50, 50));
        homeContent.getStyleClass().add("welcome-pane");

        welcomeMessageLabel = new Label();
        welcomeMessageLabel.getStyleClass().add("welcome-header");
        updateWelcomeMessage();

        Label instructionLabel = new Label("Khám phá dữ liệu Olympic hoặc quản lý hệ thống từ thanh điều hướng bên trái.");
        instructionLabel.getStyleClass().add("welcome-subheader");
        
        VBox welcomeTextContainer = new VBox(10, welcomeMessageLabel, instructionLabel);
        welcomeTextContainer.setAlignment(Pos.CENTER);

        if (this.olympicAnimationPlaceholder == null) {
             this.olympicAnimationPlaceholder = new StackPane(); 
             this.olympicAnimationPlaceholder.setPrefHeight(150); 
             this.olympicAnimationPlaceholder.setId("olympicAnimationPlaceholderCodeInjected");
        }

        GridPane quickAccessGrid = new GridPane();
        quickAccessGrid.getStyleClass().add("quick-access-grid");
        quickAccessGrid.setAlignment(Pos.CENTER);

        Node cardMedals = createAccessCard("/com/duyphuc/olympics/images/medal_icon.png","Quản lý Huy Chương","Thêm, sửa, xóa dữ liệu huy chương Olympic.", e -> handleManageMedals(null));
        Node cardCharts = createAccessCard("/com/duyphuc/olympics/images/chart_icon.png", "Xem Biểu Đồ", "Trực quan hóa dữ liệu huy chương qua các biểu đồ.", e -> handleViewCharts(null));
        Node cardReports = createAccessCard("/com/duyphuc/olympics/images/report_icon.png", "Xem Báo Cáo", "Tạo và xem các báo cáo tổng hợp.", e -> handleViewReports(null));

        quickAccessGrid.add(cardMedals, 0, 0);
        quickAccessGrid.add(cardCharts, 1, 0);
        quickAccessGrid.add(cardReports, 2, 0);
        
        homeContent.getChildren().addAll(welcomeTextContainer, this.olympicAnimationPlaceholder, quickAccessGrid);
        initialDashboardHomeView = homeContent;
    }
    
    private VBox createAccessCard(String iconPath, String title, String description, javafx.event.EventHandler<javafx.scene.input.MouseEvent> onClickAction) {
        VBox card = new VBox(10);
        card.getStyleClass().add("access-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(280); card.setPrefHeight(200);
        ImageView iconView = new ImageView();
        try {
            iconView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
            iconView.setFitHeight(48); iconView.setFitWidth(48);
        } catch (Exception e) { System.err.println("Cannot load icon for card: " + iconPath + " - " + e.getMessage()); }
        Label titleLabel = new Label(title); titleLabel.getStyleClass().add("access-card-title");
        Label descriptionLabel = new Label(description); descriptionLabel.getStyleClass().add("access-card-description");
        descriptionLabel.setWrapText(true); descriptionLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        card.getChildren().addAll(iconView, titleLabel, descriptionLabel);
        card.setOnMouseClicked(onClickAction);
        ScaleTransition stOver = new ScaleTransition(Duration.millis(200), card); stOver.setToX(1.03); stOver.setToY(1.03);
        ScaleTransition stOut = new ScaleTransition(Duration.millis(200), card); stOut.setToX(1.0); stOut.setToY(1.0);
        card.setOnMouseEntered(e -> stOver.playFromStart());
        card.setOnMouseExited(e -> stOut.playFromStart());
        return card;
    }

    private void updateWelcomeMessage() {
        User currentUser = authService.getCurrentUser();
        String welcomeText = "Chào mừng đến với Olympic Games Medal Analyzer!";
        if (currentUser != null) {
            welcomeText = "Chào mừng, " + currentUser.getUsername() + "!"; 
        }
        if (welcomeMessageLabel != null) { welcomeMessageLabel.setText(welcomeText); }
    }

    private void configureAdminControls() {
        User currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        
        if (adminSection != null) { 
            adminSection.setVisible(isAdmin); 
            adminSection.setManaged(isAdmin); 
        }
        // Individual buttons within adminSection also need to be managed if adminSection itself is not the sole controller of their visibility.
        // However, since adminSection's visibility is set, its children will also be hidden/shown.
        // If you had admin buttons outside adminSection, you'd manage them like this:
        // if (manageUsersButton != null) { manageUsersButton.setVisible(isAdmin); manageUsersButton.setManaged(isAdmin); }
        // if (manageOlympicEventsButton != null) { manageOlympicEventsButton.setVisible(isAdmin); manageOlympicEventsButton.setManaged(isAdmin); }
    }

    private void loadViewIntoContentArea(String fxmlPath, boolean passMedalControllerRef) {
        try {
            FXMLLoader loader = FxmlLoaderUtil.getLoader(Objects.requireNonNull(fxmlPath, "FXML path cannot be null"));
            Node view = loader.load();

            if (passMedalControllerRef && "/com/duyphuc/olympics/fxml/MedalManagementView.fxml".equals(fxmlPath)) {
                this.medalManagementControllerInstance = loader.getController();
            }
            
            loadViewIntoContentArea(view, true);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể tải giao diện: " + fxmlPath + "\nLỗi: " + e.getMessage());
            handleShowDashboardHome(null); // Revert to home on error
        }
    }
    
    // Overloaded method for convenience if no controller reference needs to be passed or captured
    private void loadViewIntoContentArea(String fxmlPath) {
        loadViewIntoContentArea(fxmlPath, false);
    }


    private void loadViewIntoContentArea(Node viewNode, boolean useFadeTransition) {
        if (contentArea == null) return;
        if (useFadeTransition) {
            FadeTransition ft = new FadeTransition(Duration.millis(300), viewNode); ft.setFromValue(0.0); ft.setToValue(1.0);
            ft.setInterpolator(Interpolator.EASE_IN); contentArea.setCenter(viewNode); ft.play();
        } else { contentArea.setCenter(viewNode); }
        if (contentScrollPane != null) { contentScrollPane.setVvalue(0.0); contentScrollPane.setHvalue(0.0); }
    }

    @FXML void handleShowDashboardHome(ActionEvent event) {
        if (initialDashboardHomeView == null) { createInitialDashboardHomeView(); }
        loadViewIntoContentArea(initialDashboardHomeView, true); 
        updateWelcomeMessage();
        // Restart animation if necessary
        if (olympicAnimation != null && olympicAnimationPlaceholder != null && 
            !olympicAnimationPlaceholder.getChildren().contains(olympicAnimation.getRingsGroup())) {
            try {
                olympicAnimationPlaceholder.getChildren().clear(); 
                olympicAnimation = new OlympicRingsAnimation(olympicAnimationPlaceholder); // Re-initialize
                olympicAnimation.startAnimation();
            } catch (Exception e) { System.err.println("Error restarting animation on home: " + e.getMessage()); }
        } else if (olympicAnimation != null) { 
            olympicAnimation.startAnimation(); // Ensure it's running
        }
        
        if (event != null && event.getSource() instanceof Button) { ((Button)event.getSource()).requestFocus(); } 
        else if (dashboardHomeButton != null) { dashboardHomeButton.requestFocus(); }
    }
    
    @FXML void handleViewReports(ActionEvent event) {
        loadViewIntoContentArea("/com/duyphuc/olympics/fxml/ReportView.fxml");
        if (event != null && event.getSource() instanceof Button) ((Button)event.getSource()).requestFocus();
    }
    
    @FXML void handleShowProfile(ActionEvent event) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) { AlertUtil.showError("Lỗi Người Dùng", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại."); return; }
            FXMLLoader loader = FxmlLoaderUtil.getLoader("/com/duyphuc/olympics/fxml/UserProfileView.fxml");
            Parent profileRoot = loader.load();
            UserProfileController controller = loader.getController();
            controller.initData(currentUser, authService);
            Stage profileStage = new Stage(); profileStage.setTitle("Hồ Sơ Người Dùng");
            profileStage.setScene(new Scene(profileRoot)); profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.initOwner(mainDashboardPane.getScene().getWindow()); profileStage.setResizable(false);
            try { profileStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/duyphuc/olympics/images/Olympic_rings.png"))));
            } catch (Exception e) { System.err.println("Profile icon not found.");}
            profileStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể mở màn hình hồ sơ: " + e.getMessage());
        }
        if (event != null && event.getSource() instanceof Button) ((Button)event.getSource()).requestFocus();
    }

    @FXML void handleLogout(ActionEvent event) {
        if (olympicAnimation != null) { olympicAnimation.stopAnimation(); }
        if (authService != null) { authService.logout(); }
        Stage currentStage = (Stage) mainDashboardPane.getScene().getWindow();
        try { MainApp.showLoginScene(currentStage); } 
        catch (Exception e) { e.printStackTrace(); AlertUtil.showError("Lỗi Đăng Xuất", "Không thể quay lại màn hình đăng nhập.");}
    }

    @FXML void handleManageMedals(ActionEvent event) {
        // Pass true to indicate we want to capture the MedalManagementController instance
        loadViewIntoContentArea("/com/duyphuc/olympics/fxml/MedalManagementView.fxml", true);
        if (event != null && event.getSource() instanceof Button) ((Button)event.getSource()).requestFocus();
    }

    @FXML void handleViewCharts(ActionEvent event) {
        loadViewIntoContentArea("/com/duyphuc/olympics/fxml/ChartView.fxml");
        if (event != null && event.getSource() instanceof Button) ((Button)event.getSource()).requestFocus();
    }

    @FXML void handleManageUsers(ActionEvent event) {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            loadViewIntoContentArea("/com/duyphuc/olympics/fxml/AdminUserManagementView.fxml");
        } else {
            AlertUtil.showError("Truy Cập Bị Từ Chối", "Bạn không có quyền truy cập chức năng này.");
            handleShowDashboardHome(null); // Go back to home
        }
        if (event != null && event.getSource() instanceof Button) ((Button)event.getSource()).requestFocus();
    }

    // <<< NEW METHOD >>>
    @FXML
    void handleManageOlympicEvents(ActionEvent event) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            AlertUtil.showError("Truy Cập Bị Từ Chối", "Bạn không có quyền truy cập chức năng này.");
            handleShowDashboardHome(null); // Go back to home
            if (event != null && event.getSource() instanceof Button) ((Button)event.getSource()).requestFocus();
            return;
        }

        try {
            FXMLLoader loader = FxmlLoaderUtil.getLoader("/com/duyphuc/olympics/fxml/AdminOlympicEventManagementView.fxml");
            Parent root = loader.load();
            
            AdminOlympicEventManagementController controller = loader.getController();
            if (this.medalManagementControllerInstance != null) {
                 controller.setMedalManagementController(this.medalManagementControllerInstance);
            } else {
                // This is a common scenario if MedalManagementView hasn't been opened yet.
                // The AdminOlympicEventManagementController should be designed to function
                // even if medalManagementControllerInstance is null (e.g., not crash).
                System.out.println("MainDashboard: MedalManagementController instance is null when opening AdminOlympicEventManagementView. This is okay if Medal Management screen wasn't opened first.");
            }

            Stage stage = new Stage();
            stage.setTitle("Quản lý Sự kiện Olympic");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(mainDashboardPane.getScene().getWindow());
            stage.setScene(new Scene(root));
             try { stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/duyphuc/olympics/images/Olympic_rings.png"))));
            } catch (Exception e) { System.err.println("Dialog icon not found.");}

            stage.showAndWait();

        } catch (IOException e) {
            AlertUtil.showError("Lỗi Tải Giao Diện", "Không thể tải giao diện Quản lý Sự kiện Olympic: " + e.getMessage());
            e.printStackTrace();
        }
        if (event != null && event.getSource() instanceof Button) ((Button)event.getSource()).requestFocus();
    }

    public void cleanupDashboard() {
        if (olympicAnimation != null) { olympicAnimation.stopAnimation(); }
        // Any other cleanup
    }
}