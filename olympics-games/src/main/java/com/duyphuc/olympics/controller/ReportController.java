package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;
import com.duyphuc.olympics.service.MedalService;
import com.duyphuc.olympics.service.ReportService;
import com.duyphuc.olympics.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReportController {

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<OlympicEvent> olympicEventComboBox;
    @FXML private TextField paramNocInput;
    @FXML private Spinner<Integer> paramNSpinner;
    @FXML private TextArea reportOutputTextArea;

    @FXML private Label olympicEventLabel;
    @FXML private Label paramNocLabel;
    @FXML private Label paramNLabel;
    @FXML private Button generateReportButton;
    @FXML private Button exportReportButton;

    private ReportService reportService;
    private MedalService medalService; // Cần để lấy danh sách Olympic Events

    private ProgressIndicator loadingIndicator; // Để hiển thị khi đang tải

    public void initialize() {
        // Khởi tạo service
        // Trong một ứng dụng lớn hơn, bạn có thể dùng Dependency Injection (DI)
        // hoặc một ServiceLocator pattern.
        this.medalService = new MedalService(/*Khởi tạo DAO nếu cần*/);
        this.reportService = new ReportService(this.medalService);

        // Khởi tạo ProgressIndicator (thêm vào FXML nếu muốn có vị trí cố định)
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        // Ví dụ: reportViewPane.getChildren().add(loadingIndicator); nếu muốn nó ở cuối VBox.
        // Hoặc bạn có thể tạo một StackPane bao quanh TextArea và đặt nó ở đó.

        setupReportTypeComboBox();
        setupOlympicEventComboBox();
        updateVisibleControls(null); // Ẩn các control không cần thiết ban đầu

        exportReportButton.setVisible(false); // Ẩn nút xuất ban đầu
    }

    private void setupReportTypeComboBox() {
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Bảng Xếp Hạng Tổng Thể (Theo Kỳ)",
                "Quốc Gia Nhiều HCV Nhất (Theo Kỳ)",
                "Tổng Huy Chương Được Trao (Theo Kỳ)",
                "Top N Quốc Gia (Theo Kỳ - Tổng HC)",
                "Thành Tích Quốc Gia Qua Các Kỳ",
                "Bảng Xếp Hạng Tổng Thể (Tất Cả Các Kỳ)"
        ));
        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateVisibleControls(newVal));
    }

    private void setupOlympicEventComboBox() {
        try {
            List<OlympicEvent> events = medalService.getEvents();
            olympicEventComboBox.setItems(FXCollections.observableArrayList(events));
            olympicEventComboBox.setConverter(new StringConverter<OlympicEvent>() {
                @Override
                public String toString(OlympicEvent event) {
                    return event == null ? null : event.getEventName() + " (" + event.getYear() + ")";
                }

                @Override
                public OlympicEvent fromString(String string) {
                    return null; // Không cần implement
                }
            });
        } catch (SQLException e) {
            AlertUtil.showError("Lỗi Tải Dữ Liệu", "Không thể tải danh sách các kỳ Olympic: " + e.getMessage());
        }
    }

    private void updateVisibleControls(String selectedReportType) {
        // Ẩn tất cả các control tùy chọn trước
        olympicEventLabel.setVisible(false);
        olympicEventComboBox.setVisible(false);
        paramNocLabel.setVisible(false);
        paramNocInput.setVisible(false);
        paramNLabel.setVisible(false);
        paramNSpinner.setVisible(false);

        if (selectedReportType == null) return;

        switch (selectedReportType) {
            case "Bảng Xếp Hạng Tổng Thể (Theo Kỳ)":
            case "Quốc Gia Nhiều HCV Nhất (Theo Kỳ)":
            case "Tổng Huy Chương Được Trao (Theo Kỳ)":
                olympicEventLabel.setVisible(true);
                olympicEventComboBox.setVisible(true);
                break;
            case "Top N Quốc Gia (Theo Kỳ - Tổng HC)":
                olympicEventLabel.setVisible(true);
                olympicEventComboBox.setVisible(true);
                paramNLabel.setVisible(true);
                paramNSpinner.setVisible(true);
                break;
            case "Thành Tích Quốc Gia Qua Các Kỳ":
                paramNocLabel.setVisible(true);
                paramNocInput.setVisible(true);
                break;
            case "Bảng Xếp Hạng Tổng Thể (Tất Cả Các Kỳ)":
                paramNLabel.setVisible(true);
                paramNSpinner.setVisible(true);
                break;
        }
    }

    @FXML
    void handleGenerateReport(ActionEvent event) {
        String selectedType = reportTypeComboBox.getValue();
        if (selectedType == null) {
            AlertUtil.showWarning("Chưa Chọn Báo Cáo", "Vui lòng chọn một loại báo cáo để tạo.");
            return;
        }

        reportOutputTextArea.clear();
        exportReportButton.setVisible(false); // Ẩn nút xuất khi bắt đầu tạo báo cáo mới
        loadingIndicator.setVisible(true);
        generateReportButton.setDisable(true);

        Task<String> reportTask = new Task<>() {
            @Override
            protected String call() throws Exception { // SQLException được Task coi là Exception
                switch (selectedType) {
                    case "Bảng Xếp Hạng Tổng Thể (Theo Kỳ)":
                        return generateRankingReportForEventTask();
                    case "Quốc Gia Nhiều HCV Nhất (Theo Kỳ)":
                        return getCountryWithMostGoldMedalsInEventTask();
                    case "Tổng Huy Chương Được Trao (Theo Kỳ)":
                        return getTotalMedalsAwardedInEventTask();
                    case "Top N Quốc Gia (Theo Kỳ - Tổng HC)":
                        return getTopNCountriesByTotalMedalsInEventTask();
                    case "Thành Tích Quốc Gia Qua Các Kỳ":
                        return getCountryPerformanceAcrossEventsTask();
                    case "Bảng Xếp Hạng Tổng Thể (Tất Cả Các Kỳ)":
                        return getOverallLeaderboardAllEventsTask();
                    default:
                        return "Loại báo cáo không được hỗ trợ.";
                }
            }
        };

        reportTask.setOnSucceeded(e -> {
            reportOutputTextArea.setText(reportTask.getValue());
            exportReportButton.setVisible(!reportTask.getValue().isEmpty() && !reportTask.getValue().startsWith("Lỗi:"));
            loadingIndicator.setVisible(false);
            generateReportButton.setDisable(false);
        });

        reportTask.setOnFailed(e -> {
            Throwable ex = reportTask.getException();
            reportOutputTextArea.setText("Lỗi khi tạo báo cáo: " + ex.getMessage());
            AlertUtil.showError("Lỗi Tạo Báo Cáo", "Đã xảy ra lỗi: " + ex.getMessage());
            ex.printStackTrace();
            loadingIndicator.setVisible(false);
            generateReportButton.setDisable(false);
        });

        new Thread(reportTask).start();
    }

    // Các phương thức helper để thực hiện logic cho từng loại báo cáo trong Task
    private String generateRankingReportForEventTask() throws SQLException {
        OlympicEvent selectedEvent = olympicEventComboBox.getValue();
        if (selectedEvent == null) return "Lỗi: Vui lòng chọn một kỳ Olympic.";
        List<MedalEntry> ranking = reportService.generateOverallRankingReportForEvent(selectedEvent);
        return formatRankingData(ranking, "Bảng Xếp Hạng Huy Chương - " + selectedEvent.getEventName());
    }

    private String getCountryWithMostGoldMedalsInEventTask() throws SQLException {
        OlympicEvent selectedEvent = olympicEventComboBox.getValue();
        if (selectedEvent == null) return "Lỗi: Vui lòng chọn một kỳ Olympic.";
        Optional<MedalEntry> mostGold = reportService.getCountryWithMostGoldMedalsInEvent(selectedEvent);
        if (mostGold.isPresent()) {
            MedalEntry entry = mostGold.get();
            return "Quốc gia nhiều HCV nhất tại " + selectedEvent.getEventName() + ":\n" +
                   "NOC: " + entry.getNoc() + "\n" +
                   "Số HCV: " + entry.getGold();
        } else {
            return "Không có dữ liệu hoặc không có HCV nào được trao tại " + selectedEvent.getEventName();
        }
    }

    private String getTotalMedalsAwardedInEventTask() throws SQLException {
        OlympicEvent selectedEvent = olympicEventComboBox.getValue();
        if (selectedEvent == null) return "Lỗi: Vui lòng chọn một kỳ Olympic.";
        Map<String, Integer> totalMedals = reportService.getTotalMedalsAwardedInEvent(selectedEvent);
        return "Tổng Huy Chương Được Trao Tại " + selectedEvent.getEventName() + ":\n" +
               "Vàng: " + totalMedals.getOrDefault("Gold", 0) + "\n" +
               "Bạc: " + totalMedals.getOrDefault("Silver", 0) + "\n" +
               "Đồng: " + totalMedals.getOrDefault("Bronze", 0) + "\n" +
               "Tổng Cộng: " + totalMedals.getOrDefault("Total", 0);
    }

    private String getTopNCountriesByTotalMedalsInEventTask() throws SQLException {
        OlympicEvent selectedEvent = olympicEventComboBox.getValue();
        if (selectedEvent == null) return "Lỗi: Vui lòng chọn một kỳ Olympic.";
        int n = paramNSpinner.getValue();
        List<MedalEntry> topN = reportService.getTopNCountriesByTotalMedalsInEvent(selectedEvent, n);
        return formatRankingData(topN, "Top " + n + " Quốc Gia (Theo Tổng Huy Chương) - " + selectedEvent.getEventName());
    }

    private String getCountryPerformanceAcrossEventsTask() throws SQLException {
        String noc = paramNocInput.getText();
        if (noc == null || noc.trim().isEmpty()) return "Lỗi: Vui lòng nhập mã quốc gia (NOC).";
        List<MedalEntry> performance = reportService.getCountryPerformanceAcrossEvents(noc.toUpperCase().trim());
        return formatCountryPerformanceData(performance, "Thành Tích Của Quốc Gia " + noc.toUpperCase().trim() + " Qua Các Kỳ Olympic");
    }

    private String getOverallLeaderboardAllEventsTask() throws SQLException {
        int n = paramNSpinner.getValue();
        List<MedalEntry> overallLeaderboard = reportService.getOverallLeaderboardAllEvents(n);
        return formatRankingData(overallLeaderboard, "Bảng Xếp Hạng Tổng Thể Tất Cả Các Kỳ (Top " + n + ")");
    }


    // --- Các hàm định dạng dữ liệu báo cáo ---
    private String formatRankingData(List<MedalEntry> entries, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        sb.append("====================================================\n");
        sb.append(String.format("%-6s %-8s %-7s %-7s %-7s %-7s\n", "Hạng", "NOC", "Vàng", "Bạc", "Đồng", "Tổng"));
        sb.append("----------------------------------------------------\n");
        if (entries == null || entries.isEmpty()) {
            sb.append("Không có dữ liệu để hiển thị.\n");
        } else {
            for (int i = 0; i < entries.size(); i++) {
                MedalEntry entry = entries.get(i);
                sb.append(String.format("%-6d %-8s %-7d %-7d %-7d %-7d\n",
                        (i + 1), entry.getNoc(), entry.getGold(), entry.getSilver(), entry.getBronze(), entry.getTotal()));
            }
        }
        sb.append("====================================================\n");
        return sb.toString();
    }

    private String formatCountryPerformanceData(List<MedalEntry> entries, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        sb.append("==========================================================================\n");
        sb.append(String.format("%-12s %-18s %-7s %-7s %-7s %-7s\n", "Năm", "Loại Sự Kiện", "Vàng", "Bạc", "Đồng", "Tổng"));
        sb.append("--------------------------------------------------------------------------\n");
        if (entries == null || entries.isEmpty()) {
            sb.append("Không có dữ liệu thành tích cho quốc gia này.\n");
        } else {
            for (MedalEntry entry : entries) {
                sb.append(String.format("%-12d %-18s %-7d %-7d %-7d %-7d\n",
                        entry.getOlympicEventYear(),
                        entry.getOlympicEventType() != null ? entry.getOlympicEventType() : "N/A",
                        entry.getGold(), entry.getSilver(), entry.getBronze(), entry.getTotal()));
            }
        }
        sb.append("==========================================================================\n");
        return sb.toString();
    }

    @FXML
    void handleExportReport(ActionEvent event) {
        String reportContent = reportOutputTextArea.getText();
        if (reportContent == null || reportContent.trim().isEmpty()) {
            AlertUtil.showWarning("Không Có Dữ Liệu", "Không có nội dung báo cáo để xuất.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo Cáo");
        fileChooser.setInitialFileName("OlympicReport.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(reportOutputTextArea.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(reportContent);
                AlertUtil.showInfo("Xuất Thành Công", "Báo cáo đã được lưu vào: " + file.getAbsolutePath());
            } catch (IOException e) {
                AlertUtil.showError("Lỗi Lưu File", "Không thể lưu báo cáo: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}