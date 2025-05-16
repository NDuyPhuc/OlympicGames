//MedalManagementController.java
package com.duyphuc.olympics.controller;

import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;
import com.duyphuc.olympics.service.MedalService;
import com.duyphuc.olympics.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional; // <<<--- ADD THIS IMPORT

public class MedalManagementController {

    @FXML private BorderPane rootPane;
    @FXML private ComboBox<OlympicEvent> olympicEventComboBox;
    @FXML private TableView<MedalEntry> medalTableView;
    @FXML private TableColumn<MedalEntry, String> nocColumn;
    @FXML private TableColumn<MedalEntry, Integer> goldColumn;
    @FXML private TableColumn<MedalEntry, Integer> silverColumn;
    @FXML private TableColumn<MedalEntry, Integer> bronzeColumn;
    @FXML private TableColumn<MedalEntry, Integer> totalColumn;

    @FXML private GridPane formPane;
    @FXML private TextField nocTextField;
    @FXML private TextField goldTextField;
    @FXML private TextField silverTextField;
    @FXML private TextField bronzeTextField;
    @FXML private TextField searchTextField; // For extra feature

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    private MedalService medalService;
    private ObservableList<MedalEntry> medalEntries = FXCollections.observableArrayList();
    private FilteredList<MedalEntry> filteredData; // For extra feature
    private MedalEntry selectedMedalEntry;

    @FXML
    public void initialize() {
        medalService = new MedalService();

        setupTableColumns();
        // loadOlympicEvents(); // <<<--- REMOVE OR COMMENT OUT THIS OLD CALL
        refreshOlympicEventsComboBox(); // <<<--- ADD THIS CALL
        setupEventListeners();

        // (Extra) Setup search functionality
        setupSearchFilter();

        // Initially disable form and update/delete buttons
        formPane.setDisable(true);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void setupTableColumns() {
        nocColumn.setCellValueFactory(new PropertyValueFactory<>("noc"));
        goldColumn.setCellValueFactory(new PropertyValueFactory<>("gold"));
        silverColumn.setCellValueFactory(new PropertyValueFactory<>("silver"));
        bronzeColumn.setCellValueFactory(new PropertyValueFactory<>("bronze"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    /**
     * Reloads the Olympic events into the ComboBox.
     * Can be called externally when the list of events changes.
     */
    public void refreshOlympicEventsComboBox() { // <<<--- THIS IS THE NEW/MODIFIED METHOD
        OlympicEvent selectedCurrently = olympicEventComboBox.getSelectionModel().getSelectedItem();
        // olympicEventComboBox.getItems().clear(); // Clearing and re-setting items can sometimes lose selection state or cause minor UI flickers.
                                                // A more robust way is to update the underlying list of the ComboBox if it's already an ObservableList.
                                                // However, for simplicity and given it's usually a full refresh, setItems is common.

        try {
            List<OlympicEvent> events = medalService.getEvents();
            ObservableList<OlympicEvent> observableEvents = FXCollections.observableArrayList(events);
            olympicEventComboBox.setItems(observableEvents);

            // Try to re-select previously selected item if it still exists
            if (selectedCurrently != null) {
                Optional<OlympicEvent> reselect = events.stream()
                                                       .filter(e -> e.getId() == selectedCurrently.getId())
                                                       .findFirst();
                if (reselect.isPresent()) {
                    olympicEventComboBox.getSelectionModel().select(reselect.get());
                } else {
                    // If previously selected item no longer exists, clear selection and related UI
                    olympicEventComboBox.getSelectionModel().clearSelection();
                    // medalEntries.clear(); // This will be handled by the listener on selectedItemProperty if it becomes null
                    // formPane.setDisable(true);
                    // clearFormFields();
                }
            }
            // If no event is selected (either initially or after failing to re-select), ensure UI state is correct.
            // The listener on olympicEventComboBox.getSelectionModel().selectedItemProperty() should handle this.
             if (olympicEventComboBox.getSelectionModel().getSelectedItem() == null) {
                medalEntries.clear();
                formPane.setDisable(true);
                clearFormFields(); // Also clear form
            }

        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to reload Olympic events: " + e.getMessage());
            olympicEventComboBox.getItems().clear(); // Clear items on error
            olympicEventComboBox.setDisable(true);
            medalEntries.clear(); // Clear table
            formPane.setDisable(true); // Disable form
            clearFormFields();
        }
    }

    // The old private loadOlympicEvents() method can be removed if refreshOlympicEventsComboBox() completely replaces its functionality.
    // If you choose to keep it for some internal reason, ensure it's not the one being called by initialize() anymore.
    // For this fix, I'm assuming refreshOlympicEventsComboBox replaces it.
    /*
    private void loadOlympicEvents() { // <<<--- THIS CAN BE REMOVED OR KEPT IF USED ELSEWHERE INTERNALLY (but not from initialize)
        try {
            List<OlympicEvent> events = medalService.getEvents();
            olympicEventComboBox.setItems(FXCollections.observableArrayList(events));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to load Olympic events: " + e.getMessage());
            olympicEventComboBox.setDisable(true);
        }
    }
    */


    private void setupEventListeners() {
        olympicEventComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldEvent, newEvent) -> {
            if (newEvent != null) {
                loadMedalDataForSelectedEvent(newEvent);
                formPane.setDisable(false);
            } else {
                medalEntries.clear();
                formPane.setDisable(true);
                clearFormFields(); // Good to clear form if no event is selected
                // Also ensure action buttons are disabled
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        medalTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedMedalEntry = newSelection;
            populateFormWithSelectedMedal(newSelection);
            updateButton.setDisable(newSelection == null);
            deleteButton.setDisable(newSelection == null);
        });
    }

    private void setupSearchFilter() {
        filteredData = new FilteredList<>(medalEntries, p -> true);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(medalEntry -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return medalEntry.getNoc() != null && medalEntry.getNoc().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<MedalEntry> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(medalTableView.comparatorProperty());
        medalTableView.setItems(sortedData);
    }


    private void loadMedalDataForSelectedEvent(OlympicEvent event) {
        try {
            List<MedalEntry> medals = medalService.getMedalDataForEvent(event);
            medalEntries.setAll(medals); // This will update the FilteredList -> SortedList -> TableView
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to load medal data for " + event.getEventName() + ": " + e.getMessage());
            medalEntries.clear();
        }
    }

    private void populateFormWithSelectedMedal(MedalEntry entry) {
        if (entry != null) {
            nocTextField.setText(entry.getNoc());
            goldTextField.setText(String.valueOf(entry.getGold()));
            silverTextField.setText(String.valueOf(entry.getSilver()));
            bronzeTextField.setText(String.valueOf(entry.getBronze()));
        } else {
            clearFormFields();
        }
    }

    @FXML
    private void handleAddButtonAction() {
        OlympicEvent selectedEvent = olympicEventComboBox.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            AlertUtil.showError("Selection Error", "Please select an Olympic event first.");
            return;
        }
         if (selectedEvent.getTableNameInDb() == null || selectedEvent.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(selectedEvent.getTableNameInDb())) {
            AlertUtil.showError("Event Error", "The selected Olympic event does not have an active medal table. Cannot add entries.");
            return;
        }

        try {
            String noc = nocTextField.getText().trim().toUpperCase();
            if (noc.isEmpty() || noc.length() > 3) {
                AlertUtil.showError("Input Error", "NOC cannot be empty and must be 3 characters max.");
                return;
            }
            int gold = Integer.parseInt(goldTextField.getText());
            int silver = Integer.parseInt(silverTextField.getText());
            int bronze = Integer.parseInt(bronzeTextField.getText());

            if (gold < 0 || silver < 0 || bronze < 0) {
                AlertUtil.showError("Input Error", "Medal counts cannot be negative.");
                return;
            }

            MedalEntry newEntry = new MedalEntry();
            newEntry.setNoc(noc);
            newEntry.setGold(gold);
            newEntry.setSilver(silver);
            newEntry.setBronze(bronze);

            if (medalService.addMedal(newEntry, selectedEvent)) {
                AlertUtil.showInfo("Success", "Medal entry added successfully. ID: " + newEntry.getId());
                loadMedalDataForSelectedEvent(selectedEvent);
                clearFormFields();
            } else {
                AlertUtil.showError("Operation Failed", "Failed to add medal entry. The NOC might already exist for this event or an unknown error occurred.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Please enter valid numbers for medal counts.");
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to add medal entry: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateButtonAction() {
        if (selectedMedalEntry == null) {
            AlertUtil.showError("Selection Error", "No medal entry selected to update.");
            return;
        }
        OlympicEvent selectedEvent = olympicEventComboBox.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            AlertUtil.showError("Error", "No Olympic event context for update.");
            return;
        }
        if (selectedEvent.getTableNameInDb() == null || selectedEvent.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(selectedEvent.getTableNameInDb())) {
            AlertUtil.showError("Event Error", "The selected Olympic event does not have an active medal table. Cannot update entries.");
            return;
        }

        try {
            String noc = nocTextField.getText().trim().toUpperCase();
            if (noc.isEmpty() || noc.length() > 3) {
                AlertUtil.showError("Input Error", "NOC cannot be empty and must be 3 characters max.");
                return;
            }
            int gold = Integer.parseInt(goldTextField.getText());
            int silver = Integer.parseInt(silverTextField.getText());
            int bronze = Integer.parseInt(bronzeTextField.getText());

            if (gold < 0 || silver < 0 || bronze < 0) {
                AlertUtil.showError("Input Error", "Medal counts cannot be negative.");
                return;
            }

            // Update the existing selectedMedalEntry object
            selectedMedalEntry.setNoc(noc);
            selectedMedalEntry.setGold(gold);
            selectedMedalEntry.setSilver(silver);
            selectedMedalEntry.setBronze(bronze);
            // Total is auto-updated by setters in MedalEntry

            if (medalService.updateMedal(selectedMedalEntry, selectedEvent)) {
                AlertUtil.showInfo("Success", "Medal entry updated successfully.");
                // Refresh the specific item in the table view for immediate visual feedback
                // medalTableView.refresh(); // This can refresh the whole table
                // More targeted refresh if needed, but usually loadMedalDataForSelectedEvent is fine
                loadMedalDataForSelectedEvent(selectedEvent); // Reloads all data for the event
                clearFormFields();
                medalTableView.getSelectionModel().clearSelection(); // Deselect
            } else {
                AlertUtil.showError("Operation Failed", "Failed to update medal entry. The NOC might have been changed to one that already exists, or an unknown error occurred.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Please enter valid numbers for medal counts.");
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to update medal entry: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteButtonAction() {
        if (selectedMedalEntry == null) {
            AlertUtil.showError("Selection Error", "No medal entry selected to delete.");
            return;
        }
        OlympicEvent selectedEvent = olympicEventComboBox.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            AlertUtil.showError("Error", "No Olympic event context for delete.");
            return;
        }
        if (selectedEvent.getTableNameInDb() == null || selectedEvent.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(selectedEvent.getTableNameInDb())) {
            AlertUtil.showError("Event Error", "The selected Olympic event does not have an active medal table. Cannot delete entries.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Confirm Delete",
                "Are you sure you want to delete the medal entry for " + selectedMedalEntry.getNoc() + "?");

        if (confirmed) {
            try {
                if (medalService.deleteMedal(selectedMedalEntry, selectedEvent)) {
                    AlertUtil.showInfo("Success", "Medal entry deleted successfully.");
                    loadMedalDataForSelectedEvent(selectedEvent);
                    clearFormFields();
                    medalTableView.getSelectionModel().clearSelection();
                } else {
                    AlertUtil.showError("Operation Failed", "Failed to delete medal entry.");
                }
            } catch (SQLException e) {
                AlertUtil.showError("Database Error", "Failed to delete medal entry: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearFormAction() {
        clearFormFields();
        medalTableView.getSelectionModel().clearSelection();
    }

    private void clearFormFields() {
        nocTextField.clear();
        goldTextField.clear();
        silverTextField.clear();
        bronzeTextField.clear();
        // Optionally, reset focus
        // nocTextField.requestFocus();
    }
}