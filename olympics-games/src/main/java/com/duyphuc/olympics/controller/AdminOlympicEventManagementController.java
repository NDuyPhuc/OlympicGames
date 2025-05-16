package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.OlympicEvent;
import com.duyphuc.olympics.service.MedalService;
import com.duyphuc.olympics.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminOlympicEventManagementController {

    @FXML private ListView<OlympicEvent> olympicEventsListView;
    @FXML private TextField filterField;
    @FXML private Button addNewEventButton;
    @FXML private Button deleteEventButton;
    @FXML private Button refreshButton;

    @FXML private GridPane eventFormPane;
    @FXML private TextField eventNameField;
    @FXML private TextField eventYearField;
    @FXML private ComboBox<String> eventTypeComboBox;
    @FXML private Button saveEventButton;
    @FXML private Button cancelEventButton;

    private MedalService medalService;
    private ObservableList<OlympicEvent> olympicEvents = FXCollections.observableArrayList();
    private FilteredList<OlympicEvent> filteredOlympicEvents;

    // To refresh MedalManagementController's ComboBox
    private MedalManagementController medalManagementController; 

    public void setMedalManagementController(MedalManagementController controller) {
        this.medalManagementController = controller;
    }

    @FXML
    public void initialize() {
        medalService = new MedalService();

        eventTypeComboBox.setItems(FXCollections.observableArrayList("Summer", "Winter"));

        filteredOlympicEvents = new FilteredList<>(olympicEvents, p -> true);
        olympicEventsListView.setItems(filteredOlympicEvents);
        olympicEventsListView.setCellFactory(lv -> new ListCell<OlympicEvent>() {
            @Override
            protected void updateItem(OlympicEvent event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(event.toString() + (event.getTableNameInDb() != null ? " [Table: " + event.getTableNameInDb() + "]" : " [No Table Linked]"));
                }
            }
        });


        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredOlympicEvents.setPredicate(event -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return event.getEventName().toLowerCase().contains(lowerCaseFilter) ||
                       String.valueOf(event.getYear()).contains(lowerCaseFilter);
            });
        });

        olympicEventsListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> deleteEventButton.setDisable(newSelection == null)
        );

        loadOlympicEvents();
        hideEventForm();
    }

    private void loadOlympicEvents() {
        try {
            List<OlympicEvent> events = medalService.getEvents();
            olympicEvents.setAll(events);
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to load Olympic events: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshList() {
        loadOlympicEvents();
        if (medalManagementController != null) {
            medalManagementController.refreshOlympicEventsComboBox();
        }
    }
    
    @FXML
    private void handleAddNewEvent() {
        eventNameField.clear();
        eventYearField.clear();
        eventTypeComboBox.getSelectionModel().clearSelection();
        showEventForm();
    }

    @FXML
    private void handleDeleteEvent() {
        OlympicEvent selectedEvent = olympicEventsListView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            AlertUtil.showWarning("No Selection", "Please select an Olympic event to delete.");
            return;
        }

        if (selectedEvent.getTableNameInDb() == null || selectedEvent.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(selectedEvent.getTableNameInDb())) {
             boolean confirmNoTable = AlertUtil.showConfirmation("Confirm Delete",
                    "The selected event '" + selectedEvent.getEventName() + "' does not have an associated medal table or it's pending. " +
                    "Do you want to delete only the event record?");
            if (!confirmNoTable) return;
        } else {
            boolean confirmed = AlertUtil.showConfirmation("Confirm Delete",
                    "Are you sure you want to delete the event '" + selectedEvent.getEventName() +
                    "' and its associated medal table '" + selectedEvent.getTableNameInDb() + "'?\n" +
                    "ALL MEDAL DATA FOR THIS EVENT WILL BE LOST PERMANENTLY.");
            if (!confirmed) return;
        }


        try {
            medalService.deleteOlympicEventWithTable(selectedEvent);
            AlertUtil.showInfo("Success", "Olympic event and its table (if existed) deleted successfully.");
            loadOlympicEvents(); // Refresh list
            if (medalManagementController != null) {
                medalManagementController.refreshOlympicEventsComboBox();
            }
        } catch (SQLException e) {
            AlertUtil.showError("Deletion Failed", "Could not delete the Olympic event: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Error", "Invalid event data for deletion: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveEvent() {
        String eventName = eventNameField.getText().trim();
        String yearText = eventYearField.getText().trim();
        String eventType = eventTypeComboBox.getSelectionModel().getSelectedItem();

        if (eventName.isEmpty() || yearText.isEmpty() || eventType == null) {
            AlertUtil.showError("Input Error", "All fields (Event Name, Year, Type) are required.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
            if (year < 1896 || year > 2200) { // Basic validation for year
                AlertUtil.showError("Input Error", "Please enter a valid year (e.g., 1896-2200).");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Year must be a valid number.");
            return;
        }

        try {
            OlympicEvent newEvent = medalService.createOlympicEventWithTable(eventName, year, eventType);
            AlertUtil.showInfo("Success", "New Olympic Event '" + newEvent.getEventName() +
                                          "' and its medal table '" + newEvent.getTableNameInDb() +
                                          "' created successfully.");
            loadOlympicEvents(); // Refresh the list
            hideEventForm();
            if (medalManagementController != null) {
                medalManagementController.refreshOlympicEventsComboBox();
            }
        } catch (SQLException e) {	
            AlertUtil.showError("Creation Failed", "Could not create the Olympic event and table: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelEventForm() {
        hideEventForm();
    }

    private void showEventForm() {
        eventFormPane.setVisible(true);
        eventFormPane.setManaged(true);
        olympicEventsListView.setDisable(true); // Disable list while form is active
        addNewEventButton.setDisable(true);
        deleteEventButton.setDisable(true);
        refreshButton.setDisable(true);
    }

    private void hideEventForm() {
        eventFormPane.setVisible(false);
        eventFormPane.setManaged(false);
        olympicEventsListView.setDisable(false);
        addNewEventButton.setDisable(false);
        // deleteButton state depends on selection, re-evaluate or rely on selection listener
        deleteEventButton.setDisable(olympicEventsListView.getSelectionModel().getSelectedItem() == null);
        refreshButton.setDisable(false);
    }
}