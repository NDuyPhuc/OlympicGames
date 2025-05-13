package com.duyphuc.olympics.controller;
//src/main/java/com/yourgroup/olympics/controller/MedalManagementController.java

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
     loadOlympicEvents();
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

 private void loadOlympicEvents() {
     olympicEventComboBox.setItems(FXCollections.observableArrayList(medalService.getEvents()));
 }

 private void setupEventListeners() {
     olympicEventComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldEvent, newEvent) -> {
         if (newEvent != null) {
             loadMedalDataForSelectedEvent(newEvent);
             formPane.setDisable(false); // Enable form when an event is selected
         } else {
             medalEntries.clear();
             formPane.setDisable(true);
         }
     });

     medalTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
         selectedMedalEntry = newSelection;
         populateFormWithSelectedMedal(newSelection);
         updateButton.setDisable(newSelection == null);
         deleteButton.setDisable(newSelection == null);
     });
 }
 
 // (Extra) Setup search functionality
 private void setupSearchFilter() {
     filteredData = new FilteredList<>(medalEntries, p -> true); // Initially show all data

     searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
         filteredData.setPredicate(medalEntry -> {
             if (newValue == null || newValue.isEmpty()) {
                 return true; // Show all if search field is empty
             }
             String lowerCaseFilter = newValue.toLowerCase();
             return medalEntry.getNoc().toLowerCase().contains(lowerCaseFilter);
         });
     });

     SortedList<MedalEntry> sortedData = new SortedList<>(filteredData);
     sortedData.comparatorProperty().bind(medalTableView.comparatorProperty()); // Bind comparator
     medalTableView.setItems(sortedData); // Set sorted and filtered data to table
 }


 private void loadMedalDataForSelectedEvent(OlympicEvent event) {
     medalEntries.setAll(medalService.getMedalDataForEvent(event));
     // If not using FilteredList directly for search, you would set medalTableView.setItems(medalEntries);
     // Since we use FilteredList, this change to medalEntries will be reflected.
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

         MedalEntry newEntry = new MedalEntry(noc, gold, silver, bronze); // ID will be set by DAO after insert

         if (medalService.addMedal(newEntry, selectedEvent)) {
             AlertUtil.showInfo("Success", "Medal entry added successfully.");
             loadMedalDataForSelectedEvent(selectedEvent); // Refresh table
             clearFormFields();
         } else {
             AlertUtil.showError("Database Error", "Failed to add medal entry.");
         }
     } catch (NumberFormatException e) {
         AlertUtil.showError("Input Error", "Please enter valid numbers for medal counts.");
     }
 }

 @FXML
 private void handleUpdateButtonAction() {
     if (selectedMedalEntry == null) {
         AlertUtil.showError("Selection Error", "No medal entry selected to update.");
         return;
     }
     OlympicEvent selectedEvent = olympicEventComboBox.getSelectionModel().getSelectedItem();
      if (selectedEvent == null) { // Should not happen if an entry is selected but good check
         AlertUtil.showError("Error", "No Olympic event context for update.");
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
         
         selectedMedalEntry.setNoc(noc);
         selectedMedalEntry.setGold(gold);
         selectedMedalEntry.setSilver(silver);
         selectedMedalEntry.setBronze(bronze);
         // Total is automatically updated by setters in MedalEntry

         if (medalService.updateMedal(selectedMedalEntry, selectedEvent)) {
             AlertUtil.showInfo("Success", "Medal entry updated successfully.");
             loadMedalDataForSelectedEvent(selectedEvent); // Refresh table
             clearFormFields();
         } else {
             AlertUtil.showError("Database Error", "Failed to update medal entry.");
         }
     } catch (NumberFormatException e) {
         AlertUtil.showError("Input Error", "Please enter valid numbers for medal counts.");
     }
 }

 @FXML
 private void handleDeleteButtonAction() {
     if (selectedMedalEntry == null) {
         AlertUtil.showError("Selection Error", "No medal entry selected to delete.");
         return;
     }
     OlympicEvent selectedEvent = olympicEventComboBox.getSelectionModel().getSelectedItem();

     boolean confirmed = AlertUtil.showConfirmation("Confirm Delete",
             "Are you sure you want to delete the medal entry for " + selectedMedalEntry.getNoc() + "?");

     if (confirmed) {
         if (medalService.deleteMedal(selectedMedalEntry, selectedEvent)) {
             AlertUtil.showInfo("Success", "Medal entry deleted successfully.");
             loadMedalDataForSelectedEvent(selectedEvent); // Refresh table
             clearFormFields();
         } else {
             AlertUtil.showError("Database Error", "Failed to delete medal entry.");
         }
     }
 }

 @FXML
 private void handleClearFormAction() {
     clearFormFields();
     medalTableView.getSelectionModel().clearSelection(); // Deselect row
     selectedMedalEntry = null;
 }

 private void clearFormFields() {
     nocTextField.clear();
     goldTextField.clear();
     silverTextField.clear();
     bronzeTextField.clear();
 }
}