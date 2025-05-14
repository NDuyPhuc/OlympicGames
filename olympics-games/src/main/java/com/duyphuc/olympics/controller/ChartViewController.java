package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.OlympicEvent;
import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.service.ChartService;
import com.duyphuc.olympics.service.MedalService;
import com.duyphuc.olympics.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task; // <<<--- IMPORT Task
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
// import javafx.scene.control.Label; // Không thấy sử dụng trực tiếp trong FXML, có thể xóa
import javafx.scene.control.ProgressIndicator; // <<<--- IMPORT ProgressIndicator
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane; // <<<--- IMPORT StackPane (hoặc một Pane khác để chứa ProgressIndicator)
import org.jfree.chart.ChartPanel;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ChartViewController {

    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private ComboBox<OlympicEvent> olympicEventComboBox;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private Spinner<Integer> topNSpinner;
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private ComboBox<String> medalTypeComboBox;

    @FXML private HBox olympicEventControls;
    @FXML private HBox countryControls;
    @FXML private HBox topNControls;
    @FXML private HBox medalTypeControls;

    @FXML private Button generateChartButton;
    @FXML private SwingNode swingNodeChart;
    @FXML private StackPane chartContainerPane; // <<<--- THÊM StackPane trong FXML để chứa SwingNode và ProgressIndicator

    private MedalService medalService;
    private ChartService chartService;

    private ObservableList<OlympicEvent> olympicEventsList;
    private ObservableList<String> nocList = FXCollections.observableArrayList();
    private ProgressIndicator loadingIndicator; // <<<--- Biến cho ProgressIndicator

    public void initialize() {
        medalService = new MedalService();
        chartService = new ChartService();

        // Khởi tạo ProgressIndicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false); // Ban đầu ẩn
        // Đảm bảo chartContainerPane đã được khởi tạo từ FXML trước khi thêm
        // Nếu chartContainerPane chưa có trong FXML, bạn cần tạo nó hoặc dùng một Pane đã có
        if (chartContainerPane != null) {
             // Đặt ProgressIndicator lên trên SwingNode
            chartContainerPane.getChildren().add(loadingIndicator);
        } else {
            System.err.println("ChartViewController: chartContainerPane is null. ProgressIndicator might not be visible.");
            // Nếu không có chartContainerPane, bạn có thể đặt loadingIndicator ở vị trí khác
            // hoặc đơn giản là không hiển thị nó nếu không có Pane phù hợp.
        }


        // Populate Chart Types
        chartTypeComboBox.setItems(FXCollections.observableArrayList(
                "Top N Countries (Bar Chart)",
                "Country Medal Distribution (Pie Chart)",
                "Country Medal Trend (Line Chart)"
        ));
        chartTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateVisibleControls(newVal));
        chartTypeComboBox.getSelectionModel().selectFirst();

        // Populate Sort By options
        sortByComboBox.setItems(FXCollections.observableArrayList("Total", "Gold", "Silver", "Bronze"));
        sortByComboBox.getSelectionModel().selectFirst();

        // Populate Medal Type options
        medalTypeComboBox.setItems(FXCollections.observableArrayList("Total", "Gold", "Silver", "Bronze"));
        medalTypeComboBox.getSelectionModel().selectFirst();

        // Load Olympic Events (có thể cũng đưa ra luồng nền nếu danh sách quá lớn)
        // Hiện tại, việc load này thường nhanh nên có thể giữ ở đây
        try {
            olympicEventsList = FXCollections.observableArrayList(medalService.getEvents());
            olympicEventComboBox.setItems(olympicEventsList);
            if (!olympicEventsList.isEmpty()) {
                olympicEventComboBox.getSelectionModel().selectFirst();
                loadNOCsForSelectedEvent();
            }
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to load Olympic events: " + e.getMessage());
        }

        olympicEventComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldEvent, newEvent) -> {
            if (newEvent != null) {
                loadNOCsForSelectedEvent();
            }
        });
    }

    private void loadNOCsForSelectedEvent() {
        OlympicEvent selectedEvent = olympicEventComboBox.getValue();
        if (selectedEvent != null) {
            // Việc load NOCs cũng có thể được đưa ra luồng nền nếu danh sách quá dài
            // và gây trễ khi chọn OlympicEvent.
            // Hiện tại giữ đơn giản.
            try {
                nocList.setAll(medalService.getAllNOCsForEvent(selectedEvent));
                countryComboBox.setItems(nocList);
                if(!nocList.isEmpty()){
                    countryComboBox.getSelectionModel().selectFirst();
                } else {
                    countryComboBox.getItems().clear(); // Xóa NOCs cũ nếu event mới không có NOC
                    countryComboBox.setValue(null);     // Reset giá trị đang chọn
                }
            } catch (SQLException e) {
                 AlertUtil.showError("Database Error", "Failed to load NOCs for event " + selectedEvent.getEventName() + ": " + e.getMessage());
            }
        } else {
            countryComboBox.getItems().clear();
            countryComboBox.setValue(null);
        }
    }


    private void updateVisibleControls(String chartType) {
        // ... (giữ nguyên code của bạn) ...
        if (chartType == null) return;

        olympicEventControls.setVisible(true);
        olympicEventControls.setManaged(true);
        countryControls.setVisible(true);
        countryControls.setManaged(true);
        topNControls.setVisible(true);
        topNControls.setManaged(true);
        medalTypeControls.setVisible(true);
        medalTypeControls.setManaged(true);


        switch (chartType) {
            case "Top N Countries (Bar Chart)":
                countryControls.setVisible(false);
                countryControls.setManaged(false);
                medalTypeControls.setVisible(false);
                medalTypeControls.setManaged(false);
                break;
            case "Country Medal Distribution (Pie Chart)":
                topNControls.setVisible(false);
                topNControls.setManaged(false);
                medalTypeControls.setVisible(false);
                medalTypeControls.setManaged(false);
                break;
            case "Country Medal Trend (Line Chart)":
                olympicEventControls.setVisible(false); // Trend is across all available events
                olympicEventControls.setManaged(false);
                topNControls.setVisible(false);
                topNControls.setManaged(false);
                if (olympicEventsList != null && !olympicEventsList.isEmpty() && (countryComboBox.getItems() == null || countryComboBox.getItems().isEmpty())) {
                     // Cân nhắc load danh sách tất cả NOCs một lần cho Trend chart
                     // Hoặc để người dùng tự nhập
                     // Hiện tại, nếu chọn Olympic Event trước đó, NOC list sẽ còn
                     // Nếu không, có thể cần cơ chế load NOCs riêng cho Trend
                }
                break;
            default:
                break;
        }
    }

    @FXML
    private void handleGenerateChart() {
        String selectedChartType = chartTypeComboBox.getValue();
        OlympicEvent selectedEvent = olympicEventComboBox.getValue(); // Có thể null nếu không được chọn
        String selectedNOC = countryComboBox.getValue(); // Có thể null
        Integer nValue = topNSpinner.getValue(); // Spinner trả về Integer
        String sortBy = sortByComboBox.getValue();
        String medalType = medalTypeComboBox.getValue();

        if (selectedChartType == null) {
            AlertUtil.showWarning("Input Error", "Please select a chart type.");
            return;
        }

        // Hiển thị ProgressIndicator và vô hiệu hóa nút
        loadingIndicator.setVisible(true);
        generateChartButton.setDisable(true);
        swingNodeChart.setContent(null); // Xóa biểu đồ cũ trong khi tải

        Task<ChartPanel> chartGenerationTask = new Task<>() {
            @Override
            protected ChartPanel call() throws Exception { // SQLException sẽ được coi là Exception
                // Các thao tác nặng sẽ được thực hiện ở đây (trong luồng nền)
                ChartPanel panel = null;
                switch (selectedChartType) {
                    case "Top N Countries (Bar Chart)":
                        if (selectedEvent == null) {
                            updateMessage("Lỗi: Vui lòng chọn một kỳ Olympic."); // Gửi thông báo cho onFailed/onSucceeded
                            throw new IllegalArgumentException("Olympic event not selected.");
                        }
                        List<MedalEntry> topNData = medalService.getTopNCountriesForEvent(selectedEvent, nValue, sortBy);
                        if (topNData.isEmpty()) {
                            updateMessage("Không có dữ liệu huy chương cho tiêu chí đã chọn.");
                            return null; // Trả về null để onSucceeded xử lý
                        }
                        panel = chartService.createTopNCountriesBarChart(topNData, selectedEvent, nValue, sortBy);
                        break;

                    case "Country Medal Distribution (Pie Chart)":
                        if (selectedEvent == null || selectedNOC == null || selectedNOC.trim().isEmpty()) {
                            updateMessage("Lỗi: Vui lòng chọn một kỳ Olympic và một quốc gia.");
                            throw new IllegalArgumentException("Olympic event or NOC not selected.");
                        }
                        MedalEntry countryData = medalService.getMedalDataForCountryInEvent(selectedEvent, selectedNOC);
                        // ChartService đã xử lý countryData == null, nên không cần kiểm tra ở đây nữa
                        panel = chartService.createMedalDistributionPieChart(countryData, selectedEvent);
                        break;

                    case "Country Medal Trend (Line Chart)":
                        if (selectedNOC == null || selectedNOC.trim().isEmpty()) {
                            updateMessage("Lỗi: Vui lòng chọn hoặc nhập mã quốc gia (NOC).");
                            throw new IllegalArgumentException("NOC not selected for trend chart.");
                        }
                        if (olympicEventsList == null || olympicEventsList.isEmpty()) {
                            updateMessage("Lỗi: Không có dữ liệu các kỳ Olympic để phân tích xu hướng.");
                            throw new IllegalStateException("Olympic events list is empty for trend analysis.");
                        }
                        Map<Integer, Integer> trendData = medalService.getMedalTrendForCountry(selectedNOC, olympicEventsList, medalType);
                        if (trendData.isEmpty()) {
                            updateMessage("Không có dữ liệu xu hướng huy chương cho " + selectedNOC + " với loại " + medalType + ".");
                            return null;
                        }
                        panel = chartService.createCountryTrendLineChart(trendData, selectedNOC, medalType + " Medals");
                        break;
                }
                return panel;
            }
        };

        chartGenerationTask.setOnSucceeded(workerStateEvent -> {
            ChartPanel resultPanel = chartGenerationTask.getValue();
            if (resultPanel != null) {
                setChart(resultPanel);
            } else {
                // Hiển thị thông báo nếu task trả về null (ví dụ: không có dữ liệu)
                String message = chartGenerationTask.getMessage();
                if (message != null && !message.isEmpty() && !message.startsWith("Lỗi:")) { // Chỉ hiển thị nếu không phải lỗi đã throw
                    AlertUtil.showInfo("Thông báo", message);
                }
                setChart(null); // Xóa biểu đồ cũ
            }
            loadingIndicator.setVisible(false);
            generateChartButton.setDisable(false);
        });

        chartGenerationTask.setOnFailed(workerStateEvent -> {
            Throwable exception = chartGenerationTask.getException();
            String taskMessage = chartGenerationTask.getMessage(); // Lấy thông báo từ updateMessage() nếu có

            if (taskMessage != null && taskMessage.startsWith("Lỗi:")) {
                AlertUtil.showError("Lỗi Tạo Biểu Đồ", taskMessage);
            } else {
                AlertUtil.showError("Lỗi Tạo Biểu Đồ", "Đã xảy ra lỗi: " + exception.getMessage());
            }
            exception.printStackTrace(); // Luôn in stack trace để debug
            setChart(null); // Xóa biểu đồ
            loadingIndicator.setVisible(false);
            generateChartButton.setDisable(false);
        });

        // Chạy Task trên một Thread Pool mặc định của JavaFX
        new Thread(chartGenerationTask).start();
    }

    private void setChart(final ChartPanel chartPanel) {
        SwingUtilities.invokeLater(() -> {
            swingNodeChart.setContent(chartPanel);
        });
    }
}