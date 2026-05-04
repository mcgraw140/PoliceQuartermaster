package com.quartermaster.controller;

import com.quartermaster.Main;
import com.quartermaster.auth.SessionManager;
import com.quartermaster.auth.UserRole;
import com.quartermaster.dao.EquipmentDao;
import com.quartermaster.dao.IssuanceDao;
import com.quartermaster.dao.LookupDao;
import com.quartermaster.dao.OfficerDao;
import com.quartermaster.dao.SettingsDao;
import com.quartermaster.dao.UserDao;
import com.quartermaster.dao.VehicleDao;
import com.quartermaster.dao.VehicleMaintenanceLogDao;
import com.quartermaster.model.EquipmentBranch;
import com.quartermaster.model.EquipmentCondition;
import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.EquipmentStatus;
import com.quartermaster.model.IssuanceAdminRow;
import com.quartermaster.model.IssuedItemRow;
import com.quartermaster.model.LookupCategory;
import com.quartermaster.model.LookupItem;
import com.quartermaster.model.Officer;
import com.quartermaster.model.User;
import com.quartermaster.model.Vehicle;
import com.quartermaster.model.VehicleMaintenanceLog;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Window;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminController {

    private enum MainTab {
        EQUIPMENT,
        ISSUE_RETURN,
        ADMIN,
        VEHICLE
    }

    private enum AdminSubTab {
        PERSONNEL,
        LOOKUPS,
        SETTINGS,
        HISTORICAL
    }

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button topTabEquipment;

    @FXML
    private Button topTabIssueReturn;

    @FXML
    private Button topTabAdmin;

    @FXML
    private Button topTabVehicle;

    @FXML
    private HBox moduleNavBar;

    @FXML
    private Label moduleNavLabel;

    @FXML
    private VBox moduleInventoryPane;

    @FXML
    private VBox moduleIssuePane;

    @FXML
    private VBox moduleFleetPane;

    @FXML
    private VBox moduleAdminPane;

    @FXML
    private Label inventoryModuleTitleLabel;

    @FXML
    private TextField inventorySearchField;

    @FXML
    private ComboBox<String> inventoryStatusFilterCombo;

    @FXML
    private Label inventorySummaryLabel;

    @FXML
    private Button attachItemButton;

    @FXML
    private Button removeAttachmentButton;

    @FXML
    private TableView<EquipmentItem> equipmentTable;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentNameColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentCategoryColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentLocationColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentSerialColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentCostColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentConditionColumn;

    @FXML
    private TableColumn<EquipmentItem, String> equipmentStatusColumn;

    @FXML
    private ComboBox<Officer> issueOfficerCombo;

    @FXML
    private TextField issueSearchField;

    @FXML
    private TableView<EquipmentItem> issueAvailableTable;

    @FXML
    private TableColumn<EquipmentItem, String> issueAvailableNameColumn;

    @FXML
    private TableColumn<EquipmentItem, String> issueAvailableCategoryColumn;

    @FXML
    private TableColumn<EquipmentItem, String> issueAvailableSerialColumn;

    @FXML
    private TableView<IssuedItemRow> issueSelectedTable;

    @FXML
    private TableColumn<IssuedItemRow, String> issueSelectedNameColumn;

    @FXML
    private TableColumn<IssuedItemRow, String> issueSelectedCategoryColumn;

    @FXML
    private TableColumn<IssuedItemRow, String> issueSelectedSerialColumn;

    @FXML
    private DatePicker issueDatePicker;

    @FXML
    private TableView<IssuanceAdminRow> issuanceTable;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceOfficerColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceItemColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceBranchColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceCategoryColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceSerialColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceStatusColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceConditionColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceCostColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceDateColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> issuanceByColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> returnDateColumn;

    @FXML
    private TableColumn<IssuanceAdminRow, String> returnByColumn;

    @FXML
    private TableView<Vehicle> vehiclesTable;

    @FXML
    private TableColumn<Vehicle, String> vehicleUnitColumn;

    @FXML
    private TableColumn<Vehicle, String> vehicleTypeColumn;

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

    @FXML
    private TableColumn<VehicleMaintenanceLog, String> maintenanceCostColumn;

    @FXML
    private Label adminSubModuleTitle;

    @FXML
    private VBox adminPersonnelPane;

    @FXML
    private VBox adminLookupsPane;

    @FXML
    private VBox adminSettingsPane;

    @FXML
    private VBox adminHistoryPane;

    @FXML
    private TextField agencyNameField;

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
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> userIdColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> linkedOfficerColumn;

    @FXML
    private ListView<LookupCategory> lookupCategoryList;

    @FXML
    private Label lookupEditorTitle;

    @FXML
    private TextField lookupNewItemField;

    @FXML
    private ListView<LookupItem> lookupItemList;

    private final EquipmentDao equipmentDao = new EquipmentDao();
    private final IssuanceDao issuanceDao = new IssuanceDao();
    private final OfficerDao officerDao = new OfficerDao();
    private final UserDao userDao = new UserDao();
    private final VehicleDao vehicleDao = new VehicleDao();
    private final VehicleMaintenanceLogDao maintenanceLogDao = new VehicleMaintenanceLogDao();
    private final LookupDao lookupDao = new LookupDao();
    private final SettingsDao settingsDao = new SettingsDao();

    private MainTab activeMainTab = MainTab.EQUIPMENT;
    private EquipmentBranch activeEquipmentBranch = EquipmentBranch.WEAPON;
    private final ObservableList<EquipmentItem> issueAvailableSource = FXCollections.observableArrayList();
    private final ObservableList<IssuedItemRow> issuedToOfficerItems = FXCollections.observableArrayList();
    private final FilteredList<EquipmentItem> issueAvailableFiltered = new FilteredList<>(issueAvailableSource, item -> true);
    private final Map<Integer, EquipmentItem> pendingIssueItemsById = new HashMap<>();
    private final Map<Integer, IssuedItemRow> pendingIssuedRowsByItemId = new HashMap<>();
    private final Map<Integer, IssuedItemRow> pendingReturnRowsByIssuanceId = new HashMap<>();
    private final Set<Integer> pendingReturnIssuanceIds = new HashSet<>();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null || SessionManager.getCurrentUser().getRole() != UserRole.ADMIN) {
            forceLogoutToLogin();
            return;
        }

        welcomeLabel.setText("Admin: " + SessionManager.getCurrentUser().getUsername());
        setupTables();
        setupFilters();
        setupLookupAdmin();
        loadAgencySettings();
        issueDatePicker.setValue(LocalDate.now());

        refreshAll();
        onTopTabEquipment();
    }

    @FXML
    public void onTopTabEquipment() {
        activeMainTab = MainTab.EQUIPMENT;
        setPaneVisible(moduleInventoryPane, true);
        setPaneVisible(moduleIssuePane, false);
        setPaneVisible(moduleFleetPane, false);
        setPaneVisible(moduleAdminPane, false);
        applyTopTabStyles();
        showEquipmentSubNav();
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onTopTabIssueReturn() {
        activeMainTab = MainTab.ISSUE_RETURN;
        setPaneVisible(moduleInventoryPane, false);
        setPaneVisible(moduleIssuePane, true);
        setPaneVisible(moduleFleetPane, false);
        setPaneVisible(moduleAdminPane, false);
        applyTopTabStyles();
        setModuleNavMessage("Assign equipment by officer.");
        refreshIssueOptions();
    }

    @FXML
    public void onTopTabAdmin() {
        activeMainTab = MainTab.ADMIN;
        setPaneVisible(moduleInventoryPane, false);
        setPaneVisible(moduleIssuePane, false);
        setPaneVisible(moduleFleetPane, false);
        setPaneVisible(moduleAdminPane, true);
        applyTopTabStyles();
        showAdminSubNav(AdminSubTab.PERSONNEL);
    }

    @FXML
    public void onTopTabVehicle() {
        activeMainTab = MainTab.VEHICLE;
        setPaneVisible(moduleInventoryPane, false);
        setPaneVisible(moduleIssuePane, false);
        setPaneVisible(moduleFleetPane, true);
        setPaneVisible(moduleAdminPane, false);
        applyTopTabStyles();
        setModuleNavMessage("Manage fleet units and maintenance history.");
        refreshVehiclesAndMaintenanceOptions();
    }

    @FXML
    public void onRefreshAll() {
        refreshAll();
    }

    @FXML
    public void onLogout() {
        forceLogoutToLogin();
    }

    @FXML
    public void onClearInventoryFilters() {
        inventorySearchField.setText("");
        inventoryStatusFilterCombo.getSelectionModel().select("All");
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onSaveSettings() {
        if (agencyNameField == null) {
            return;
        }
        settingsDao.saveAgencyName(text(agencyNameField));
        loadAgencySettings();
        UiAlerts.info("Settings", "Agency name saved.");
    }

    @FXML
    public void onAddEquipment() {
        EquipmentFormData formData = showEquipmentDialog(null);
        if (formData == null) {
            return;
        }

        int created = 0;
        if (formData.branch() == EquipmentBranch.UNIFORM) {
            int quantity = formData.quantity() == null ? 1 : formData.quantity();
            String serialPrefix = formData.serialPrefix();
            for (int i = 1; i <= quantity; i++) {
                String generatedSerial = buildUniformSerial(serialPrefix, i);
                equipmentDao.insert(formData.toItem(0, generatedSerial));
                created++;
            }
        } else {
            List<String> serials = formData.serialNumbers();
            if (serials.isEmpty()) {
                equipmentDao.insert(formData.toItem(0, formData.serialNumber()));
                created = 1;
            } else {
                for (String serial : serials) {
                    equipmentDao.insert(formData.toItem(0, serial));
                    created++;
                }
            }
        }

        if (created > 1) {
            UiAlerts.info("Equipment", "Added " + created + " items.");
        }
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

        equipmentDao.update(formData.toItem(selected.getItemId(), formData.serialNumber()));
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onDeleteEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Equipment", "Select an equipment item to delete.");
            return;
        }

        if (equipmentDao.hasIssuanceHistory(selected.getItemId())) {
            UiAlerts.error("Equipment", "Cannot delete equipment with issuance history. Set status to RETIRED instead.");
            return;
        }

        try {
            equipmentDao.delete(selected.getItemId());
            refreshEquipmentAndIssueOptions();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            UiAlerts.error("Equipment", "Unable to delete equipment. It may be linked to history or other records.");
        }
    }

    @FXML
    public void onAttachItemToWeapon() {
        EquipmentItem selectedWeapon = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedWeapon == null || !selectedWeapon.isWeapon()) {
            UiAlerts.error("Weapon Attachments", "Select a weapon first.");
            return;
        }

        List<EquipmentItem> candidates = equipmentDao.findAvailableAttachments();
        if (candidates.isEmpty()) {
            UiAlerts.error("Weapon Attachments", "No available attachment items were found.");
            return;
        }

        ChoiceDialog<EquipmentItem> dialog = new ChoiceDialog<>(candidates.get(0), candidates);
        dialog.setTitle("Attach Item");
        dialog.setHeaderText("Attach to weapon: " + selectedWeapon.getName());
        dialog.setContentText("Attachment");
        UiAlerts.applyTheme(dialog);

        Optional<EquipmentItem> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        equipmentDao.attachToWeapon(selectedWeapon.getItemId(), result.get().getItemId());
        UiAlerts.info("Weapon Attachments", "Attachment linked to weapon.");
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onRemoveAttachmentFromWeapon() {
        EquipmentItem selectedWeapon = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedWeapon == null || !selectedWeapon.isWeapon()) {
            UiAlerts.error("Weapon Attachments", "Select a weapon first.");
            return;
        }

        List<EquipmentItem> linked = equipmentDao.getAttachmentsForWeapon(selectedWeapon.getItemId());
        if (linked.isEmpty()) {
            UiAlerts.error("Weapon Attachments", "This weapon has no linked attachments.");
            return;
        }

        ChoiceDialog<EquipmentItem> dialog = new ChoiceDialog<>(linked.get(0), linked);
        dialog.setTitle("Remove Attachment");
        dialog.setHeaderText("Remove from weapon: " + selectedWeapon.getName());
        dialog.setContentText("Attachment");
        UiAlerts.applyTheme(dialog);

        Optional<EquipmentItem> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        equipmentDao.detachFromWeapon(selectedWeapon.getItemId(), result.get().getItemId());
        UiAlerts.info("Weapon Attachments", "Attachment removed.");
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onIssueSelectedItem() {
        Officer officer = issueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            UiAlerts.error("Issue / Return", "Select an officer first.");
            return;
        }

        List<IssuedItemRow> returnedItems = new ArrayList<>(pendingReturnRowsByIssuanceId.values());

        LocalDate issueDate = issueDatePicker.getValue();
        if (!pendingIssueItemsById.isEmpty() && issueDate == null) {
            UiAlerts.error("Issue / Return", "Select a date to save issued items.");
            return;
        }

        Integer actorUserId = currentUserIdOrNull();

        for (Integer issuanceId : pendingReturnIssuanceIds) {
            issuanceDao.returnItemAndAttachments(issuanceId, actorUserId);
        }
        if (issueDate != null) {
            for (EquipmentItem item : pendingIssueItemsById.values()) {
                issuanceDao.issueItem(officer.getOfficerId(), item.getItemId(), actorUserId, issueDate);
            }
        }

        clearPendingChanges();

        refreshIssueOptions();
        refreshIssuedItemsForOfficer();
        refreshIssuances();
        refreshEquipmentAndIssueOptions();
        showIssuancePrintSheet(officer, returnedItems);
    }

    @FXML
    public void onAddIssueSelection() {
        if (issueOfficerCombo.getSelectionModel().getSelectedItem() == null) {
            UiAlerts.error("Issue / Return", "Select an officer first.");
            return;
        }

        List<EquipmentItem> selected = selectedOrFocusedAvailableRows();
        if (selected.isEmpty()) {
            UiAlerts.error("Issue / Return", "Select at least one available item.");
            return;
        }

        for (EquipmentItem item : selected) {
            if (item.getItemId() > 0) {
                stageIssue(item);
            } else {
                int issuanceId = -item.getItemId();
                cancelStagedReturn(issuanceId);
            }
        }
        issueAvailableTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void onRemoveIssueSelection() {
        List<IssuedItemRow> selected = selectedOrFocusedIssuedRows();
        if (selected.isEmpty()) {
            UiAlerts.error("Issue / Return", "Select at least one issued item to return.");
            return;
        }

        for (IssuedItemRow row : selected) {
            if (row.getIssuanceId() < 0) {
                cancelStagedIssue(row.getIssuanceId());
            } else {
                stageReturn(row);
            }
        }
        issueSelectedTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void onReturnSelectedItem() {
        IssuanceAdminRow selected = issuanceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Issue / Return", "Select an issuance row to return.");
            return;
        }

        if (selected.getReturnedDate() != null) {
            UiAlerts.error("Issue / Return", "That issuance is already returned.");
            return;
        }

        issuanceDao.returnItemAndAttachments(selected.getIssuanceId(), currentUserIdOrNull());
        refreshIssueOptions();
        refreshIssuances();
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onAddVehicle() {
        VehicleFormData formData = showVehicleDialog(null);
        if (formData == null) {
            return;
        }

        vehicleDao.insert(formData.unitNumber(), formData.vehicleTypeId(), formData.make(), formData.model(),
                formData.year(), formData.vin(), formData.plate());
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

        vehicleDao.update(selected.getVehicleId(), formData.unitNumber(), formData.vehicleTypeId(), formData.make(),
                formData.model(), formData.year(), formData.vin(), formData.plate());
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
        Vehicle selected = maintenanceVehicleCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            maintenanceTable.setItems(FXCollections.observableArrayList());
            return;
        }
        vehiclesTable.getSelectionModel().select(
                vehiclesTable.getItems().stream()
                        .filter(vehicle -> vehicle.getVehicleId() == selected.getVehicleId())
                        .findFirst()
                        .orElse(null)
        );
        maintenanceTable.setItems(FXCollections.observableArrayList(
                maintenanceLogDao.findByVehicle(selected.getVehicleId())
        ));
    }

    @FXML
    public void onAddMaintenanceLog() {
        Vehicle selected = maintenanceVehicleCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Maintenance", "Select a vehicle first.");
            return;
        }

        MaintenanceDialogResult result = showMaintenanceDialog(selected, null);
        if (result == null || result.deleteRequested()) {
            return;
        }
        MaintenanceFormData formData = result.formData();

        maintenanceLogDao.insert(selected.getVehicleId(), formData.logDate(), formData.mileage(),
                formData.cost(), formData.description(), formData.performedBy());
        onMaintenanceVehicleChanged();
    }

    @FXML
    public void onEditMaintenanceLog() {
        Vehicle selectedVehicle = maintenanceVehicleCombo.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            UiAlerts.error("Maintenance", "Select a vehicle first.");
            return;
        }

        VehicleMaintenanceLog selectedLog = maintenanceTable.getSelectionModel().getSelectedItem();
        if (selectedLog == null) {
            UiAlerts.error("Maintenance", "Select a maintenance log row to edit.");
            return;
        }

        MaintenanceDialogResult result = showMaintenanceDialog(selectedVehicle, selectedLog);
        if (result == null) {
            return;
        }

        if (result.deleteRequested()) {
            maintenanceLogDao.delete(selectedLog.getLogId());
            onMaintenanceVehicleChanged();
            UiAlerts.info("Maintenance", "Maintenance log deleted.");
            return;
        }

        MaintenanceFormData formData = result.formData();

        maintenanceLogDao.update(selectedLog.getLogId(), selectedVehicle.getVehicleId(), formData.logDate(),
                formData.mileage(), formData.cost(), formData.description(), formData.performedBy());
        onMaintenanceVehicleChanged();
        UiAlerts.info("Maintenance", "Maintenance log updated.");
    }

    @FXML
    public void onDeleteMaintenanceLog() {
        Vehicle selectedVehicle = maintenanceVehicleCombo.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            UiAlerts.error("Maintenance", "Select a vehicle first.");
            return;
        }

        VehicleMaintenanceLog selectedLog = maintenanceTable.getSelectionModel().getSelectedItem();
        if (selectedLog == null) {
            UiAlerts.error("Maintenance", "Select a maintenance log row to delete.");
            return;
        }

        maintenanceLogDao.delete(selectedLog.getLogId());
        onMaintenanceVehicleChanged();
        UiAlerts.info("Maintenance", "Maintenance log deleted.");
    }

    private void showVehicleMaintenanceHistoryDialog(Vehicle vehicle) {
        if (vehicle == null) {
            return;
        }

        List<VehicleMaintenanceLog> logs = new ArrayList<>(maintenanceLogDao.findByVehicle(vehicle.getVehicleId()));
        double totalCost = maintenanceLogDao.findTotalCostByVehicle(vehicle.getVehicleId());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Vehicle Maintenance History");
        UiAlerts.applyTheme(dialog);

        ButtonType printButton = new ButtonType("Print", ButtonBar.ButtonData.OTHER);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(printButton, closeButton);

        Label title = new Label("Maintenance History");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");
        Label vehicleLine = new Label("Vehicle: " + vehicle.getUnitNumber() + " - " + vehicle.getMake() + " " + vehicle.getModel());
        Label metaLine = new Label("Type: " + (vehicle.getVehicleTypeName() == null ? "" : vehicle.getVehicleTypeName())
                + "   VIN: " + (vehicle.getVin() == null ? "" : vehicle.getVin())
                + "   Plate: " + (vehicle.getPlateNumber() == null ? "" : vehicle.getPlateNumber()));
        Label totalLine = new Label("Total Maintenance Cost: " + String.format("$%.2f", totalCost));
        totalLine.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");

        TableView<VehicleMaintenanceLog> historyTable = new TableView<>(FXCollections.observableArrayList(logs));
        historyTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        historyTable.setPrefHeight(Math.min(520, 170 + logs.size() * 26.0));

        TableColumn<VehicleMaintenanceLog, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getLogDate() == null ? "" : cellData.getValue().getLogDate().toString()
        ));
        TableColumn<VehicleMaintenanceLog, Integer> mileageCol = new TableColumn<>("Mileage");
        mileageCol.setCellValueFactory(new PropertyValueFactory<>("mileage"));
        TableColumn<VehicleMaintenanceLog, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<VehicleMaintenanceLog, String> byCol = new TableColumn<>("Performed By");
        byCol.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
        TableColumn<VehicleMaintenanceLog, String> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            Double value = cellData.getValue().getCost();
            return value == null ? "" : String.format("$%.2f", value);
        }));
        historyTable.getColumns().clear();
        historyTable.getColumns().addAll(List.of(dateCol, mileageCol, descriptionCol, byCol, costCol));

        historyTable.setRowFactory(tableView -> {
            TableRow<VehicleMaintenanceLog> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    VehicleMaintenanceLog selected = row.getItem();
                    if (showMaintenanceLogDetailEditDeleteOption(vehicle, selected)) {
                        List<VehicleMaintenanceLog> refreshed = maintenanceLogDao.findByVehicle(vehicle.getVehicleId());
                        historyTable.setItems(FXCollections.observableArrayList(refreshed));
                        maintenanceTable.setItems(FXCollections.observableArrayList(refreshed));
                        totalLine.setText("Total Maintenance Cost: " + String.format("$%.2f", maintenanceLogDao.findTotalCostByVehicle(vehicle.getVehicleId())));
                    }
                }
            });
            return row;
        });

        VBox content = new VBox(10, title, vehicleLine, metaLine, totalLine, historyTable);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() == closeButton) {
                break;
            }
            if (result.get() == printButton) {
                List<VehicleMaintenanceLog> currentLogs = new ArrayList<>(historyTable.getItems());
                double currentTotal = maintenanceLogDao.findTotalCostByVehicle(vehicle.getVehicleId());
                if (openVehicleMaintenanceHistoryPrintPreview(vehicle, currentLogs, currentTotal)) {
                    continue;
                }
                VBox printable = buildVehicleMaintenanceHistoryPrintableNode(vehicle, currentLogs, currentTotal);
                Window owner = moduleFleetPane != null && moduleFleetPane.getScene() != null
                        ? moduleFleetPane.getScene().getWindow()
                        : null;
                printNode(owner, printable);
            }
        }
    }

    private boolean showMaintenanceLogDetailEditDeleteOption(Vehicle vehicle, VehicleMaintenanceLog log) {
        Dialog<ButtonType> detailDialog = new Dialog<>();
        detailDialog.setTitle("Maintenance Details");
        UiAlerts.applyTheme(detailDialog);

        ButtonType editButton = new ButtonType("Edit", ButtonBar.ButtonData.OTHER);
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        detailDialog.getDialogPane().getButtonTypes().addAll(editButton, deleteButton, closeButton);

        GridPane details = new GridPane();
        details.setHgap(12);
        details.setVgap(8);
        details.add(new Label("Date"), 0, 0);
        details.add(new Label(log.getLogDate() == null ? "" : log.getLogDate().toString()), 1, 0);
        details.add(new Label("Mileage"), 0, 1);
        details.add(new Label(log.getMileage() == null ? "" : String.valueOf(log.getMileage())), 1, 1);
        details.add(new Label("Performed By"), 0, 2);
        details.add(new Label(log.getPerformedBy() == null ? "" : log.getPerformedBy()), 1, 2);
        details.add(new Label("Cost"), 0, 3);
        details.add(new Label(log.getCost() == null ? "" : String.format("$%.2f", log.getCost())), 1, 3);
        details.add(new Label("Description"), 0, 4);
        TextArea description = new TextArea(log.getDescription() == null ? "" : log.getDescription());
        description.setEditable(false);
        description.setWrapText(true);
        description.setPrefRowCount(5);
        details.add(description, 1, 4);

        detailDialog.getDialogPane().setContent(details);

        Optional<ButtonType> result = detailDialog.showAndWait();
        if (result.isPresent() && result.get() == editButton) {
            MaintenanceDialogResult dialogResult = showMaintenanceDialog(vehicle, log);
            if (dialogResult == null) {
                return false;
            }

            if (dialogResult.deleteRequested()) {
                maintenanceLogDao.delete(log.getLogId());
                UiAlerts.info("Maintenance", "Maintenance log deleted.");
                return true;
            }

            MaintenanceFormData formData = dialogResult.formData();
            maintenanceLogDao.update(log.getLogId(), vehicle.getVehicleId(), formData.logDate(),
                    formData.mileage(), formData.cost(), formData.description(), formData.performedBy());
            UiAlerts.info("Maintenance", "Maintenance log updated.");
            return true;
        }
        if (result.isPresent() && result.get() == deleteButton) {
            maintenanceLogDao.delete(log.getLogId());
            UiAlerts.info("Maintenance", "Maintenance log deleted.");
            return true;
        }
        return false;
    }

    private boolean openVehicleMaintenanceHistoryPrintPreview(Vehicle vehicle, List<VehicleMaintenanceLog> logs, double totalCost) {
        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                return false;
            }
            Path tempHtml = Files.createTempFile("quartermaster-vehicle-maintenance-", ".html");
            Files.writeString(tempHtml, buildVehicleMaintenanceHistoryHtml(vehicle, logs, totalCost), StandardCharsets.UTF_8);
            tempHtml.toFile().deleteOnExit();
            desktop.browse(tempHtml.toUri());
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            UiAlerts.error("Print Preview", "Could not open Windows print preview. Falling back to print dialog.");
            return false;
        }
    }

    private String buildVehicleMaintenanceHistoryHtml(Vehicle vehicle, List<VehicleMaintenanceLog> logs, double totalCost) {
        String agencyName = getAgencyName();
        String printedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder rows = new StringBuilder();
        for (VehicleMaintenanceLog log : logs) {
            rows.append("<tr>")
                    .append("<td>").append(escapeHtml(log.getLogDate() == null ? "" : log.getLogDate().toString())).append("</td>")
                    .append("<td>").append(escapeHtml(log.getMileage() == null ? "" : String.valueOf(log.getMileage()))).append("</td>")
                    .append("<td>").append(escapeHtml(log.getDescription())).append("</td>")
                    .append("<td>").append(escapeHtml(log.getPerformedBy())).append("</td>")
                    .append("<td>").append(escapeHtml(log.getCost() == null ? "" : String.format("$%.2f", log.getCost()))).append("</td>")
                    .append("</tr>");
        }

        return """
                <!doctype html>
                <html lang=\"en\">
                <head>
                  <meta charset=\"utf-8\" />
                  <title>Vehicle Maintenance History</title>
                  <style>
                    body { font-family: Segoe UI, Tahoma, Arial, sans-serif; margin: 20px; color: #111; }
                    h1 { margin: 0; font-size: 28px; }
                    h2 { margin: 6px 0 14px 0; font-size: 20px; }
                    .meta { margin: 4px 0; font-size: 14px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                    th, td { border: 1px solid #444; padding: 8px; text-align: left; font-size: 13px; }
                    th { background: #efefef; }
                  </style>
                </head>
                <body onload=\"setTimeout(function(){ window.print(); }, 200);\">
                  <h1>%s</h1>
                  <h2>Vehicle Maintenance History</h2>
                  <div class=\"meta\"><strong>Printed:</strong> %s</div>
                  <div class=\"meta\"><strong>Vehicle:</strong> %s - %s %s</div>
                  <div class=\"meta\"><strong>Type:</strong> %s</div>
                  <div class=\"meta\"><strong>VIN:</strong> %s</div>
                  <div class=\"meta\"><strong>Plate:</strong> %s</div>
                  <div class=\"meta\"><strong>Total Maintenance Cost:</strong> %s</div>
                  <table>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Mileage</th>
                        <th>Description</th>
                        <th>Performed By</th>
                        <th>Cost</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(agencyName),
                escapeHtml(printedAt),
                escapeHtml(vehicle.getUnitNumber()),
                escapeHtml(vehicle.getMake()),
                escapeHtml(vehicle.getModel()),
                escapeHtml(vehicle.getVehicleTypeName()),
                escapeHtml(vehicle.getVin()),
                escapeHtml(vehicle.getPlateNumber()),
                escapeHtml(String.format("$%.2f", totalCost)),
                rows
        );
    }

    private VBox buildVehicleMaintenanceHistoryPrintableNode(Vehicle vehicle, List<VehicleMaintenanceLog> logs, double totalCost) {
        Label title = new Label(getAgencyName());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: black;");
        Label subtitle = new Label("Vehicle Maintenance History");
        subtitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: black;");
        Label printed = new Label("Printed: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        printed.setStyle("-fx-text-fill: #333333;");

        GridPane info = new GridPane();
        info.setHgap(16);
        info.setVgap(8);
        info.add(new Label("Vehicle"), 0, 0);
        info.add(new Label(vehicle.getUnitNumber() + " - " + vehicle.getMake() + " " + vehicle.getModel()), 1, 0);
        info.add(new Label("Type"), 0, 1);
        info.add(new Label(vehicle.getVehicleTypeName() == null ? "" : vehicle.getVehicleTypeName()), 1, 1);
        info.add(new Label("VIN"), 0, 2);
        info.add(new Label(vehicle.getVin() == null ? "" : vehicle.getVin()), 1, 2);
        info.add(new Label("Plate"), 0, 3);
        info.add(new Label(vehicle.getPlateNumber() == null ? "" : vehicle.getPlateNumber()), 1, 3);
        info.add(new Label("Total Maintenance Cost"), 0, 4);
        info.add(new Label(String.format("$%.2f", totalCost)), 1, 4);

        TableView<VehicleMaintenanceLog> table = new TableView<>(FXCollections.observableArrayList(logs));
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Math.min(520, 150 + logs.size() * 26.0));

        TableColumn<VehicleMaintenanceLog, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getLogDate() == null ? "" : cellData.getValue().getLogDate().toString()
        ));
        TableColumn<VehicleMaintenanceLog, Integer> mileageCol = new TableColumn<>("Mileage");
        mileageCol.setCellValueFactory(new PropertyValueFactory<>("mileage"));
        TableColumn<VehicleMaintenanceLog, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<VehicleMaintenanceLog, String> byCol = new TableColumn<>("Performed By");
        byCol.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
        TableColumn<VehicleMaintenanceLog, String> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            Double value = cellData.getValue().getCost();
            return value == null ? "" : String.format("$%.2f", value);
        }));
        table.getColumns().clear();
        table.getColumns().addAll(List.of(dateCol, mileageCol, descriptionCol, byCol, costCol));

        VBox printable = new VBox(12, title, subtitle, printed, info, table);
        printable.setPadding(new Insets(16));
        printable.setStyle("-fx-background-color: white;");
        return printable;
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
    public void onAddLoginPersonnel() {
        UserFormData formData = showUserDialog(null);
        if (formData == null) {
            return;
        }

        String hash = BCrypt.hashpw(formData.password(), BCrypt.gensalt());
        userDao.createUser(formData.username(), hash, formData.role(),
                formData.officer() == null ? null : formData.officer().getOfficerId());
        refreshUsers();
    }

    @FXML
    public void onEditLoginPersonnel() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Users", "Select a login row to edit.");
            return;
        }

        UserFormData formData = showUserDialog(selected);
        if (formData == null) {
            return;
        }

        userDao.updateUser(selected.getUserId(), formData.username(), formData.role(),
                formData.officer() == null ? null : formData.officer().getOfficerId());

        if (!formData.password().isEmpty()) {
            String hash = BCrypt.hashpw(formData.password(), BCrypt.gensalt());
            userDao.updatePasswordHash(selected.getUserId(), hash);
        }

        refreshUsers();
        UiAlerts.info("Users", "Login personnel updated.");
    }

    @FXML
    public void onDeleteLoginPersonnel() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Users", "Select a login row to delete.");
            return;
        }

        if ("admin".equalsIgnoreCase(selected.getUsername())) {
            UiAlerts.error("Users", "Default admin account cannot be deleted.");
            return;
        }

        userDao.deleteUser(selected.getUserId());
        refreshUsers();
    }

    @FXML
    public void onLookupAdd() {
        LookupCategory category = lookupCategoryList.getSelectionModel().getSelectedItem();
        String value = lookupNewItemField.getText() == null ? "" : lookupNewItemField.getText().trim();
        if (category == null) {
            UiAlerts.error("Lookups", "Select a lookup category first.");
            return;
        }
        if (value.isEmpty()) {
            UiAlerts.error("Lookups", "Value is required.");
            return;
        }

        lookupDao.add(category, value);
        lookupNewItemField.clear();
        refreshLookupItems(category);
    }

    @FXML
    public void onLookupRename() {
        LookupCategory category = lookupCategoryList.getSelectionModel().getSelectedItem();
        LookupItem selected = lookupItemList.getSelectionModel().getSelectedItem();
        if (category == null || selected == null) {
            UiAlerts.error("Lookups", "Select both category and item.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Rename Lookup Value");
        dialog.setHeaderText("Category: " + category.getDisplayName());
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField valueField = new TextField(selected.getName());
        dialog.getDialogPane().setContent(valueField);
        dialog.setResultConverter(buttonType -> buttonType == saveButton
                ? (valueField.getText() == null ? "" : valueField.getText().trim())
                : null);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isEmpty()) {
            return;
        }

        lookupDao.rename(category, selected.getId(), result.get());
        refreshLookupItems(category);
    }

    @FXML
    public void onLookupDelete() {
        LookupCategory category = lookupCategoryList.getSelectionModel().getSelectedItem();
        LookupItem selected = lookupItemList.getSelectionModel().getSelectedItem();
        if (category == null || selected == null) {
            UiAlerts.error("Lookups", "Select both category and item.");
            return;
        }

        lookupDao.delete(category, selected.getId());
        refreshLookupItems(category);
    }

    private void setupTables() {
        setupEquipmentTable();
        setupIssueSelectionTables();
        setupIssuanceTable();
        setupOfficerTable();
        setupUsersTable();
        setupVehicleTable();
        setupMaintenanceTable();

        issueOfficerCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Officer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getBadgeNumber() + ")");
            }
        });
        issueOfficerCombo.setButtonCell(issueOfficerCombo.getCellFactory().call(null));

        maintenanceVehicleCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        maintenanceVehicleCombo.setButtonCell(maintenanceVehicleCombo.getCellFactory().call(null));

        issueAvailableTable.setItems(issueAvailableFiltered);
        issueSelectedTable.setItems(issuedToOfficerItems);
        issueAvailableTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        issueSelectedTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        issueSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyIssueAvailableFilter());
        issueOfficerCombo.valueProperty().addListener((obs, oldOfficer, newOfficer) -> {
            clearPendingChanges();
            refreshIssueOptions();
            refreshIssuedItemsForOfficer();
        });

        equipmentTable.setRowFactory(tableView -> {
            TableRow<EquipmentItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEquipmentDetailsDialog(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupIssueSelectionTables() {
        issueAvailableNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        issueAvailableCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorySummary"));
        issueAvailableSerialColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String serial = cellData.getValue().getSerialNumber();
            return serial == null ? "" : serial;
        }));

        issueSelectedNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        issueSelectedCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorySummary"));
        issueSelectedSerialColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String serial = cellData.getValue().getSerialNumber();
            return serial == null ? "" : serial;
        }));
    }

    private void setupFilters() {
        inventoryStatusFilterCombo.setItems(FXCollections.observableArrayList(
                "All",
                EquipmentStatus.AVAILABLE.name(),
                EquipmentStatus.ISSUED.name(),
                EquipmentStatus.MAINTENANCE.name(),
                EquipmentStatus.RETIRED.name()
        ));
        inventoryStatusFilterCombo.getSelectionModel().select("All");
        inventorySearchField.textProperty().addListener((obs, o, n) -> refreshEquipmentAndIssueOptions());
        inventoryStatusFilterCombo.valueProperty().addListener((obs, o, n) -> refreshEquipmentAndIssueOptions());
    }

    private void setupLookupAdmin() {
        lookupCategoryList.setItems(FXCollections.observableArrayList(LookupCategory.values()));
        lookupCategoryList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            refreshLookupItems(selected);
        });
        lookupCategoryList.getSelectionModel().selectFirst();
    }

    private void setupEquipmentTable() {
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        equipmentCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorySummary"));
        equipmentLocationColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String value = cellData.getValue().getStorageLocationName();
            return value == null ? "" : value;
        }));
        equipmentSerialColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        equipmentCostColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            Double cost = cellData.getValue().getReplacementCost();
            return cost == null ? "" : String.format("$%.2f", cost);
        }));
        equipmentConditionColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getCondition().name()
        ));
        equipmentStatusColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getStatus().name()
        ));

        equipmentStatusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("status-available", "status-issued", "status-maintenance", "status-retired");
                if (empty || status == null) {
                    setText(null);
                    return;
                }
                setText(status);
                switch (status) {
                    case "AVAILABLE" -> getStyleClass().add("status-available");
                    case "ISSUED" -> getStyleClass().add("status-issued");
                    case "MAINTENANCE" -> getStyleClass().add("status-maintenance");
                    case "RETIRED" -> getStyleClass().add("status-retired");
                    default -> {
                    }
                }
            }
        });
    }

    private void setupIssuanceTable() {
        issuanceOfficerColumn.setCellValueFactory(new PropertyValueFactory<>("officerName"));
        issuanceItemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        issuanceBranchColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        issuanceCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorySummary"));
        issuanceSerialColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String value = cellData.getValue().getSerialNumber();
            return value == null ? "" : value;
        }));
        issuanceStatusColumn.setCellValueFactory(new PropertyValueFactory<>("itemStatus"));
        issuanceConditionColumn.setCellValueFactory(new PropertyValueFactory<>("itemCondition"));
        issuanceCostColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            Double value = cellData.getValue().getReplacementCost();
            return value == null ? "" : String.format("$%.2f", value);
        }));
        issuanceDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getIssuedDate().toString()
        ));
        issuanceByColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String value = cellData.getValue().getIssuedByUsername();
            return value == null ? "" : value;
        }));
        returnDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getReturnedDate() == null ? "" : cellData.getValue().getReturnedDate().toString()
        ));
        returnByColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String value = cellData.getValue().getReturnedByUsername();
            return value == null ? "" : value;
        }));
    }

    private void setupOfficerTable() {
        officerIdColumn.setCellValueFactory(new PropertyValueFactory<>("officerId"));
        officerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        officerRankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        officerBadgeColumn.setCellValueFactory(new PropertyValueFactory<>("badgeNumber"));
    }

    private void setupUsersTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getRole().name()
        ));
        linkedOfficerColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            Integer officerId = cellData.getValue().getOfficerId();
            return officerId == null ? "" : String.valueOf(officerId);
        }));
    }

    private void setupVehicleTable() {
        vehicleUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unitNumber"));
        vehicleTypeColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String value = cellData.getValue().getVehicleTypeName();
            return value == null ? "" : value;
        }));
        vehicleMakeColumn.setCellValueFactory(new PropertyValueFactory<>("make"));
        vehicleModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        vehicleYearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        vehicleVinColumn.setCellValueFactory(new PropertyValueFactory<>("vin"));
        vehiclePlateColumn.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));

        vehiclesTable.setRowFactory(tableView -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showVehicleMaintenanceHistoryDialog(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupMaintenanceTable() {
        maintenanceDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getLogDate().toString()
        ));
        maintenanceMileageColumn.setCellValueFactory(new PropertyValueFactory<>("mileage"));
        maintenanceDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        maintenanceByColumn.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
        maintenanceCostColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            Double value = cellData.getValue().getCost();
            return value == null ? "" : String.format("$%.2f", value);
        }));

        maintenanceTable.setRowFactory(tableView -> {
            TableRow<VehicleMaintenanceLog> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    onEditMaintenanceLog();
                }
            });
            return row;
        });
    }

    private void refreshAll() {
        refreshOfficers();
        refreshUsers();
        loadAgencySettings();
        refreshEquipmentAndIssueOptions();
        refreshIssueOptions();
        refreshIssuances();
        refreshVehiclesAndMaintenanceOptions();
    }

    private void loadAgencySettings() {
        if (agencyNameField == null) {
            return;
        }
        agencyNameField.setText(settingsDao.getAgencyName());
    }

    private String getAgencyName() {
        String value = agencyNameField == null ? "" : text(agencyNameField);
        if (!value.isBlank()) {
            return value;
        }
        return settingsDao.getAgencyName();
    }

    private void showEquipmentDetailsDialog(EquipmentItem item) {
        if (item == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Equipment Details");
        UiAlerts.applyTheme(dialog);

        ButtonType editButton = new ButtonType("Edit Item", ButtonBar.ButtonData.OTHER);
        ButtonType printButton = new ButtonType("Print History", ButtonBar.ButtonData.OTHER);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(editButton, printButton, closeButton);

        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(8);
        details.add(new Label("Name"), 0, 0);
        details.add(new Label(item.getName()), 1, 0);
        details.add(new Label("Branch"), 0, 1);
        details.add(new Label(item.getBranch().getDisplayName()), 1, 1);
        details.add(new Label("Category"), 0, 2);
        details.add(new Label(item.getCategorySummary()), 1, 2);
        details.add(new Label("Serial"), 0, 3);
        details.add(new Label(item.getSerialNumber() == null ? "" : item.getSerialNumber()), 1, 3);
        details.add(new Label("Replacement Cost"), 0, 4);
        details.add(new Label(item.getReplacementCost() == null ? "" : String.format("$%.2f", item.getReplacementCost())), 1, 4);
        details.add(new Label("Status"), 0, 5);
        details.add(new Label(item.getStatus().name()), 1, 5);

        TableView<IssuanceAdminRow> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        historyTable.setPrefHeight(240);

        TableColumn<IssuanceAdminRow, String> issuedToCol = new TableColumn<>("Issued To");
        issuedToCol.setCellValueFactory(new PropertyValueFactory<>("officerName"));
        TableColumn<IssuanceAdminRow, String> issuedByCol = new TableColumn<>("Issued By");
        issuedByCol.setCellValueFactory(new PropertyValueFactory<>("issuedByUsername"));
        TableColumn<IssuanceAdminRow, String> issuedDateCol = new TableColumn<>("Issued Date");
        issuedDateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getIssuedDate() == null ? "" : cellData.getValue().getIssuedDate().toString()
        ));
        TableColumn<IssuanceAdminRow, String> returnedByCol = new TableColumn<>("Returned By");
        returnedByCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String value = cellData.getValue().getReturnedByUsername();
            return value == null ? "" : value;
        }));
        TableColumn<IssuanceAdminRow, String> returnedDateCol = new TableColumn<>("Returned Date");
        returnedDateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getReturnedDate() == null ? "" : cellData.getValue().getReturnedDate().toString()
        ));
        historyTable.getColumns().clear();
        historyTable.getColumns().addAll(List.of(issuedToCol, issuedByCol, issuedDateCol, returnedByCol, returnedDateCol));

        List<IssuanceAdminRow> history = issuanceDao.findByItemId(item.getItemId());
        historyTable.setItems(FXCollections.observableArrayList(history));

        VBox content = new VBox(10,
                new Label("Item Information"),
                details,
                new Label("Item History"),
                historyTable
        );
        content.setPadding(new Insets(8));

        dialog.getDialogPane().setContent(content);

        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() == closeButton) {
                break;
            }
            if (result.get() == editButton) {
                EquipmentFormData formData = showEquipmentDialog(item);
                if (formData != null) {
                    equipmentDao.update(formData.toItem(item.getItemId(), formData.serialNumber()));
                    refreshEquipmentAndIssueOptions();
                    int currentItemId = item.getItemId();
                    item = equipmentTable.getItems().stream()
                            .filter(eq -> eq.getItemId() == currentItemId)
                            .findFirst()
                            .orElse(item);
                    history = issuanceDao.findByItemId(item.getItemId());
                    historyTable.setItems(FXCollections.observableArrayList(history));
                }
            }
            if (result.get() == printButton) {
                printEquipmentHistory(item, history);
            }
        }
    }

    private void printEquipmentHistory(EquipmentItem item, List<IssuanceAdminRow> history) {
        if (openEquipmentHistoryPrintPreview(item, history)) {
            return;
        }

        VBox printable = buildEquipmentHistoryPrintableNode(item, history);
        Window owner = moduleInventoryPane != null && moduleInventoryPane.getScene() != null
                ? moduleInventoryPane.getScene().getWindow()
                : null;
        printNode(owner, printable);
    }

    private boolean openEquipmentHistoryPrintPreview(EquipmentItem item, List<IssuanceAdminRow> history) {
        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                return false;
            }
            Path tempHtml = Files.createTempFile("quartermaster-item-history-", ".html");
            Files.writeString(tempHtml, buildEquipmentHistoryHtml(item, history), StandardCharsets.UTF_8);
            tempHtml.toFile().deleteOnExit();
            desktop.browse(tempHtml.toUri());
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            UiAlerts.error("Print Preview", "Could not open Windows print preview. Falling back to print dialog.");
            return false;
        }
    }

    private String buildEquipmentHistoryHtml(EquipmentItem item, List<IssuanceAdminRow> history) {
        String agencyName = getAgencyName();
        String printedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        StringBuilder rows = new StringBuilder();
        for (IssuanceAdminRow row : history) {
            rows.append("<tr>")
                    .append("<td>").append(escapeHtml(row.getOfficerName())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getIssuedByUsername())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getIssuedDate() == null ? "" : row.getIssuedDate().toString())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getReturnedByUsername())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getReturnedDate() == null ? "" : row.getReturnedDate().toString())).append("</td>")
                    .append("</tr>");
        }

        return """
                <!doctype html>
                <html lang=\"en\">
                <head>
                  <meta charset=\"utf-8\" />
                  <title>Equipment History</title>
                  <style>
                    body { font-family: Segoe UI, Tahoma, Arial, sans-serif; margin: 20px; color: #111; }
                    h1 { margin: 0; font-size: 28px; }
                    h2 { margin: 6px 0 14px 0; font-size: 20px; }
                    .meta { margin: 4px 0; font-size: 14px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                    th, td { border: 1px solid #444; padding: 8px; text-align: left; font-size: 13px; }
                    th { background: #efefef; }
                  </style>
                </head>
                <body onload=\"setTimeout(function(){ window.print(); }, 200);\">
                  <h1>%s</h1>
                  <h2>Equipment Item History</h2>
                  <div class=\"meta\"><strong>Printed:</strong> %s</div>
                  <div class=\"meta\"><strong>Item:</strong> %s</div>
                  <div class=\"meta\"><strong>Category:</strong> %s</div>
                  <div class=\"meta\"><strong>Serial:</strong> %s</div>
                  <div class=\"meta\"><strong>Replacement Cost:</strong> %s</div>
                  <table>
                    <thead>
                      <tr>
                        <th>Issued To</th>
                        <th>Issued By</th>
                        <th>Issued Date</th>
                        <th>Returned By</th>
                        <th>Returned Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>
                                    <p style=\"margin-top: 14px; font-size: 12px;\"><strong>Officer Responsibility:</strong> Officers are responsible for all issued items. Lost or damaged items will be replaced at the listed replacement cost.</p>
                </body>
                </html>
                """.formatted(
                escapeHtml(agencyName),
                escapeHtml(printedAt),
                escapeHtml(item.getName()),
                escapeHtml(item.getCategorySummary()),
                escapeHtml(item.getSerialNumber()),
                                escapeHtml(item.getReplacementCost() == null ? "" : String.format("$%.2f", item.getReplacementCost())),
                rows
        );
    }

    private VBox buildEquipmentHistoryPrintableNode(EquipmentItem item, List<IssuanceAdminRow> history) {
        Label title = new Label(getAgencyName());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: black;");
        Label subtitle = new Label("Equipment Item History");
        subtitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: black;");
        Label printed = new Label("Printed: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        printed.setStyle("-fx-text-fill: #333333;");

        GridPane info = new GridPane();
        info.setHgap(16);
        info.setVgap(8);
        info.add(new Label("Item"), 0, 0);
        info.add(new Label(item.getName()), 1, 0);
        info.add(new Label("Category"), 0, 1);
        info.add(new Label(item.getCategorySummary()), 1, 1);
        info.add(new Label("Serial"), 0, 2);
        info.add(new Label(item.getSerialNumber() == null ? "" : item.getSerialNumber()), 1, 2);
        info.add(new Label("Replacement Cost"), 0, 3);
        info.add(new Label(item.getReplacementCost() == null ? "" : String.format("$%.2f", item.getReplacementCost())), 1, 3);

        TableView<IssuanceAdminRow> table = new TableView<>(FXCollections.observableArrayList(history));
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Math.min(520, 160 + history.size() * 26.0));

        TableColumn<IssuanceAdminRow, String> issuedToCol = new TableColumn<>("Issued To");
        issuedToCol.setCellValueFactory(new PropertyValueFactory<>("officerName"));
        TableColumn<IssuanceAdminRow, String> issuedByCol = new TableColumn<>("Issued By");
        issuedByCol.setCellValueFactory(new PropertyValueFactory<>("issuedByUsername"));
        TableColumn<IssuanceAdminRow, String> issuedDateCol = new TableColumn<>("Issued Date");
        issuedDateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getIssuedDate() == null ? "" : cellData.getValue().getIssuedDate().toString()
        ));
        TableColumn<IssuanceAdminRow, String> returnedByCol = new TableColumn<>("Returned By");
        returnedByCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String value = cellData.getValue().getReturnedByUsername();
            return value == null ? "" : value;
        }));
        TableColumn<IssuanceAdminRow, String> returnedDateCol = new TableColumn<>("Returned Date");
        returnedDateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getReturnedDate() == null ? "" : cellData.getValue().getReturnedDate().toString()
        ));
        table.getColumns().clear();
        table.getColumns().addAll(List.of(issuedToCol, issuedByCol, issuedDateCol, returnedByCol, returnedDateCol));

        Label responsibility = new Label("Officer Responsibility: Officers are responsible for all issued items. Lost or damaged items will be replaced at the listed replacement cost.");
        responsibility.setWrapText(true);
        responsibility.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");

        VBox printable = new VBox(12, title, subtitle, printed, info, table, responsibility);
        printable.setPadding(new Insets(16));
        printable.setStyle("-fx-background-color: white;");
        return printable;
    }

    private void refreshOfficers() {
        List<Officer> officers = officerDao.findAll();
        ObservableList<Officer> list = FXCollections.observableArrayList(officers);
        officersTable.setItems(list);
        issueOfficerCombo.setItems(FXCollections.observableArrayList(officers));
    }

    private void refreshUsers() {
        usersTable.setItems(FXCollections.observableArrayList(userDao.findAll()));
    }

    private void refreshEquipmentAndIssueOptions() {
        List<EquipmentItem> byBranch = equipmentDao.findByBranch(activeEquipmentBranch);
        List<EquipmentItem> filtered = byBranch.stream()
                .filter(this::matchesInventoryFilter)
                .collect(Collectors.toList());

        equipmentTable.setItems(FXCollections.observableArrayList(filtered));
        inventorySummaryLabel.setText(buildInventorySummaryText(filtered, byBranch.size()));

        boolean weaponView = activeEquipmentBranch == EquipmentBranch.WEAPON;
        attachItemButton.setVisible(weaponView);
        attachItemButton.setManaged(weaponView);
        removeAttachmentButton.setVisible(weaponView);
        removeAttachmentButton.setManaged(weaponView);

        refreshIssueOptions();
    }

    private String buildInventorySummaryText(List<EquipmentItem> filtered, int byBranchTotal) {
        String base = "Showing " + filtered.size() + " of " + byBranchTotal + " items";
        if (activeEquipmentBranch != EquipmentBranch.UNIFORM) {
            return base;
        }

        Map<String, Long> grouped = filtered.stream()
                .collect(Collectors.groupingBy(EquipmentItem::getCategorySummary, Collectors.counting()));
        if (grouped.isEmpty()) {
            return base;
        }

        String breakdown = grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> entry.getKey() + " x" + entry.getValue())
                .collect(Collectors.joining(" | "));
        return base + "\nUniform counts: " + breakdown;
    }

    private void refreshIssueOptions() {
        List<EquipmentItem> available = equipmentDao.findAll().stream()
                .filter(item -> item.getStatus() == EquipmentStatus.AVAILABLE)
                .filter(item -> !item.isAttachment())
                .sorted(Comparator.comparing(EquipmentItem::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        for (IssuedItemRow pendingReturn : pendingReturnRowsByIssuanceId.values()) {
            available.add(toPendingAvailablePlaceholder(pendingReturn));
        }
        available.sort(Comparator.comparing(EquipmentItem::getName, String.CASE_INSENSITIVE_ORDER));

        issueAvailableSource.setAll(available);
        applyIssueAvailableFilter();
    }

    private void refreshIssuedItemsForOfficer() {
        Officer officer = issueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            issuedToOfficerItems.setAll(List.of());
            return;
        }
        List<IssuedItemRow> issued = new ArrayList<>(issuanceDao.findActiveIssuedItemsByOfficer(officer.getOfficerId()));
        issued.removeIf(row -> pendingReturnIssuanceIds.contains(row.getIssuanceId()));
        issued.addAll(pendingIssuedRowsByItemId.values());
        issuedToOfficerItems.setAll(issued);
    }

    private void applyIssueAvailableFilter() {
        String query = issueSearchField.getText() == null ? "" : issueSearchField.getText().trim().toLowerCase();
        issueAvailableFiltered.setPredicate(item -> {
            if (query.isEmpty()) {
                return true;
            }
            String serial = item.getSerialNumber() == null ? "" : item.getSerialNumber().toLowerCase();
            return item.getName().toLowerCase().contains(query)
                    || item.getCategorySummary().toLowerCase().contains(query)
                    || serial.contains(query);
        });
    }

    private void refreshIssuances() {
        issuanceTable.setItems(FXCollections.observableArrayList(issuanceDao.findAll()));
    }

        private void showIssuancePrintSheet(Officer officer, List<IssuedItemRow> returnedItems) {
        List<IssuedItemRow> currentIssued = issuanceDao.findActiveIssuedItemsByOfficer(officer.getOfficerId());
        if (currentIssued.isEmpty() && (returnedItems == null || returnedItems.isEmpty())) {
            return;
        }

            if (openWindowsPrintPreview(officer, currentIssued, returnedItems)) {
                        return;
                }

        VBox printable = buildIssuancePrintableNode(officer, currentIssued, returnedItems);
        Window owner = moduleIssuePane != null && moduleIssuePane.getScene() != null
                ? moduleIssuePane.getScene().getWindow()
                : null;
        printNode(owner, printable);
    }

        private boolean openWindowsPrintPreview(Officer officer, List<IssuedItemRow> currentIssued,
                            List<IssuedItemRow> returnedItems) {
                try {
                        if (!Desktop.isDesktopSupported()) {
                                return false;
                        }
                        Desktop desktop = Desktop.getDesktop();
                        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                                return false;
                        }

                        Path tempHtml = Files.createTempFile("quartermaster-signoff-", ".html");
            Files.writeString(tempHtml, buildIssuancePrintHtml(officer, currentIssued, returnedItems), StandardCharsets.UTF_8);
                        tempHtml.toFile().deleteOnExit();
                        desktop.browse(tempHtml.toUri());
                        return true;
                } catch (IOException ex) {
                        ex.printStackTrace();
                        UiAlerts.error("Print Preview", "Could not open Windows print preview. Falling back to print dialog.");
                        return false;
                }
        }

            private String buildIssuancePrintHtml(Officer officer, List<IssuedItemRow> currentIssued,
                                  List<IssuedItemRow> returnedItems) {
                String agencyName = getAgencyName();
                String adminName = SessionManager.getCurrentUser() == null
                                ? ""
                                : SessionManager.getCurrentUser().getUsername();
                String printedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                StringBuilder rows = new StringBuilder();
                for (IssuedItemRow row : currentIssued) {
                        String issuedDate = row.getIssuedDate() == null ? "" : row.getIssuedDate().toString();
                    String replacementCost = row.getReplacementCost() == null
                            ? ""
                            : String.format("$%.2f", row.getReplacementCost());
                        rows.append("<tr>")
                                        .append("<td>").append(escapeHtml(row.getItemName())).append("</td>")
                                        .append("<td>").append(escapeHtml(row.getCategorySummary())).append("</td>")
                                        .append("<td>").append(escapeHtml(row.getSerialNumber())).append("</td>")
                            .append("<td>").append(escapeHtml(replacementCost)).append("</td>")
                                        .append("<td>").append(escapeHtml(issuedDate)).append("</td>")
                                        .append("</tr>");
                }

                StringBuilder returnedRows = new StringBuilder();
                if (returnedItems != null) {
                        for (IssuedItemRow row : returnedItems) {
                                String issuedDate = row.getIssuedDate() == null ? "" : row.getIssuedDate().toString();
                            String replacementCost = row.getReplacementCost() == null
                                    ? ""
                                    : String.format("$%.2f", row.getReplacementCost());
                                returnedRows.append("<tr>")
                                                .append("<td>").append(escapeHtml(row.getItemName())).append("</td>")
                                                .append("<td>").append(escapeHtml(row.getCategorySummary())).append("</td>")
                                                .append("<td>").append(escapeHtml(row.getSerialNumber())).append("</td>")
                                    .append("<td>").append(escapeHtml(replacementCost)).append("</td>")
                                                .append("<td>").append(escapeHtml(issuedDate)).append("</td>")
                                                .append("</tr>");
                        }
                }

                String returnedSection = returnedRows.isEmpty()
                                ? ""
                                : """
                                    <h2 style=\"margin-top: 20px; margin-bottom: 6px; font-size: 20px;\">Returned Items (This Save)</h2>
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Name</th>
                                                <th>Category</th>
                                                <th>Serial</th>
                                                <th>Replacement Cost</th>
                                                <th>Originally Issued</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            %s
                                        </tbody>
                                    </table>
                                """.formatted(returnedRows);

                return """
                                <!doctype html>
                                <html lang=\"en\">
                                <head>
                                    <meta charset=\"utf-8\" />
                                    <title>%s - Officer Equipment Sign-Off</title>
                                    <style>
                                        body { font-family: Segoe UI, Tahoma, Arial, sans-serif; margin: 20px; color: #111; }
                                        h1 { margin: 0; font-size: 28px; }
                                        h2 { margin: 6px 0 10px 0; font-size: 20px; }
                                        .meta { margin: 4px 0; font-size: 14px; }
                                        table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                                        th, td { border: 1px solid #444; padding: 8px; text-align: left; font-size: 13px; }
                                        th { background: #efefef; }
                                        .sign { margin-top: 24px; font-size: 16px; }
                                        @media print {
                                            body { margin: 14px; }
                                        }
                                    </style>
                                </head>
                                <body onload=\"setTimeout(function(){ window.print(); }, 200);\">
                                    <h1>%s</h1>
                                    <h2>Equipment Issue / Return Sign-Off</h2>
                                    <div class=\"meta\"><strong>Printed:</strong> %s</div>
                                    <div class=\"meta\"><strong>Officer:</strong> %s (%s)</div>
                                    <div class=\"meta\"><strong>Prepared By:</strong> %s</div>
                                    <div class=\"meta\"><strong>Issued Items:</strong> %s</div>
                                    <div class=\"meta\"><strong>Returned Items:</strong> %s</div>
                                    <h2 style=\"margin-top: 16px; margin-bottom: 6px; font-size: 20px;\">Currently Issued</h2>
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Name</th>
                                                <th>Category</th>
                                                <th>Serial</th>
                                                <th>Replacement Cost</th>
                                                <th>Issued Date</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            %s
                                        </tbody>
                                    </table>
                                    %s
                                    <div class=\"sign\">Issued By (%s): ________________________________</div>
                                    <div class=\"sign\">Officer Signature (%s): ________________________________</div>
                                    <p style="margin-top: 14px; font-size: 12px;"><strong>Officer Responsibility:</strong> Officers are responsible for all issued items. Lost or damaged items will be replaced at the listed replacement cost.</p>
                                </body>
                                </html>
                                """.formatted(
                                escapeHtml(agencyName),
                                escapeHtml(agencyName),
                                escapeHtml(printedAt),
                                escapeHtml(officer.getName()),
                                escapeHtml(officer.getBadgeNumber()),
                                escapeHtml(adminName),
                                currentIssued.size(),
                                returnedItems == null ? 0 : returnedItems.size(),
                                rows,
                                returnedSection,
                                escapeHtml(adminName),
                                escapeHtml(officer.getName())
                );
        }

        private String escapeHtml(String value) {
                if (value == null) {
                        return "";
                }
                return value
                                .replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;")
                                .replace("\"", "&quot;")
                                .replace("'", "&#39;");
        }

                    private void stageIssue(EquipmentItem item) {
                        pendingIssueItemsById.put(item.getItemId(), item);
                        IssuedItemRow staged = new IssuedItemRow(
                                -item.getItemId(),
                                item.getName(),
                                item.getBranch(),
                                item.getCategorySummary(),
                                item.getSerialNumber(),
                            item.getReplacementCost(),
                                issueDatePicker.getValue() == null ? LocalDate.now() : issueDatePicker.getValue(),
                                null
                        );
                        pendingIssuedRowsByItemId.put(item.getItemId(), staged);
                        issueAvailableSource.remove(item);
                        issuedToOfficerItems.add(staged);
                    }

                    private void cancelStagedIssue(int stagedIssuanceId) {
                        int itemId = -stagedIssuanceId;
                        EquipmentItem original = pendingIssueItemsById.remove(itemId);
                        pendingIssuedRowsByItemId.remove(itemId);

                        issuedToOfficerItems.removeIf(row -> row.getIssuanceId() == stagedIssuanceId);
                        if (original != null) {
                            issueAvailableSource.add(original);
                            sortIssueAvailableSource();
                        }
                    }

                    private void stageReturn(IssuedItemRow row) {
                        pendingReturnIssuanceIds.add(row.getIssuanceId());
                        pendingReturnRowsByIssuanceId.put(row.getIssuanceId(), row);
                        issuedToOfficerItems.remove(row);
                        issueAvailableSource.add(toPendingAvailablePlaceholder(row));
                        sortIssueAvailableSource();
                    }

                    private void cancelStagedReturn(int issuanceId) {
                        IssuedItemRow row = pendingReturnRowsByIssuanceId.remove(issuanceId);
                        pendingReturnIssuanceIds.remove(issuanceId);
                        issueAvailableSource.removeIf(item -> item.getItemId() == -issuanceId);
                        if (row != null) {
                            issuedToOfficerItems.add(row);
                        }
                    }

                    private EquipmentItem toPendingAvailablePlaceholder(IssuedItemRow row) {
                        return new EquipmentItem(
                                -row.getIssuanceId(),
                                row.getItemName(),
                                row.getBranch(),
                                null, null,
                                null, null,
                                null, null,
                                null, null,
                                null, null,
                                row.getSerialNumber(),
                                row.getReplacementCost(),
                                EquipmentCondition.GOOD,
                                EquipmentStatus.AVAILABLE,
                                false
                        );
                    }

                    private void sortIssueAvailableSource() {
                        FXCollections.sort(issueAvailableSource, Comparator.comparing(EquipmentItem::getName, String.CASE_INSENSITIVE_ORDER));
                    }

                    private void clearPendingChanges() {
                        pendingIssueItemsById.clear();
                        pendingIssuedRowsByItemId.clear();
                        pendingReturnRowsByIssuanceId.clear();
                        pendingReturnIssuanceIds.clear();
                    }

                    private Integer currentUserIdOrNull() {
                        return SessionManager.getCurrentUser() == null ? null : SessionManager.getCurrentUser().getUserId();
                    }

    private VBox buildIssuancePrintableNode(Officer officer, List<IssuedItemRow> currentIssued,
                                            List<IssuedItemRow> returnedItems) {
        String agencyName = getAgencyName();
        String adminName = SessionManager.getCurrentUser() == null
                ? ""
                : SessionManager.getCurrentUser().getUsername();

        Label title = new Label(agencyName);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: black;");
        Label subtitle = new Label("Equipment Issue / Return Sign-Off");
        subtitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: black;");

        Label timestamp = new Label("Printed: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        timestamp.setStyle("-fx-text-fill: #333333;");

        GridPane details = new GridPane();
        details.setHgap(16);
        details.setVgap(8);
        details.add(new Label("Officer"), 0, 0);
        details.add(new Label(officer.getName() + " (" + officer.getBadgeNumber() + ")"), 1, 0);
        details.add(new Label("Prepared By"), 0, 1);
        details.add(new Label(adminName), 1, 1);
        details.add(new Label("Issued Items"), 0, 2);
        details.add(new Label(String.valueOf(currentIssued.size())), 1, 2);
        details.add(new Label("Returned Items"), 0, 3);
        details.add(new Label(String.valueOf(returnedItems == null ? 0 : returnedItems.size())), 1, 3);

        TableView<IssuedItemRow> table = new TableView<>(FXCollections.observableArrayList(currentIssued));
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Math.min(520, 150 + currentIssued.size() * 26.0));

        TableColumn<IssuedItemRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        TableColumn<IssuedItemRow, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categorySummary"));
        TableColumn<IssuedItemRow, String> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        TableColumn<IssuedItemRow, String> costCol = new TableColumn<>("Replacement Cost");
        costCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            Double value = cellData.getValue().getReplacementCost();
            return value == null ? "" : String.format("$%.2f", value);
        }));
        TableColumn<IssuedItemRow, String> dateCol = new TableColumn<>("Issued Date");
        dateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getIssuedDate() == null ? "" : cellData.getValue().getIssuedDate().toString()
        ));
        table.getColumns().clear();
        table.getColumns().addAll(List.of(nameCol, categoryCol, serialCol, costCol, dateCol));

        VBox returnedSection = new VBox(6);
        if (returnedItems != null && !returnedItems.isEmpty()) {
            Label returnedTitle = new Label("Returned Items (This Save)");
            returnedTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: black;");

            TableView<IssuedItemRow> returnedTable = new TableView<>(FXCollections.observableArrayList(returnedItems));
            returnedTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
            returnedTable.setPrefHeight(Math.min(320, 120 + returnedItems.size() * 26.0));

            TableColumn<IssuedItemRow, String> returnedNameCol = new TableColumn<>("Name");
            returnedNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            TableColumn<IssuedItemRow, String> returnedCategoryCol = new TableColumn<>("Category");
            returnedCategoryCol.setCellValueFactory(new PropertyValueFactory<>("categorySummary"));
            TableColumn<IssuedItemRow, String> returnedSerialCol = new TableColumn<>("Serial");
            returnedSerialCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
            TableColumn<IssuedItemRow, String> returnedCostCol = new TableColumn<>("Replacement Cost");
            returnedCostCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
                Double value = cellData.getValue().getReplacementCost();
                return value == null ? "" : String.format("$%.2f", value);
            }));
            TableColumn<IssuedItemRow, String> returnedDateCol = new TableColumn<>("Originally Issued");
            returnedDateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getIssuedDate() == null ? "" : cellData.getValue().getIssuedDate().toString()
            ));
            returnedTable.getColumns().clear();
            returnedTable.getColumns().addAll(List.of(returnedNameCol, returnedCategoryCol, returnedSerialCol, returnedCostCol, returnedDateCol));

            returnedSection.getChildren().addAll(returnedTitle, returnedTable);
        }

        Label line1 = new Label("Issued By (" + adminName + "): ________________________________");
        line1.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        Label line2 = new Label("Officer Signature (" + officer.getName() + "): ________________________________");
        line2.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        Label responsibility = new Label("Officer Responsibility: Officers are responsible for all issued items. Lost or damaged items will be replaced at the listed replacement cost.");
        responsibility.setWrapText(true);
        responsibility.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");

        VBox printable = new VBox(12, title, subtitle, timestamp, details, table, returnedSection, line1, line2, responsibility);
        printable.setPadding(new Insets(16));
        printable.setStyle("-fx-background-color: white;");
        return printable;
    }

    private boolean printNode(Window owner, Node node) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob == null) {
            UiAlerts.error("Print", "No printer is available.");
            return false;
        }
        if (!printerJob.showPrintDialog(owner)) {
            return false;
        }

        Printer printer = printerJob.getPrinter();
        PageLayout pageLayout = printer.createPageLayout(
                Paper.NA_LETTER,
                PageOrientation.PORTRAIT,
                Printer.MarginType.DEFAULT
        );
        printerJob.getJobSettings().setPageLayout(pageLayout);

        // Attach to a temporary scene to ensure CSS and layout are applied before measuring for print scale.
        Group printRoot = new Group(node);
        new javafx.scene.Scene(printRoot);
        printRoot.applyCss();
        printRoot.layout();

        double width = Math.max(1, node.getLayoutBounds().getWidth());
        double height = Math.max(1, node.getLayoutBounds().getHeight());
        double scaleX = pageLayout.getPrintableWidth() / width;
        double scaleY = pageLayout.getPrintableHeight() / height;
        double scale = Math.min(1.0, Math.min(scaleX, scaleY));

        Scale transform = new Scale(scale, scale);
        node.getTransforms().add(transform);
        boolean printed = printerJob.printPage(pageLayout, node);
        node.getTransforms().remove(transform);

        if (printed) {
            printerJob.endJob();
        }

        printRoot.getChildren().clear();
        return printed;
    }

    private List<EquipmentItem> selectedOrFocusedAvailableRows() {
        List<EquipmentItem> selected = new ArrayList<>(issueAvailableTable.getSelectionModel().getSelectedItems());
        if (!selected.isEmpty()) {
            return selected;
        }

        EquipmentItem single = issueAvailableTable.getSelectionModel().getSelectedItem();
        if (single != null) {
            return List.of(single);
        }

        int focusedIndex = issueAvailableTable.getFocusModel().getFocusedIndex();
        if (focusedIndex >= 0 && focusedIndex < issueAvailableTable.getItems().size()) {
            EquipmentItem focused = issueAvailableTable.getItems().get(focusedIndex);
            return focused == null ? List.of() : List.of(focused);
        }
        return List.of();
    }

    private List<IssuedItemRow> selectedOrFocusedIssuedRows() {
        List<IssuedItemRow> selected = new ArrayList<>(issueSelectedTable.getSelectionModel().getSelectedItems());
        if (!selected.isEmpty()) {
            return selected;
        }

        IssuedItemRow single = issueSelectedTable.getSelectionModel().getSelectedItem();
        if (single != null) {
            return List.of(single);
        }

        int focusedIndex = issueSelectedTable.getFocusModel().getFocusedIndex();
        if (focusedIndex >= 0 && focusedIndex < issueSelectedTable.getItems().size()) {
            IssuedItemRow focused = issueSelectedTable.getItems().get(focusedIndex);
            return focused == null ? List.of() : List.of(focused);
        }
        return List.of();
    }

    private void refreshVehiclesAndMaintenanceOptions() {
        List<Vehicle> vehicles = vehicleDao.findAll();
        ObservableList<Vehicle> list = FXCollections.observableArrayList(vehicles);
        vehiclesTable.setItems(list);

        Vehicle previous = maintenanceVehicleCombo.getSelectionModel().getSelectedItem();
        maintenanceVehicleCombo.setItems(list);
        if (previous != null) {
            maintenanceVehicleCombo.getSelectionModel().select(
                    vehicles.stream()
                            .filter(v -> v.getVehicleId() == previous.getVehicleId())
                            .findFirst()
                            .orElse(null)
            );
        }
        onMaintenanceVehicleChanged();
    }

    private boolean matchesInventoryFilter(EquipmentItem item) {
        String selectedStatus = inventoryStatusFilterCombo.getSelectionModel().getSelectedItem();
        if (selectedStatus != null && !"All".equals(selectedStatus) && !selectedStatus.equals(item.getStatus().name())) {
            return false;
        }

        String query = inventorySearchField.getText() == null ? "" : inventorySearchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            return true;
        }

        String serial = item.getSerialNumber() == null ? "" : item.getSerialNumber().toLowerCase();
        String location = item.getStorageLocationName() == null ? "" : item.getStorageLocationName().toLowerCase();
        return item.getName().toLowerCase().contains(query)
                || item.getCategorySummary().toLowerCase().contains(query)
                || serial.contains(query)
                || location.contains(query);
    }

    private void showEquipmentSubNav() {
        moduleNavLabel.setText("Equipment Categories");

        Button weaponsButton = createSubNavButton("Weapons", () -> setEquipmentBranch(EquipmentBranch.WEAPON));
        Button dutyButton = createSubNavButton("Duty", () -> setEquipmentBranch(EquipmentBranch.EQUIPMENT));
        Button uniformsButton = createSubNavButton("Uniforms", () -> setEquipmentBranch(EquipmentBranch.UNIFORM));
        Button addButton = createSubNavButton("Add Equipment", this::onAddEquipment);

        moduleNavBar.getChildren().setAll(moduleNavLabel, weaponsButton, dutyButton, uniformsButton, addButton);
        setEquipmentBranch(activeEquipmentBranch);
    }

    private void setEquipmentBranch(EquipmentBranch branch) {
        this.activeEquipmentBranch = branch;
        inventoryModuleTitleLabel.setText(branch.getDisplayName());

        List<Button> buttons = getSubNavButtons();
        if (buttons.size() >= 3) {
            clearActive(buttons);
            switch (branch) {
                case WEAPON -> buttons.get(0).getStyleClass().add("active");
                case EQUIPMENT -> buttons.get(1).getStyleClass().add("active");
                case UNIFORM -> buttons.get(2).getStyleClass().add("active");
            }
        }

        refreshEquipmentAndIssueOptions();
    }

    private void showAdminSubNav(AdminSubTab subTab) {
        moduleNavLabel.setText("Admin");

        Button personnelButton = createSubNavButton("Officers", () -> switchAdminSubTab(AdminSubTab.PERSONNEL));
        Button lookupsButton = createSubNavButton("Lookups", () -> switchAdminSubTab(AdminSubTab.LOOKUPS));
        Button historicalButton = createSubNavButton("Historical", () -> switchAdminSubTab(AdminSubTab.HISTORICAL));
        Button settingsButton = createSubNavButton("Settings", () -> switchAdminSubTab(AdminSubTab.SETTINGS));

        moduleNavBar.getChildren().setAll(moduleNavLabel, personnelButton, lookupsButton, historicalButton, settingsButton);
        switchAdminSubTab(subTab);
    }

    private void switchAdminSubTab(AdminSubTab subTab) {
        List<Button> buttons = getSubNavButtons();
        clearActive(buttons);
        switch (subTab) {
            case PERSONNEL -> {
                adminSubModuleTitle.setText("Personnel");
                setPaneVisible(adminPersonnelPane, true);
                setPaneVisible(adminLookupsPane, false);
                setPaneVisible(adminSettingsPane, false);
                setPaneVisible(adminHistoryPane, false);
                if (buttons.size() > 0) {
                    buttons.get(0).getStyleClass().add("active");
                }
            }
            case LOOKUPS -> {
                adminSubModuleTitle.setText("Lookups");
                setPaneVisible(adminPersonnelPane, false);
                setPaneVisible(adminLookupsPane, true);
                setPaneVisible(adminSettingsPane, false);
                setPaneVisible(adminHistoryPane, false);
                if (buttons.size() > 1) {
                    buttons.get(1).getStyleClass().add("active");
                }
            }
            case HISTORICAL -> {
                adminSubModuleTitle.setText("Historical Issuance");
                setPaneVisible(adminPersonnelPane, false);
                setPaneVisible(adminLookupsPane, false);
                setPaneVisible(adminSettingsPane, false);
                setPaneVisible(adminHistoryPane, true);
                refreshIssuances();
                if (buttons.size() > 2) {
                    buttons.get(2).getStyleClass().add("active");
                }
            }
            case SETTINGS -> {
                adminSubModuleTitle.setText("Settings");
                setPaneVisible(adminPersonnelPane, false);
                setPaneVisible(adminLookupsPane, false);
                setPaneVisible(adminSettingsPane, true);
                setPaneVisible(adminHistoryPane, false);
                if (buttons.size() > 3) {
                    buttons.get(3).getStyleClass().add("active");
                }
            }
        }
    }

    private void setModuleNavMessage(String text) {
        moduleNavLabel.setText(text);
        moduleNavBar.getChildren().setAll(moduleNavLabel);
    }

    private Button createSubNavButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-tab");
        button.setOnAction(event -> action.run());
        return button;
    }

    private List<Button> getSubNavButtons() {
        List<Button> buttons = new ArrayList<>();
        for (javafx.scene.Node node : moduleNavBar.getChildren()) {
            if (node instanceof Button button) {
                buttons.add(button);
            }
        }
        return buttons;
    }

    private void clearActive(List<Button> buttons) {
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }
    }

    private void applyTopTabStyles() {
        List<Button> buttons = List.of(topTabEquipment, topTabIssueReturn, topTabAdmin, topTabVehicle);
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }

        switch (activeMainTab) {
            case EQUIPMENT -> topTabEquipment.getStyleClass().add("active");
            case ISSUE_RETURN -> topTabIssueReturn.getStyleClass().add("active");
            case ADMIN -> topTabAdmin.getStyleClass().add("active");
            case VEHICLE -> topTabVehicle.getStyleClass().add("active");
        }
    }

    private void refreshLookupItems(LookupCategory category) {
        lookupEditorTitle.setText(category.getDisplayName());
        lookupItemList.setItems(FXCollections.observableArrayList(lookupDao.findAll(category)));
    }

    private void setPaneVisible(VBox pane, boolean visible) {
        pane.setVisible(visible);
        pane.setManaged(visible);
    }

    private OfficerFormData showOfficerDialog(Officer existing) {
        Dialog<OfficerFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Officer" : "Edit Officer");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

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
        dialog.setResultConverter(buttonType -> buttonType == saveButton
                ? new OfficerFormData(
                        text(nameField),
                        text(rankField),
                        text(badgeField)
                )
                : null);

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

    private UserFormData showUserDialog(User existing) {
        Dialog<UserFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Login" : "Edit Login");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField usernameField = new TextField(existing == null ? "" : existing.getUsername());
        TextField passwordField = new TextField();
        passwordField.setPromptText(existing == null ? "Required" : "Leave blank to keep current password");
        ComboBox<UserRole> roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        roleCombo.getSelectionModel().select(existing == null ? UserRole.OFFICER : existing.getRole());

        List<Officer> officers = officerDao.findAll();
        ComboBox<Officer> officerCombo = new ComboBox<>(FXCollections.observableArrayList(officers));
        if (existing != null && existing.getOfficerId() != null) {
            officerCombo.getSelectionModel().select(
                officers.stream()
                    .filter(officer -> officer.getOfficerId() == existing.getOfficerId())
                    .findFirst()
                    .orElse(null)
            );
        }

        roleCombo.valueProperty().addListener((obs, oldRole, newRole) -> {
            boolean officerRole = newRole == UserRole.OFFICER;
            officerCombo.setDisable(!officerRole);
            if (!officerRole) {
                officerCombo.getSelectionModel().clearSelection();
            }
        });
        boolean officerRole = roleCombo.getValue() == UserRole.OFFICER;
        officerCombo.setDisable(!officerRole);
        if (!officerRole) {
            officerCombo.getSelectionModel().clearSelection();
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Role"), 0, 2);
        grid.add(roleCombo, 1, 2);
        grid.add(new Label("Officer"), 0, 3);
        grid.add(officerCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> buttonType == saveButton
                ? new UserFormData(text(usernameField), text(passwordField), roleCombo.getValue(), officerCombo.getValue())
                : null);

        Optional<UserFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        UserFormData formData = result.get();
        if (formData.username().isEmpty() || formData.role() == null) {
            UiAlerts.error("Users", "Username and role are required.");
            return null;
        }
        if (existing == null && formData.password().isEmpty()) {
            UiAlerts.error("Users", "Password is required.");
            return null;
        }
        if (formData.role() == UserRole.OFFICER && formData.officer() == null) {
            UiAlerts.error("Users", "Officer role requires a linked officer.");
            return null;
        }
        return formData;
    }

    private EquipmentFormData showEquipmentDialog(EquipmentItem existing) {
        Dialog<EquipmentFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Equipment" : "Edit Equipment");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField nameField = new TextField(existing == null ? "" : existing.getName());
        TextField makeField = new TextField();
        TextField modelField = new TextField();
        TextField serialField = new TextField(existing == null ? "" : existing.getSerialNumber());
        TextField replacementCostField = new TextField(
            existing == null || existing.getReplacementCost() == null
                ? ""
                : String.format("%.2f", existing.getReplacementCost())
        );
        TextArea serialsArea = new TextArea();
        serialsArea.setPrefRowCount(4);
        serialsArea.setPromptText("One serial number per line");
        TextField quantityField = new TextField("1");
        quantityField.setPromptText("Quantity");
        TextField serialPrefixField = new TextField();
        serialPrefixField.setPromptText("Optional serial prefix (e.g. UNI-SHIRT)");

        ComboBox<EquipmentBranch> branchCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentBranch.values()));
        branchCombo.getSelectionModel().select(existing == null ? activeEquipmentBranch : existing.getBranch());

        ComboBox<LookupItem> equipmentTypeCombo = new ComboBox<>(
                FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.EQUIPMENT_TYPES)));
        ComboBox<LookupItem> uniformItemCombo = new ComboBox<>(
            FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.UNIFORM_ITEMS)));
        ComboBox<LookupItem> weaponTypeCombo = new ComboBox<>(
                FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.WEAPON_TYPES)));
        ComboBox<LookupItem> caliberCombo = new ComboBox<>(
                FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.CALIBERS)));
        ComboBox<LookupItem> sizeCombo = new ComboBox<>(
                FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.UNIFORM_SIZES)));
        ComboBox<LookupItem> storageCombo = new ComboBox<>(
                FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.STORAGE_LOCATIONS)));
        ComboBox<EquipmentCondition> conditionCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentCondition.values()));
        ComboBox<EquipmentStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentStatus.values()));
        CheckBox attachmentCheck = new CheckBox("Is attachment item");

        equipmentTypeCombo.setEditable(true);
        uniformItemCombo.setEditable(true);
        weaponTypeCombo.setEditable(true);
        caliberCombo.setEditable(true);
        sizeCombo.setEditable(true);
        storageCombo.setEditable(true);
        enableLookupTypeAhead(equipmentTypeCombo);
        enableLookupTypeAhead(uniformItemCombo);
        enableLookupTypeAhead(weaponTypeCombo);
        enableLookupTypeAhead(caliberCombo);
        enableLookupTypeAhead(sizeCombo);
        enableLookupTypeAhead(storageCombo);

        if (existing != null) {
            if (existing.getBranch() == EquipmentBranch.UNIFORM) {
                selectLookupById(uniformItemCombo, existing.getEquipmentTypeId());
            } else {
                selectLookupById(equipmentTypeCombo, existing.getEquipmentTypeId());
            }
            selectLookupById(weaponTypeCombo, existing.getWeaponTypeId());
            selectLookupById(caliberCombo, existing.getCaliberId());
            selectLookupById(sizeCombo, existing.getSizeId());
            selectLookupById(storageCombo, existing.getStorageLocationId());
            conditionCombo.getSelectionModel().select(existing.getCondition());
            statusCombo.getSelectionModel().select(existing.getStatus());
            attachmentCheck.setSelected(existing.isAttachment());
        } else {
            conditionCombo.getSelectionModel().select(EquipmentCondition.GOOD);
            statusCombo.getSelectionModel().select(EquipmentStatus.AVAILABLE);
            storageCombo.getSelectionModel().selectFirst();

            nameField.setEditable(false);
            nameField.setPromptText("Generated from make/model/category");

            Runnable refreshGeneratedName = () -> nameField.setText(buildGeneratedEquipmentName(
                    text(makeField),
                    text(modelField),
                    branchCombo.getValue(),
                        branchCombo.getValue() == EquipmentBranch.UNIFORM
                            ? selectedName(uniformItemCombo)
                            : selectedName(equipmentTypeCombo),
                    selectedName(weaponTypeCombo),
                    selectedName(caliberCombo),
                    selectedName(sizeCombo)
            ));

            makeField.textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            modelField.textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            branchCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            equipmentTypeCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            uniformItemCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            weaponTypeCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            caliberCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            sizeCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            equipmentTypeCombo.setOnAction(event -> refreshGeneratedName.run());
            uniformItemCombo.setOnAction(event -> refreshGeneratedName.run());
            weaponTypeCombo.setOnAction(event -> refreshGeneratedName.run());
            caliberCombo.setOnAction(event -> refreshGeneratedName.run());
            sizeCombo.setOnAction(event -> refreshGeneratedName.run());
        }

        Runnable applyBranchState = () -> {
            EquipmentBranch branch = branchCombo.getValue();
            boolean weapon = branch == EquipmentBranch.WEAPON;
            boolean uniform = branch == EquipmentBranch.UNIFORM;
            boolean equipment = branch == EquipmentBranch.EQUIPMENT;
            equipmentTypeCombo.setDisable(!equipment);
            uniformItemCombo.setDisable(!uniform);
            weaponTypeCombo.setDisable(!weapon);
            caliberCombo.setDisable(!weapon);
            sizeCombo.setDisable(!uniform);
            attachmentCheck.setDisable(branch != EquipmentBranch.EQUIPMENT);
            if (branch != EquipmentBranch.EQUIPMENT) {
                attachmentCheck.setSelected(false);
            }

            equipmentTypeCombo.setManaged(equipment);
            equipmentTypeCombo.setVisible(equipment);
            uniformItemCombo.setManaged(uniform);
            uniformItemCombo.setVisible(uniform);
            weaponTypeCombo.setManaged(weapon);
            weaponTypeCombo.setVisible(weapon);
            caliberCombo.setManaged(weapon);
            caliberCombo.setVisible(weapon);
            sizeCombo.setManaged(uniform);
            sizeCombo.setVisible(uniform);
            attachmentCheck.setManaged(equipment);
            attachmentCheck.setVisible(equipment);
        };
        branchCombo.valueProperty().addListener((obs, oldValue, branch) -> applyBranchState.run());
        applyBranchState.run();

        Label equipmentTypeLabel = new Label("Equipment Type");
        Label uniformItemLabel = new Label("Uniform Item");
        Label weaponTypeLabel = new Label("Weapon Type");
        Label caliberLabel = new Label("Caliber");
        Label sizeLabel = new Label("Uniform Size");
        Label serialNumbersLabel = new Label("Serial Numbers");
        Label quantityLabel = new Label("Quantity");
        Label serialPrefixLabel = new Label("Serial Prefix");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int row = 0;
        grid.add(new Label("Branch"), 0, row);
        grid.add(branchCombo, 1, row++);
        grid.add(new Label("Make"), 0, row);
        grid.add(makeField, 1, row++);
        grid.add(new Label("Model"), 0, row);
        grid.add(modelField, 1, row++);
        grid.add(new Label("Name"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(equipmentTypeLabel, 0, row);
        grid.add(equipmentTypeCombo, 1, row++);
        grid.add(uniformItemLabel, 0, row);
        grid.add(uniformItemCombo, 1, row++);
        grid.add(weaponTypeLabel, 0, row);
        grid.add(weaponTypeCombo, 1, row++);
        grid.add(caliberLabel, 0, row);
        grid.add(caliberCombo, 1, row++);
        grid.add(sizeLabel, 0, row);
        grid.add(sizeCombo, 1, row++);
        grid.add(new Label("Storage"), 0, row);
        grid.add(storageCombo, 1, row++);
        if (existing == null) {
            grid.add(serialNumbersLabel, 0, row);
            grid.add(serialsArea, 1, row++);
            grid.add(quantityLabel, 0, row);
            grid.add(quantityField, 1, row++);
            grid.add(serialPrefixLabel, 0, row);
            grid.add(serialPrefixField, 1, row++);
        } else {
            grid.add(new Label("Serial"), 0, row);
            grid.add(serialField, 1, row++);
        }
        grid.add(new Label("Replacement Cost"), 0, row);
        grid.add(replacementCostField, 1, row++);
        grid.add(new Label("Condition"), 0, row);
        grid.add(conditionCombo, 1, row++);
        grid.add(new Label("Status"), 0, row);
        grid.add(statusCombo, 1, row++);
        grid.add(attachmentCheck, 1, row);

        Runnable applyBranchLabels = () -> {
            EquipmentBranch branch = branchCombo.getValue();
            boolean weapon = branch == EquipmentBranch.WEAPON;
            boolean uniform = branch == EquipmentBranch.UNIFORM;
            boolean equipment = branch == EquipmentBranch.EQUIPMENT;

            equipmentTypeLabel.setManaged(equipment);
            equipmentTypeLabel.setVisible(equipment);
            uniformItemLabel.setManaged(uniform);
            uniformItemLabel.setVisible(uniform);
            weaponTypeLabel.setManaged(weapon);
            weaponTypeLabel.setVisible(weapon);
            caliberLabel.setManaged(weapon);
            caliberLabel.setVisible(weapon);
            sizeLabel.setManaged(uniform);
            sizeLabel.setVisible(uniform);

            boolean addMode = existing == null;
            serialNumbersLabel.setManaged(addMode && !uniform);
            serialNumbersLabel.setVisible(addMode && !uniform);
            quantityField.setManaged(addMode && uniform);
            quantityField.setVisible(addMode && uniform);
            quantityLabel.setManaged(addMode && uniform);
            quantityLabel.setVisible(addMode && uniform);
            serialPrefixField.setManaged(addMode && uniform);
            serialPrefixField.setVisible(addMode && uniform);
            serialPrefixLabel.setManaged(addMode && uniform);
            serialPrefixLabel.setVisible(addMode && uniform);
            serialsArea.setManaged(addMode && !uniform);
            serialsArea.setVisible(addMode && !uniform);
        };
        branchCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyBranchLabels.run());
        applyBranchLabels.run();

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }
                EquipmentBranch branch = branchCombo.getValue();
                Integer effectiveEquipmentTypeId = branch == EquipmentBranch.UNIFORM
                    ? selectedId(uniformItemCombo)
                    : selectedId(equipmentTypeCombo);
                String effectiveEquipmentTypeName = branch == EquipmentBranch.UNIFORM
                    ? selectedName(uniformItemCombo)
                    : selectedName(equipmentTypeCombo);

                    Double replacementCost = null;
                    String replacementCostText = text(replacementCostField);
                    if (!replacementCostText.isEmpty()) {
                    try {
                        replacementCost = Double.parseDouble(replacementCostText);
                            if (replacementCost < 0) {
                                UiAlerts.error("Equipment", "Replacement cost cannot be negative.");
                                return null;
                            }
                    } catch (NumberFormatException ex) {
                        UiAlerts.error("Equipment", "Replacement cost must be numeric.");
                        return null;
                    }
                    }

            Integer quantity = null;
            if (existing == null && branch == EquipmentBranch.UNIFORM) {
                String quantityText = text(quantityField);
                try {
                    quantity = Integer.parseInt(quantityText);
                    if (quantity <= 0) {
                        UiAlerts.error("Equipment", "Quantity must be greater than zero.");
                        return null;
                    }
                } catch (NumberFormatException ex) {
                    UiAlerts.error("Equipment", "Quantity must be a whole number.");
                    return null;
                }
            }
            return new EquipmentFormData(
                    text(nameField),
                    branch,
                    effectiveEquipmentTypeId, effectiveEquipmentTypeName,
                    selectedId(weaponTypeCombo), selectedName(weaponTypeCombo),
                    selectedId(caliberCombo), selectedName(caliberCombo),
                    selectedId(sizeCombo), selectedName(sizeCombo),
                    selectedId(storageCombo), selectedName(storageCombo),
                    text(serialField),
                    parseSerials(serialsArea),
                    quantity,
                    text(serialPrefixField),
                        replacementCost,
                    conditionCombo.getValue(),
                    statusCombo.getValue(),
                    attachmentCheck.isSelected()
            );
        });

        Optional<EquipmentFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        EquipmentFormData formData = result.get();
        if (formData.name().isEmpty() || formData.branch() == null || formData.condition() == null || formData.status() == null) {
            UiAlerts.error("Equipment", "Name, branch, condition, and status are required.");
            return null;
        }
        if (formData.replacementCost() == null) {
            UiAlerts.error("Equipment", "Replacement cost is required.");
            return null;
        }
        if (existing == null && formData.branch() != EquipmentBranch.UNIFORM && formData.serialNumbers().isEmpty()) {
            UiAlerts.error("Equipment", "Enter at least one serial number to add inventory in bulk.");
            return null;
        }
        if (existing == null && formData.branch() == EquipmentBranch.UNIFORM && (formData.quantity() == null || formData.quantity() <= 0)) {
            UiAlerts.error("Equipment", "Uniform quantity is required.");
            return null;
        }
        return formData;
    }

    private static String buildUniformSerial(String serialPrefix, int index) {
        String prefix = serialPrefix == null ? "" : serialPrefix.trim();
        if (prefix.isEmpty()) {
            return null;
        }
        return prefix + "-" + String.format("%03d", index);
    }

    private VehicleFormData showVehicleDialog(Vehicle existing) {
        Dialog<VehicleFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Vehicle" : "Edit Vehicle");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField unitField = new TextField(existing == null ? "" : existing.getUnitNumber());
        ComboBox<LookupItem> typeCombo = new ComboBox<>(FXCollections.observableArrayList(
                lookupDao.findAll(LookupCategory.VEHICLE_TYPES)
        ));
        typeCombo.setEditable(true);
        enableLookupTypeAhead(typeCombo);
        if (existing != null) {
            selectLookupById(typeCombo, existing.getVehicleTypeId());
        }
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
        grid.add(new Label("Vehicle Type"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Make"), 0, 2);
        grid.add(makeField, 1, 2);
        grid.add(new Label("Model"), 0, 3);
        grid.add(modelField, 1, 3);
        grid.add(new Label("Year"), 0, 4);
        grid.add(yearField, 1, 4);
        grid.add(new Label("VIN"), 0, 5);
        grid.add(vinField, 1, 5);
        grid.add(new Label("Plate"), 0, 6);
        grid.add(plateField, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }
            int year;
            try {
                year = Integer.parseInt(text(yearField));
            } catch (NumberFormatException ex) {
                UiAlerts.error("Vehicles", "Year must be a valid number.");
                return null;
            }
            return new VehicleFormData(
                    text(unitField),
                    selectedId(typeCombo),
                    text(makeField),
                    text(modelField),
                    year,
                    text(vinField),
                    text(plateField)
            );
        });

        Optional<VehicleFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        VehicleFormData formData = result.get();
        if (formData == null || formData.unitNumber().isEmpty() || formData.make().isEmpty() || formData.model().isEmpty()) {
            UiAlerts.error("Vehicles", "Unit number, make, and model are required.");
            return null;
        }
        return formData;
    }

    private MaintenanceDialogResult showMaintenanceDialog(Vehicle vehicle, VehicleMaintenanceLog existing) {
        Dialog<MaintenanceDialogResult> dialog = new Dialog<>();
        boolean editMode = existing != null;
        dialog.setTitle(editMode ? "Edit Maintenance Log" : "Add Maintenance Log");
        dialog.setHeaderText("Vehicle: " + vehicle.getUnitNumber());
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
        if (editMode) {
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, deleteButton, ButtonType.CANCEL);
        } else {
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        }

        DatePicker datePicker = new DatePicker(editMode ? existing.getLogDate() : LocalDate.now());
        TextField mileageField = new TextField();
        TextField costField = new TextField();
        TextField byField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(4);

        if (editMode) {
            if (existing.getMileage() != null) {
                mileageField.setText(String.valueOf(existing.getMileage()));
            }
            if (existing.getCost() != null) {
                costField.setText(String.valueOf(existing.getCost()));
            }
            byField.setText(existing.getPerformedBy() == null ? "" : existing.getPerformedBy());
            descriptionArea.setText(existing.getDescription() == null ? "" : existing.getDescription());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Date"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Mileage"), 0, 1);
        grid.add(mileageField, 1, 1);
        grid.add(new Label("Cost"), 0, 2);
        grid.add(costField, 1, 2);
        grid.add(new Label("Performed By"), 0, 3);
        grid.add(byField, 1, 3);
        grid.add(new Label("Description"), 0, 4);
        grid.add(descriptionArea, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (editMode && buttonType == deleteButton) {
                return new MaintenanceDialogResult(true, null);
            }
            if (buttonType != saveButton) {
                return null;
            }

            Integer mileage = null;
            String mileageText = text(mileageField);
            if (!mileageText.isEmpty()) {
                try {
                    mileage = Integer.parseInt(mileageText);
                } catch (NumberFormatException ex) {
                    UiAlerts.error("Maintenance", "Mileage must be a whole number.");
                    return null;
                }
            }

            Double cost = null;
            String costText = text(costField);
            if (!costText.isEmpty()) {
                try {
                    cost = Double.parseDouble(costText);
                } catch (NumberFormatException ex) {
                    UiAlerts.error("Maintenance", "Cost must be numeric.");
                    return null;
                }
            }

            return new MaintenanceDialogResult(
                    false,
                    new MaintenanceFormData(
                            datePicker.getValue(),
                            mileage,
                            cost,
                            text(descriptionArea),
                            text(byField)
                    )
            );
        });

        Optional<MaintenanceDialogResult> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        MaintenanceDialogResult dialogResult = result.get();
        if (dialogResult == null || dialogResult.deleteRequested()) {
            return dialogResult;
        }

        MaintenanceFormData formData = dialogResult.formData();
        if (formData == null || formData.logDate() == null || formData.description().isEmpty()) {
            UiAlerts.error("Maintenance", "Date and description are required.");
            return null;
        }
        return dialogResult;
    }

    private void selectLookupById(ComboBox<LookupItem> comboBox, Integer id) {
        if (id == null) {
            return;
        }
        comboBox.getSelectionModel().select(
                comboBox.getItems().stream().filter(item -> item.getId() == id).findFirst().orElse(null)
        );
    }

    private Integer selectedId(ComboBox<LookupItem> comboBox) {
        LookupItem item = selectedLookupItem(comboBox);
        return item == null ? null : item.getId();
    }

    private void enableLookupTypeAhead(ComboBox<LookupItem> comboBox) {
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            if (query.isEmpty()) {
                return;
            }
            comboBox.getItems().stream()
                    .filter(item -> item.getName().toLowerCase().startsWith(query))
                    .findFirst()
                    .ifPresent(match -> comboBox.getSelectionModel().select(match));
        });
    }

    private String selectedName(ComboBox<LookupItem> comboBox) {
        LookupItem item = selectedLookupItem(comboBox);
        return item == null ? null : item.getName();
    }

    private LookupItem selectedLookupItem(ComboBox<LookupItem> comboBox) {
        Object value = comboBox.getValue();
        if (value instanceof LookupItem lookupItem) {
            return lookupItem;
        }

        String editorText = comboBox.getEditor().getText();
        if (editorText == null || editorText.isBlank()) {
            return null;
        }

        String normalized = editorText.trim().toLowerCase();
        return comboBox.getItems().stream()
                .filter(item -> item.getName() != null)
                .filter(item -> item.getName().trim().equalsIgnoreCase(normalized)
                        || item.getName().trim().toLowerCase().startsWith(normalized))
                .findFirst()
                .orElse(null);
    }

    private static String text(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private static String text(TextArea textArea) {
        return textArea.getText() == null ? "" : textArea.getText().trim();
    }

    private static List<String> parseSerials(TextArea serialsArea) {
        if (serialsArea == null || serialsArea.getText() == null) {
            return List.of();
        }
        return serialsArea.getText().lines()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private static String buildGeneratedEquipmentName(String make,
                                                      String model,
                                                      EquipmentBranch branch,
                                                      String equipmentType,
                                                      String weaponType,
                                                      String caliber,
                                                      String size) {
        List<String> parts = new ArrayList<>();
        if (!make.isEmpty()) {
            parts.add(make);
        }
        if (!model.isEmpty()) {
            parts.add(model);
        }
        if (branch == null) {
            return String.join(" ", parts);
        }

        switch (branch) {
            case WEAPON -> {
                if (weaponType != null && !weaponType.isBlank()) {
                    parts.add(weaponType);
                }
                if (caliber != null && !caliber.isBlank()) {
                    parts.add(caliber);
                }
            }
            case UNIFORM -> {
                if (equipmentType != null && !equipmentType.isBlank()) {
                    parts.add(equipmentType);
                }
                if (size != null && !size.isBlank()) {
                    parts.add("Size " + size);
                }
            }
            case EQUIPMENT -> {
                if (equipmentType != null && !equipmentType.isBlank()) {
                    parts.add(equipmentType);
                }
            }
        }
        return String.join(" - ", parts);
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

    private record UserFormData(String username, String password, UserRole role, Officer officer) {
    }

    private record EquipmentFormData(String name,
                                     EquipmentBranch branch,
                                     Integer equipmentTypeId, String equipmentTypeName,
                                     Integer weaponTypeId, String weaponTypeName,
                                     Integer caliberId, String caliberName,
                                     Integer sizeId, String sizeName,
                                     Integer storageLocationId, String storageLocationName,
                                     String serialNumber,
                                     List<String> serialNumbers,
                                     Integer quantity,
                                     String serialPrefix,
                                     Double replacementCost,
                                     EquipmentCondition condition,
                                     EquipmentStatus status,
                                     boolean attachment) {
        private EquipmentItem toItem(int itemId, String serialOverride) {
            return new EquipmentItem(
                    itemId,
                    name,
                    branch,
                    equipmentTypeId, equipmentTypeName,
                    weaponTypeId, weaponTypeName,
                    caliberId, caliberName,
                    sizeId, sizeName,
                    storageLocationId, storageLocationName,
                    serialOverride,
                    replacementCost,
                    condition,
                    status,
                    attachment
            );
        }
    }

    private record VehicleFormData(String unitNumber, Integer vehicleTypeId, String make, String model,
                                   int year, String vin, String plate) {
    }

    private record MaintenanceFormData(LocalDate logDate, Integer mileage, Double cost,
                                       String description, String performedBy) {
    }

    private record MaintenanceDialogResult(boolean deleteRequested, MaintenanceFormData formData) {
    }
}
