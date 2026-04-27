package com.quartermaster.controller;

import com.quartermaster.Main;
import com.quartermaster.auth.SessionManager;
import com.quartermaster.auth.UserRole;
import com.quartermaster.dao.EquipmentDao;
import com.quartermaster.dao.IssuanceDao;
import com.quartermaster.dao.OfficerDao;
import com.quartermaster.dao.VehicleDao;
import com.quartermaster.dao.VehicleMaintenanceLogDao;
import com.quartermaster.model.EquipmentCategory;
import com.quartermaster.model.EquipmentCondition;
import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.EquipmentStatus;
import com.quartermaster.model.IssuanceAdminRow;
import com.quartermaster.model.Officer;
import com.quartermaster.model.Vehicle;
import com.quartermaster.model.VehicleMaintenanceLog;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private TabPane adminTabPane;

    @FXML
    private TableView<Officer> officersTable;

    @FXML
    private TableColumn<Officer, Integer> officerIdColumn;

    @FXML
    private TableColumn<Officer, String> officerNameColumn;

    @FXML
    private TableColumn<Officer, String> officerRankColumn;

    @FXML
    private TableColumn<Officer, String> officerBadgeColumn;

    @FXML
    private TableView<EquipmentItem> equipmentTable;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentNameColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentCategoryColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentSerialColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentConditionColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentStatusColumn;

    @FXML
    private TableView<IssuanceAdminRow> issuanceTable;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceOfficerColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceItemColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceDateColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> returnDateColumn;

    @FXML
    private ComboBox<Officer> issueOfficerCombo;

    @FXML
    private ComboBox<EquipmentItem> issueItemCombo;

    @FXML
    private DatePicker issueDatePicker;

    @FXML
    private TableView<Vehicle> vehiclesTable;

    @FXML
    private TableColumn<Vehicle, String> vehicleUnitColumn;

    @FXML
    private TableColumn<Vehicle, String> vehicleMakeColumn;

    @FXML
    private TableColumn<Vehicle, String> vehicleModelColumn;

    @FXML
    private TableColumn<Vehicle, Integer> vehicleYearColumn;

    @FXML
    private TableColumn<Vehicle, String> vehicleVinColumn;

    @FXML
    private TableColumn<Vehicle, String> vehiclePlateColumn;

    @FXML
    private ComboBox<Vehicle> maintenanceVehicleCombo;

    @FXML
    private TableView<VehicleMaintenanceLog> maintenanceTable;

    @FXML
    private TableColumn<VehicleMaintenanceLog, String> maintenanceDateColumn;

    @FXML
    private TableColumn<VehicleMaintenanceLog, Integer> maintenanceMileageColumn;

    @FXML
    private TableColumn<VehicleMaintenanceLog, String> maintenanceDescriptionColumn;

    @FXML
    private TableColumn<VehicleMaintenanceLog, String> maintenanceByColumn;

    private final OfficerDao officerDao = new OfficerDao();
    private final EquipmentDao equipmentDao = new EquipmentDao();
    private final IssuanceDao issuanceDao = new IssuanceDao();
    private final VehicleDao vehicleDao = new VehicleDao();
    private final VehicleMaintenanceLogDao maintenanceLogDao = new VehicleMaintenanceLogDao();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null || SessionManager.getCurrentUser().getRole() != UserRole.ADMIN) {
            forceLogoutToLogin();
            return;
        }

        welcomeLabel.setText("Admin: " + SessionManager.getCurrentUser().getUsername());

        setupOfficerTable();
        setupEquipmentTable();
        setupIssuanceTable();
        setupVehicleTable();
        setupMaintenanceTable();

        issueDatePicker.setValue(LocalDate.now());
        refreshAll();
    }

    @FXML
    public void onLogout() {
        forceLogoutToLogin();
    }

    @FXML
    public void onRefreshAll() {
        refreshAll();
    }

    @FXML
    public void onAddOfficer() {
        OfficerFormData formData = showOfficerDialog(null);
        if (formData == null) {
            return;
        }

        officerDao.insert(formData.name(), formData.rank(), formData.badgeNumber());
        refreshOfficers();
    }

    @FXML
    public void onEditOfficer() {
        Officer selected = officersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Officers", "Select an officer to edit.");
            return;
        }

        OfficerFormData formData = showOfficerDialog(selected);
        if (formData == null) {
            return;
        }

        officerDao.update(selected.getOfficerId(), formData.name(), formData.rank(), formData.badgeNumber());
        refreshOfficers();
    }

    @FXML
    public void onDeleteOfficer() {
        Officer selected = officersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Officers", "Select an officer to delete.");
            return;
        }

        officerDao.delete(selected.getOfficerId());
        refreshOfficers();
    }

    @FXML
    public void onAddEquipment() {
        EquipmentFormData formData = showEquipmentDialog(null);
        if (formData == null) {
            return;
        }

        equipmentDao.insert(formData.name(), formData.category(), formData.serialNumber(), formData.condition(), formData.status());
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onEditEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Equipment", "Select an equipment item to edit.");
            return;
        }

        EquipmentFormData formData = showEquipmentDialog(selected);
        if (formData == null) {
            return;
        }

        equipmentDao.update(selected.getItemId(), formData.name(), formData.category(), formData.serialNumber(), formData.condition(), formData.status());
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onDeleteEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Equipment", "Select an equipment item to delete.");
            return;
        }

        equipmentDao.delete(selected.getItemId());
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onAttachItemToWeapon() {
        EquipmentItem selectedWeapon = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedWeapon == null || selectedWeapon.getCategory() != EquipmentCategory.WEAPON) {
            UiAlerts.error("Weapon Attachments", "Select a weapon first.");
            return;
        }

        List<EquipmentItem> attachmentCandidates = equipmentDao.findAll().stream()
                .filter(i -> i.getCategory() == EquipmentCategory.ATTACHMENT)
                .collect(Collectors.toList());

        if (attachmentCandidates.isEmpty()) {
            UiAlerts.error("Weapon Attachments", "No attachment items found.");
            return;
        }

        ChoiceDialog<EquipmentItem> dialog = new ChoiceDialog<>(attachmentCandidates.get(0), attachmentCandidates);
        dialog.setTitle("Attach Item");
        dialog.setHeaderText("Attach item to weapon: " + selectedWeapon.getName());
        dialog.setContentText("Attachment:");

        Optional<EquipmentItem> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        equipmentDao.attachToWeapon(selectedWeapon.getItemId(), result.get().getItemId());
        UiAlerts.info("Weapon Attachments", "Attachment linked to weapon.");
    }

    @FXML
    public void onRemoveAttachmentFromWeapon() {
        EquipmentItem selectedWeapon = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedWeapon == null || selectedWeapon.getCategory() != EquipmentCategory.WEAPON) {
            UiAlerts.error("Weapon Attachments", "Select a weapon first.");
            return;
        }

        List<EquipmentItem> linkedAttachments = equipmentDao.getAttachmentsForWeapon(selectedWeapon.getItemId());
        if (linkedAttachments.isEmpty()) {
            UiAlerts.error("Weapon Attachments", "This weapon has no linked attachments.");
            return;
        }

        ChoiceDialog<EquipmentItem> dialog = new ChoiceDialog<>(linkedAttachments.get(0), linkedAttachments);
        dialog.setTitle("Remove Attachment");
        dialog.setHeaderText("Remove attachment from weapon: " + selectedWeapon.getName());
        dialog.setContentText("Attachment:");

        Optional<EquipmentItem> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        equipmentDao.detachFromWeapon(selectedWeapon.getItemId(), result.get().getItemId());
        UiAlerts.info("Weapon Attachments", "Attachment removed from weapon.");
    }

    @FXML
    public void onIssueSelectedItem() {
        Officer officer = issueOfficerCombo.getSelectionModel().getSelectedItem();
        EquipmentItem item = issueItemCombo.getSelectionModel().getSelectedItem();
        LocalDate issueDate = issueDatePicker.getValue();

        if (officer == null || item == null || issueDate == null) {
            UiAlerts.error("Issuances", "Select officer, item, and date.");
            return;
        }

        issuanceDao.issueItem(officer.getOfficerId(), item.getItemId(), issueDate);
        refreshIssuances();
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onReturnSelectedItem() {
        IssuanceAdminRow selected = issuanceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Issuances", "Select an issuance to return.");
            return;
        }
        if (selected.getReturnedDate() != null) {
            UiAlerts.error("Issuances", "Selected issuance is already returned.");
            return;
        }

        issuanceDao.returnItemAndAttachments(selected.getIssuanceId());
        refreshIssuances();
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onAddVehicle() {
        VehicleFormData formData = showVehicleDialog(null);
        if (formData == null) {
            return;
        }

        vehicleDao.insert(formData.unitNumber(), formData.make(), formData.model(), formData.year(), formData.vin(), formData.plate());
        refreshVehiclesAndMaintenanceOptions();
    }

    @FXML
    public void onEditVehicle() {
        Vehicle selected = vehiclesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Vehicles", "Select a vehicle to edit.");
            return;
        }

        VehicleFormData formData = showVehicleDialog(selected);
        if (formData == null) {
            return;
        }

        vehicleDao.update(selected.getVehicleId(), formData.unitNumber(), formData.make(), formData.model(), formData.year(), formData.vin(), formData.plate());
        refreshVehiclesAndMaintenanceOptions();
    }

    @FXML
    public void onDeleteVehicle() {
        Vehicle selected = vehiclesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Vehicles", "Select a vehicle to delete.");
            return;
        }

        vehicleDao.delete(selected.getVehicleId());
        refreshVehiclesAndMaintenanceOptions();
    }

    @FXML
    public void onMaintenanceVehicleChanged() {
        Vehicle selectedVehicle = maintenanceVehicleCombo.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            maintenanceTable.setItems(FXCollections.observableArrayList());
            return;
        }
        List<VehicleMaintenanceLog> logs = maintenanceLogDao.findByVehicle(selectedVehicle.getVehicleId());
        maintenanceTable.setItems(FXCollections.observableArrayList(logs));
    }

    @FXML
    public void onAddMaintenanceLog() {
        Vehicle selectedVehicle = maintenanceVehicleCombo.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            UiAlerts.error("Maintenance Logs", "Select a vehicle first.");
            return;
        }

        Dialog<MaintenanceFormData> dialog = new Dialog<>();
        dialog.setTitle("Add Maintenance Log");
        dialog.setHeaderText("Vehicle: " + selectedVehicle.getUnitNumber());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField mileageField = new TextField();
        TextField performedByField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(4);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Date"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Mileage"), 0, 1);
        grid.add(mileageField, 1, 1);
        grid.add(new Label("Performed By"), 0, 2);
        grid.add(performedByField, 1, 2);
        grid.add(new Label("Description"), 0, 3);
        grid.add(descriptionArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                String mileageText = mileageField.getText() == null ? "" : mileageField.getText().trim();
                Integer mileage = mileageText.isEmpty() ? null : Integer.parseInt(mileageText);
                return new MaintenanceFormData(datePicker.getValue(), mileage,
                        descriptionArea.getText() == null ? "" : descriptionArea.getText().trim(),
                        performedByField.getText() == null ? "" : performedByField.getText().trim());
            }
            return null;
        });

        Optional<MaintenanceFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        MaintenanceFormData data = result.get();
        if (data.logDate() == null || data.description().isEmpty()) {
            UiAlerts.error("Maintenance Logs", "Date and description are required.");
            return;
        }

        maintenanceLogDao.insert(selectedVehicle.getVehicleId(), data.logDate(), data.mileage(), data.description(), data.performedBy());
        onMaintenanceVehicleChanged();
    }

    private void setupOfficerTable() {
        officerIdColumn.setCellValueFactory(new PropertyValueFactory<>("officerId"));
        officerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        officerRankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        officerBadgeColumn.setCellValueFactory(new PropertyValueFactory<>("badgeNumber"));
    }

    private void setupEquipmentTable() {
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        equipmentCategoryColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getCategory().name()
        ));
        equipmentSerialColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        equipmentConditionColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getCondition().name()
        ));
        equipmentStatusColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getStatus().name()
        ));
        issueItemCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(EquipmentItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getCategory().name() + ")");
            }
        });
        issueItemCombo.setButtonCell(issueItemCombo.getCellFactory().call(null));
    }

    private void setupIssuanceTable() {
        issuanceOfficerColumn.setCellValueFactory(new PropertyValueFactory<>("officerName"));
        issuanceItemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        issuanceDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getIssuedDate().toString()
        ));
        returnDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getReturnedDate() == null ? "" : cellData.getValue().getReturnedDate().toString()
        ));

        issueOfficerCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Officer officer, boolean empty) {
                super.updateItem(officer, empty);
                setText(empty || officer == null ? null : officer.getName() + " (" + officer.getBadgeNumber() + ")");
            }
        });
        issueOfficerCombo.setButtonCell(issueOfficerCombo.getCellFactory().call(null));
    }

    private void setupVehicleTable() {
        vehicleUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unitNumber"));
        vehicleMakeColumn.setCellValueFactory(new PropertyValueFactory<>("make"));
        vehicleModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        vehicleYearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        vehicleVinColumn.setCellValueFactory(new PropertyValueFactory<>("vin"));
        vehiclePlateColumn.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));

        maintenanceVehicleCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Vehicle vehicle, boolean empty) {
                super.updateItem(vehicle, empty);
                setText(empty || vehicle == null ? null : vehicle.getUnitNumber() + " - " + vehicle.getMake() + " " + vehicle.getModel());
            }
        });
        maintenanceVehicleCombo.setButtonCell(maintenanceVehicleCombo.getCellFactory().call(null));
    }

    private void setupMaintenanceTable() {
        maintenanceDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getLogDate().toString()
        ));
        maintenanceMileageColumn.setCellValueFactory(new PropertyValueFactory<>("mileage"));
        maintenanceDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        maintenanceByColumn.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
    }

    private void refreshAll() {
        refreshOfficers();
        refreshEquipmentAndIssueOptions();
        refreshIssuances();
        refreshVehiclesAndMaintenanceOptions();
    }

    private void refreshOfficers() {
        List<Officer> officers = officerDao.findAll();
        ObservableList<Officer> list = FXCollections.observableArrayList(officers);
        officersTable.setItems(list);
        issueOfficerCombo.setItems(FXCollections.observableArrayList(officers));
    }

    private void refreshEquipmentAndIssueOptions() {
        List<EquipmentItem> equipment = equipmentDao.findAll();
        equipmentTable.setItems(FXCollections.observableArrayList(equipment));
        List<EquipmentItem> availableItems = equipmentDao.findAvailable();
        issueItemCombo.setItems(FXCollections.observableArrayList(availableItems));
    }

    private void refreshIssuances() {
        issuanceTable.setItems(FXCollections.observableArrayList(issuanceDao.findAll()));
    }

    private void refreshVehiclesAndMaintenanceOptions() {
        List<Vehicle> vehicles = vehicleDao.findAll();
        ObservableList<Vehicle> list = FXCollections.observableArrayList(vehicles);
        vehiclesTable.setItems(list);

        Vehicle previousSelection = maintenanceVehicleCombo.getSelectionModel().getSelectedItem();
        maintenanceVehicleCombo.setItems(list);
        if (previousSelection != null) {
            maintenanceVehicleCombo.getSelectionModel().select(
                    list.stream().filter(v -> v.getVehicleId() == previousSelection.getVehicleId()).findFirst().orElse(null)
            );
        }
        onMaintenanceVehicleChanged();
    }

    private OfficerFormData showOfficerDialog(Officer existing) {
        Dialog<OfficerFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Officer" : "Edit Officer");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(existing == null ? "" : existing.getName());
        TextField rankField = new TextField(existing == null ? "" : existing.getRank());
        TextField badgeField = new TextField(existing == null ? "" : existing.getBadgeNumber());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Rank"), 0, 1);
        grid.add(rankField, 1, 1);
        grid.add(new Label("Badge Number"), 0, 2);
        grid.add(badgeField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new OfficerFormData(
                        nameField.getText() == null ? "" : nameField.getText().trim(),
                        rankField.getText() == null ? "" : rankField.getText().trim(),
                        badgeField.getText() == null ? "" : badgeField.getText().trim()
                );
            }
            return null;
        });

        Optional<OfficerFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        OfficerFormData formData = result.get();
        if (formData.name().isEmpty() || formData.rank().isEmpty() || formData.badgeNumber().isEmpty()) {
            UiAlerts.error("Officers", "Name, rank, and badge number are required.");
            return null;
        }
        return formData;
    }

    private EquipmentFormData showEquipmentDialog(EquipmentItem existing) {
        Dialog<EquipmentFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Equipment" : "Edit Equipment");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(existing == null ? "" : existing.getName());
        TextField serialField = new TextField(existing == null ? "" : existing.getSerialNumber());
        ComboBox<EquipmentCategory> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentCategory.values()));
        ComboBox<EquipmentCondition> conditionCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentCondition.values()));
        ComboBox<EquipmentStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentStatus.values()));

        categoryCombo.getSelectionModel().select(existing == null ? EquipmentCategory.UNIFORM : existing.getCategory());
        conditionCombo.getSelectionModel().select(existing == null ? EquipmentCondition.GOOD : existing.getCondition());
        statusCombo.getSelectionModel().select(existing == null ? EquipmentStatus.AVAILABLE : existing.getStatus());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Serial Number"), 0, 2);
        grid.add(serialField, 1, 2);
        grid.add(new Label("Condition"), 0, 3);
        grid.add(conditionCombo, 1, 3);
        grid.add(new Label("Status"), 0, 4);
        grid.add(statusCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new EquipmentFormData(
                        nameField.getText() == null ? "" : nameField.getText().trim(),
                        categoryCombo.getSelectionModel().getSelectedItem(),
                        serialField.getText() == null ? "" : serialField.getText().trim(),
                        conditionCombo.getSelectionModel().getSelectedItem(),
                        statusCombo.getSelectionModel().getSelectedItem()
                );
            }
            return null;
        });

        Optional<EquipmentFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        EquipmentFormData formData = result.get();
        if (formData.name().isEmpty() || formData.category() == null || formData.condition() == null || formData.status() == null) {
            UiAlerts.error("Equipment", "Name, category, condition, and status are required.");
            return null;
        }

        return formData;
    }

    private VehicleFormData showVehicleDialog(Vehicle existing) {
        Dialog<VehicleFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Vehicle" : "Edit Vehicle");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField unitField = new TextField(existing == null ? "" : existing.getUnitNumber());
        TextField makeField = new TextField(existing == null ? "" : existing.getMake());
        TextField modelField = new TextField(existing == null ? "" : existing.getModel());
        TextField yearField = new TextField(existing == null ? "" : String.valueOf(existing.getYear()));
        TextField vinField = new TextField(existing == null ? "" : existing.getVin());
        TextField plateField = new TextField(existing == null ? "" : existing.getPlateNumber());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Unit Number"), 0, 0);
        grid.add(unitField, 1, 0);
        grid.add(new Label("Make"), 0, 1);
        grid.add(makeField, 1, 1);
        grid.add(new Label("Model"), 0, 2);
        grid.add(modelField, 1, 2);
        grid.add(new Label("Year"), 0, 3);
        grid.add(yearField, 1, 3);
        grid.add(new Label("VIN"), 0, 4);
        grid.add(vinField, 1, 4);
        grid.add(new Label("Plate"), 0, 5);
        grid.add(plateField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                int year = Integer.parseInt(yearField.getText().trim());
                return new VehicleFormData(
                        unitField.getText() == null ? "" : unitField.getText().trim(),
                        makeField.getText() == null ? "" : makeField.getText().trim(),
                        modelField.getText() == null ? "" : modelField.getText().trim(),
                        year,
                        vinField.getText() == null ? "" : vinField.getText().trim(),
                        plateField.getText() == null ? "" : plateField.getText().trim()
                );
            }
            return null;
        });

        Optional<VehicleFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        VehicleFormData formData = result.get();
        if (formData.unitNumber().isEmpty() || formData.make().isEmpty() || formData.model().isEmpty()) {
            UiAlerts.error("Vehicles", "Unit number, make, and model are required.");
            return null;
        }
        return formData;
    }

    private void forceLogoutToLogin() {
        SessionManager.logout();
        try {
            Main.switchScene("/com/quartermaster/fxml/login.fxml", "Quartermaster Login", 420, 280);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to return to login", ex);
        }
    }

    private record OfficerFormData(String name, String rank, String badgeNumber) {
    }

    private record EquipmentFormData(String name, EquipmentCategory category, String serialNumber,
                                     EquipmentCondition condition, EquipmentStatus status) {
    }

    private record VehicleFormData(String unitNumber, String make, String model, int year,
                                   String vin, String plate) {
    }

    private record MaintenanceFormData(LocalDate logDate, Integer mileage, String description, String performedBy) {
    }
}
