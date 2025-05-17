package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.OlympicEvent;
import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.service.ChartService;
import com.duyphuc.olympics.service.MedalService;
import com.duyphuc.olympics.service.IChartService;
import com.duyphuc.olympics.service.IMedalService;
import com.duyphuc.olympics.util.AlertUtil;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode; // Đảm bảo import này là chính xác
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    @FXML private VBox controlsPanel;
    @FXML private Label dataScopeLabel;
    @FXML private VBox olympicEventControlsContainer;
    @FXML private Label parametersLabel;
    @FXML private VBox parametersControlsContainer;

    @FXML private Button generateChartButton;
    @FXML private SwingNode swingNodeChart;
    @FXML private StackPane chartContainerPane; // Container của SwingNode

    private IMedalService medalService;
    private IChartService chartService;

    private ObservableList<OlympicEvent> olympicEventsList;
    private ObservableList<String> nocList = FXCollections.observableArrayList();
    private ProgressIndicator loadingIndicator;

    public void initialize() {
        medalService = new MedalService();
        chartService = new ChartService();

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(100, 100);

        if (chartContainerPane != null) {
            chartContainerPane.getChildren().add(loadingIndicator);
            StackPane.setAlignment(loadingIndicator, Pos.CENTER);
        } else {
            System.err.println("ChartViewController: chartContainerPane is null. ProgressIndicator might not be visible.");
        }

        chartTypeComboBox.setItems(FXCollections.observableArrayList(
                "Top N Countries (Bar Chart)",
                "Country Medal Distribution (Pie Chart)",
                "Country Medal Trend (Line Chart)"
        ));
        chartTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateVisibleControls(newVal);
            validateInputsAndToggleButtonState();
        });

        sortByComboBox.setItems(FXCollections.observableArrayList("Total", "Gold", "Silver", "Bronze"));
        sortByComboBox.getSelectionModel().selectFirst();
        sortByComboBox.valueProperty().addListener((obs, ov, nv) -> validateInputsAndToggleButtonState());

        medalTypeComboBox.setItems(FXCollections.observableArrayList("Total", "Gold", "Silver", "Bronze"));
        medalTypeComboBox.getSelectionModel().selectFirst();
        medalTypeComboBox.valueProperty().addListener((obs, ov, nv) -> validateInputsAndToggleButtonState());


        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 10);
        topNSpinner.setValueFactory(valueFactory);
        topNSpinner.valueProperty().addListener((obs, ov, nv) -> validateInputsAndToggleButtonState());


        try {
            olympicEventsList = FXCollections.observableArrayList(medalService.getEvents());
            olympicEventComboBox.setItems(olympicEventsList);
            if (!olympicEventsList.isEmpty()) {
                olympicEventComboBox.getSelectionModel().selectFirst();
            } else {
                validateInputsAndToggleButtonState();
            }
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to load Olympic events: " + e.getMessage());
            validateInputsAndToggleButtonState();
        }

        olympicEventComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldEvent, newEvent) -> {
            if (newEvent != null) {
                loadNOCsForSelectedEvent();
            } else {
                countryComboBox.getItems().clear();
                countryComboBox.setValue(null);
                validateInputsAndToggleButtonState();
            }
        });
        countryComboBox.valueProperty().addListener((obs, ov, nv) -> validateInputsAndToggleButtonState());

        chartTypeComboBox.getSelectionModel().selectFirst();
    }

    private void loadNOCsForSelectedEvent() {
        OlympicEvent selectedEvent = olympicEventComboBox.getValue();
        if (selectedEvent != null) {
            generateChartButton.setDisable(true);
            loadingIndicator.setVisible(true);

            Task<List<String>> loadNocsTask = new Task<>() {
                @Override
                protected List<String> call() throws Exception {
                    return medalService.getAllNOCsForEvent(selectedEvent);
                }

                @Override
                protected void succeeded() {
                    nocList.setAll(getValue());
                    countryComboBox.setItems(nocList);
                    if (!nocList.isEmpty()) {
                        countryComboBox.getSelectionModel().selectFirst();
                    } else {
                        countryComboBox.getItems().clear();
                        countryComboBox.setValue(null);
                    }
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        validateInputsAndToggleButtonState();
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        AlertUtil.showError("Database Error", "Failed to load NOCs for event " + selectedEvent.getEventName() + ": " + getException().getMessage());
                        countryComboBox.getItems().clear();
                        countryComboBox.setValue(null);
                        loadingIndicator.setVisible(false);
                        validateInputsAndToggleButtonState();
                    });
                }
            };
            new Thread(loadNocsTask).start();
        } else {
            countryComboBox.getItems().clear();
            countryComboBox.setValue(null);
            validateInputsAndToggleButtonState();
        }
    }

    private void updateVisibleControls(String chartType) {
        if (chartType == null) return;

        dataScopeLabel.setVisible(true); dataScopeLabel.setManaged(true);
        olympicEventControlsContainer.setVisible(true); olympicEventControlsContainer.setManaged(true);
        parametersLabel.setVisible(true); parametersLabel.setManaged(true);
        parametersControlsContainer.setVisible(true); parametersControlsContainer.setManaged(true);

        olympicEventControls.setVisible(true); olympicEventControls.setManaged(true);
        countryControls.setVisible(true); countryControls.setManaged(true);
        topNControls.setVisible(true); topNControls.setManaged(true);
        medalTypeControls.setVisible(true); medalTypeControls.setManaged(true);

        switch (chartType) {
            case "Top N Countries (Bar Chart)":
                countryControls.setVisible(false); countryControls.setManaged(false);
                medalTypeControls.setVisible(false); medalTypeControls.setManaged(false);
                break;
            case "Country Medal Distribution (Pie Chart)":
                topNControls.setVisible(false); topNControls.setManaged(false);
                medalTypeControls.setVisible(false); medalTypeControls.setManaged(false);
                break;
            case "Country Medal Trend (Line Chart)":
                olympicEventControls.setVisible(false); olympicEventControls.setManaged(false);
                topNControls.setVisible(false); topNControls.setManaged(false);
                break;
        }

        if (!olympicEventControls.isManaged() && !countryControls.isManaged()) {
            dataScopeLabel.setVisible(false); dataScopeLabel.setManaged(false);
            olympicEventControlsContainer.setVisible(false); olympicEventControlsContainer.setManaged(false);
        }
        if (!topNControls.isManaged() && !medalTypeControls.isManaged()) {
            parametersLabel.setVisible(false); parametersLabel.setManaged(false);
            parametersControlsContainer.setVisible(false); parametersControlsContainer.setManaged(false);
        }
    }

    private void validateInputsAndToggleButtonState() {
        boolean disableButton = false;
        String selectedChartType = chartTypeComboBox.getValue();

        if (selectedChartType == null) {
            disableButton = true;
        } else {
            switch (selectedChartType) {
                case "Top N Countries (Bar Chart)":
                    if (olympicEventComboBox.getValue() == null || topNSpinner.getValue() == null || sortByComboBox.getValue() == null) {
                        disableButton = true;
                    }
                    break;
                case "Country Medal Distribution (Pie Chart)":
                    if (olympicEventComboBox.getValue() == null || countryComboBox.getValue() == null || countryComboBox.getValue().trim().isEmpty()) {
                        disableButton = true;
                    }
                    break;
                case "Country Medal Trend (Line Chart)":
                    if (countryComboBox.getValue() == null || countryComboBox.getValue().trim().isEmpty() || medalTypeComboBox.getValue() == null) {
                        disableButton = true;
                    }
                    break;
                default:
                    disableButton = true;
                    break;
            }
        }
        if (loadingIndicator.isVisible()) {
            disableButton = true;
        }
        generateChartButton.setDisable(disableButton);
    }

    /**
     * Cập nhật SwingNode với ChartPanel được cung cấp và yêu cầu JavaFX re-layout.
     * @param chartPanel ChartPanel để hiển thị, hoặc null để xóa biểu đồ.
     */
    private void updateChartDisplay(final ChartPanel chartPanel) {
        SwingUtilities.invokeLater(() -> {
            if (swingNodeChart != null) {
                swingNodeChart.setContent(chartPanel);

                Platform.runLater(() -> {
                    if (chartContainerPane != null) {
                        // THỬ THAY ĐỔI MỘT THUỘC TÍNH NHỎ
                        double originalOpacity = chartContainerPane.getOpacity();
                        chartContainerPane.setOpacity(0.99); // Thay đổi nhỏ
                        // Yêu cầu layout
                        chartContainerPane.requestLayout();

                        // Khôi phục lại sau một chút
                        Platform.runLater(() -> {
                           chartContainerPane.setOpacity(originalOpacity);
                        });
                    }
                });
            }
        });
    }

    @FXML
    private void handleGenerateChart() {
        String selectedChartType = chartTypeComboBox.getValue();
        OlympicEvent selectedEvent = olympicEventControls.isManaged() ? olympicEventComboBox.getValue() : null;
        String selectedNOC = countryControls.isManaged() ? countryComboBox.getValue() : null;
        Integer nValue = topNControls.isManaged() ? topNSpinner.getValue() : null;
        String sortBy = topNControls.isManaged() ? sortByComboBox.getValue() : null;
        String medalType = medalTypeControls.isManaged() ? medalTypeComboBox.getValue() : null;

        if (generateChartButton.isDisabled()) {
            AlertUtil.showWarning("Input Error", "Please ensure all required fields are selected for the chosen chart type.");
            return;
        }

        loadingIndicator.setVisible(true);
        generateChartButton.setDisable(true);
        
        updateChartDisplay(null); // Xóa biểu đồ cũ


        Task<ChartPanel> chartGenerationTask = new Task<>() {
            @Override
            protected ChartPanel call() throws Exception {
                ChartPanel panel = null;
                // ... (logic tạo biểu đồ giữ nguyên từ trước)
                switch (selectedChartType) {
                    case "Top N Countries (Bar Chart)":
                        if (selectedEvent == null) throw new IllegalArgumentException("Olympic event not selected.");
                        if (nValue == null || sortBy == null) throw new IllegalArgumentException("Top N or Sort By not selected.");
                        List<MedalEntry> topNData = medalService.getTopNCountriesForEvent(selectedEvent, nValue, sortBy);
                        if (topNData.isEmpty()) {
                            updateMessage("Không có dữ liệu huy chương cho tiêu chí đã chọn tại " + selectedEvent.getEventName() + ".");
                            return null;
                        }
                        panel = chartService.createTopNCountriesBarChart(topNData, selectedEvent, nValue, sortBy);
                        break;

                    case "Country Medal Distribution (Pie Chart)":
                        if (selectedEvent == null || selectedNOC == null || selectedNOC.trim().isEmpty()) {
                             throw new IllegalArgumentException("Olympic event or NOC not selected.");
                        }
                        MedalEntry countryData = medalService.getMedalDataForCountryInEvent(selectedEvent, selectedNOC);
                        if (countryData == null || (countryData.getGold() == 0 && countryData.getSilver() == 0 && countryData.getBronze() == 0)) {
                            updateMessage("Không có dữ liệu huy chương cho " + selectedNOC + " tại " + selectedEvent.getEventName() + ".");
                            return null;
                        }
                        panel = chartService.createMedalDistributionPieChart(countryData, selectedEvent);
                        break;

                    case "Country Medal Trend (Line Chart)":
                        if (selectedNOC == null || selectedNOC.trim().isEmpty()) throw new IllegalArgumentException("NOC not selected for trend chart.");
                        if (medalType == null) throw new IllegalArgumentException("Medal type not selected for trend chart.");
                        if (olympicEventsList == null || olympicEventsList.isEmpty()) throw new IllegalStateException("Olympic events list is empty for trend analysis.");
                        
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
            updateChartDisplay(resultPanel); 
            
            if (resultPanel == null) {
                String message = chartGenerationTask.getMessage();
                if (message != null && !message.isEmpty() && !message.startsWith("Lỗi:")) {
                    AlertUtil.showInfo("Thông báo", message);
                } else if (message == null || message.isEmpty()) {
                     AlertUtil.showInfo("Thông báo", "Không có dữ liệu để hiển thị biểu đồ với các lựa chọn hiện tại.");
                }
            }
            loadingIndicator.setVisible(false);
            validateInputsAndToggleButtonState();
        });

        chartGenerationTask.setOnFailed(workerStateEvent -> {
            Throwable exception = chartGenerationTask.getException();
            String taskMessage = chartGenerationTask.getMessage();

            if (taskMessage != null && taskMessage.startsWith("Lỗi:")) {
                AlertUtil.showError("Lỗi Tạo Biểu Đồ", taskMessage);
            } else if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {
                 AlertUtil.showError("Lỗi Đầu Vào", exception.getMessage());
            }
            else {
                AlertUtil.showError("Lỗi Tạo Biểu Đồ", "Đã xảy ra lỗi: " + (exception != null ? exception.getMessage() : "Unknown error"));
            }

            if (exception != null) {
                exception.printStackTrace();
            }
            updateChartDisplay(null); 
            loadingIndicator.setVisible(false);
            validateInputsAndToggleButtonState();
        });

        new Thread(chartGenerationTask).start();
    }
}