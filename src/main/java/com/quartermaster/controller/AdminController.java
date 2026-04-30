package com.quartermaster.controller;

import com.quartermaster.Main;
import com.quartermaster.auth.SessionManager;
import com.quartermaster.auth.UserRole;
import com.quartermaster.dao.EquipmentDao;
import com.quartermaster.dao.IssuanceDao;
import com.quartermaster.dao.LookupDao;
import com.quartermaster.dao.OfficerDao;
import com.quartermaster.dao.UserDao;
import com.quartermaster.dao.VehicleDao;
import com.quartermaster.dao.VehicleMaintenanceLogDao;
import com.quartermaster.model.EquipmentBranch;
import com.quartermaster.model.EquipmentCondition;
import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.EquipmentStatus;
import com.quartermaster.model.IssuanceAdminRow;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
        SETTINGS
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
    private TableView<EquipmentItem> issueSelectedTable;

    @FXML
    private TableColumn<EquipmentItem, String> issueSelectedNameColumn;

    @FXML
    private TableColumn<EquipmentItem, String> issueSelectedCategoryColumn;

    @FXML
    private TableColumn<EquipmentItem, String> issueSelectedSerialColumn;

    @FXML
    private DatePicker issueDatePicker;

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

    private MainTab activeMainTab = MainTab.EQUIPMENT;
    private EquipmentBranch activeEquipmentBranch = EquipmentBranch.WEAPON;
    private final ObservableList<EquipmentItem> issueAvailableSource = FXCollections.observableArrayList();
    private final ObservableList<EquipmentItem> issueSelectedItems = FXCollections.observableArrayList();
    private final FilteredList<EquipmentItem> issueAvailableFiltered = new FilteredList<>(issueAvailableSource, item -> true);

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
        setModuleNavMessage("Assign and return equipment by officer.");
        refreshIssuances();
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
    public void onAddEquipment() {
        EquipmentFormData formData = showEquipmentDialog(null);
        if (formData == null) {
            return;
        }

        int created = 0;
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

        equipmentDao.delete(selected.getItemId());
        refreshEquipmentAndIssueOptions();
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
        LocalDate issueDate = issueDatePicker.getValue();

        if (officer == null || issueDate == null) {
            UiAlerts.error("Issue / Return", "Select officer and date.");
            return;
        }
        if (issueSelectedItems.isEmpty()) {
            UiAlerts.error("Issue / Return", "Select at least one item to issue.");
            return;
        }

        List<EquipmentItem> toIssue = new ArrayList<>(issueSelectedItems);
        for (EquipmentItem item : toIssue) {
            issuanceDao.issueItem(officer.getOfficerId(), item.getItemId(), issueDate);
        }

        refreshIssueOptions();
        refreshIssuances();
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onAddIssueSelection() {
        List<EquipmentItem> selected = new ArrayList<>(issueAvailableTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            EquipmentItem single = issueAvailableTable.getSelectionModel().getSelectedItem();
            if (single != null) {
                selected = List.of(single);
            }
        }
        for (EquipmentItem item : selected) {
            moveFromAvailableToSelected(item);
        }
    }

    @FXML
    public void onRemoveIssueSelection() {
        List<EquipmentItem> selected = new ArrayList<>(issueSelectedTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            EquipmentItem single = issueSelectedTable.getSelectionModel().getSelectedItem();
            if (single != null) {
                selected = List.of(single);
            }
        }
        for (EquipmentItem item : selected) {
            moveFromSelectedToAvailable(item);
        }
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

        issuanceDao.returnItemAndAttachments(selected.getIssuanceId());
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

        MaintenanceFormData formData = showMaintenanceDialog(selected);
        if (formData == null) {
            return;
        }

        maintenanceLogDao.insert(selected.getVehicleId(), formData.logDate(), formData.mileage(),
                formData.cost(), formData.description(), formData.performedBy());
        onMaintenanceVehicleChanged();
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
        UserFormData formData = showUserDialog();
        if (formData == null) {
            return;
        }

        String hash = BCrypt.hashpw(formData.password(), BCrypt.gensalt());
        userDao.createUser(formData.username(), hash, formData.role(),
                formData.officer() == null ? null : formData.officer().getOfficerId());
        refreshUsers();
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
        issueSelectedTable.setItems(issueSelectedItems);
        issueAvailableTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        issueSelectedTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        issueAvailableTable.setRowFactory(table -> {
            javafx.scene.control.TableRow<EquipmentItem> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    moveFromAvailableToSelected(row.getItem());
                }
            });
            return row;
        });

        issueSelectedTable.setRowFactory(table -> {
            javafx.scene.control.TableRow<EquipmentItem> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    moveFromSelectedToAvailable(row.getItem());
                }
            });
            return row;
        });

        issueSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyIssueAvailableFilter());
    }

    private void setupIssueSelectionTables() {
        issueAvailableNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        issueAvailableCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorySummary"));
        issueAvailableSerialColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            String serial = cellData.getValue().getSerialNumber();
            return serial == null ? "" : serial;
        }));

        issueSelectedNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
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
        issuanceDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getIssuedDate().toString()
        ));
        returnDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getReturnedDate() == null ? "" : cellData.getValue().getReturnedDate().toString()
        ));
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
    }

    private void refreshAll() {
        refreshOfficers();
        refreshUsers();
        refreshEquipmentAndIssueOptions();
        refreshIssueOptions();
        refreshIssuances();
        refreshVehiclesAndMaintenanceOptions();
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
        inventorySummaryLabel.setText("Showing " + filtered.size() + " of " + byBranch.size() + " items");

        boolean weaponView = activeEquipmentBranch == EquipmentBranch.WEAPON;
        attachItemButton.setVisible(weaponView);
        attachItemButton.setManaged(weaponView);
        removeAttachmentButton.setVisible(weaponView);
        removeAttachmentButton.setManaged(weaponView);

        refreshIssueOptions();
    }

    private void refreshIssueOptions() {
        List<EquipmentItem> available = equipmentDao.findAll().stream()
                .filter(item -> item.getStatus() == EquipmentStatus.AVAILABLE)
                .filter(item -> !item.isAttachment())
                .sorted(Comparator.comparing(EquipmentItem::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        List<Integer> selectedIds = issueSelectedItems.stream()
                .map(EquipmentItem::getItemId)
                .collect(Collectors.toList());

        List<EquipmentItem> stillSelected = available.stream()
                .filter(item -> selectedIds.contains(item.getItemId()))
                .collect(Collectors.toList());
        issueSelectedItems.setAll(stillSelected);

        List<EquipmentItem> source = available.stream()
                .filter(item -> issueSelectedItems.stream().noneMatch(sel -> sel.getItemId() == item.getItemId()))
                .collect(Collectors.toList());
        issueAvailableSource.setAll(source);
        applyIssueAvailableFilter();
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

    private void moveFromAvailableToSelected(EquipmentItem item) {
        if (item == null) {
            return;
        }
        issueAvailableSource.removeIf(existing -> existing.getItemId() == item.getItemId());
        boolean exists = issueSelectedItems.stream().anyMatch(existing -> existing.getItemId() == item.getItemId());
        if (!exists) {
            issueSelectedItems.add(item);
            issueSelectedItems.sort(Comparator.comparing(EquipmentItem::getName, String.CASE_INSENSITIVE_ORDER));
        }
    }

    private void moveFromSelectedToAvailable(EquipmentItem item) {
        if (item == null) {
            return;
        }
        issueSelectedItems.removeIf(existing -> existing.getItemId() == item.getItemId());
        boolean exists = issueAvailableSource.stream().anyMatch(existing -> existing.getItemId() == item.getItemId());
        if (!exists) {
            issueAvailableSource.add(item);
            issueAvailableSource.sort(Comparator.comparing(EquipmentItem::getName, String.CASE_INSENSITIVE_ORDER));
            applyIssueAvailableFilter();
        }
    }

    private void refreshIssuances() {
        issuanceTable.setItems(FXCollections.observableArrayList(issuanceDao.findAll()));
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
        Button settingsButton = createSubNavButton("Settings", () -> switchAdminSubTab(AdminSubTab.SETTINGS));

        moduleNavBar.getChildren().setAll(moduleNavLabel, personnelButton, lookupsButton, settingsButton);
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
                if (buttons.size() > 0) {
                    buttons.get(0).getStyleClass().add("active");
                }
            }
            case LOOKUPS -> {
                adminSubModuleTitle.setText("Lookups");
                setPaneVisible(adminPersonnelPane, false);
                setPaneVisible(adminLookupsPane, true);
                setPaneVisible(adminSettingsPane, false);
                if (buttons.size() > 1) {
                    buttons.get(1).getStyleClass().add("active");
                }
            }
            case SETTINGS -> {
                adminSubModuleTitle.setText("Settings");
                setPaneVisible(adminPersonnelPane, false);
                setPaneVisible(adminLookupsPane, false);
                setPaneVisible(adminSettingsPane, true);
                if (buttons.size() > 2) {
                    buttons.get(2).getStyleClass().add("active");
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

    private UserFormData showUserDialog() {
        Dialog<UserFormData> dialog = new Dialog<>();
        dialog.setTitle("Add Login");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        TextField passwordField = new TextField();
        ComboBox<UserRole> roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        roleCombo.getSelectionModel().select(UserRole.OFFICER);

        List<Officer> officers = officerDao.findAll();
        ComboBox<Officer> officerCombo = new ComboBox<>(FXCollections.observableArrayList(officers));

        roleCombo.valueProperty().addListener((obs, oldRole, newRole) -> {
            boolean officerRole = newRole == UserRole.OFFICER;
            officerCombo.setDisable(!officerRole);
            if (!officerRole) {
                officerCombo.getSelectionModel().clearSelection();
            }
        });

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
        if (formData.username().isEmpty() || formData.password().isEmpty() || formData.role() == null) {
            UiAlerts.error("Users", "Username, password, and role are required.");
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
        TextArea serialsArea = new TextArea();
        serialsArea.setPrefRowCount(4);
        serialsArea.setPromptText("One serial number per line");

        ComboBox<EquipmentBranch> branchCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentBranch.values()));
        branchCombo.getSelectionModel().select(existing == null ? activeEquipmentBranch : existing.getBranch());

        ComboBox<LookupItem> equipmentTypeCombo = new ComboBox<>(
                FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.EQUIPMENT_TYPES)));
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
        weaponTypeCombo.setEditable(true);
        caliberCombo.setEditable(true);
        sizeCombo.setEditable(true);
        storageCombo.setEditable(true);
        enableLookupTypeAhead(equipmentTypeCombo);
        enableLookupTypeAhead(weaponTypeCombo);
        enableLookupTypeAhead(caliberCombo);
        enableLookupTypeAhead(sizeCombo);
        enableLookupTypeAhead(storageCombo);

        if (existing != null) {
            selectLookupById(equipmentTypeCombo, existing.getEquipmentTypeId());
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
                    selectedName(equipmentTypeCombo),
                    selectedName(weaponTypeCombo),
                    selectedName(caliberCombo),
                    selectedName(sizeCombo)
            ));

            makeField.textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            modelField.textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            branchCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            equipmentTypeCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            weaponTypeCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            caliberCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
            sizeCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedName.run());
        }

        Runnable applyBranchState = () -> {
            EquipmentBranch branch = branchCombo.getValue();
            boolean weapon = branch == EquipmentBranch.WEAPON;
            boolean uniform = branch == EquipmentBranch.UNIFORM;
            boolean equipment = branch == EquipmentBranch.EQUIPMENT;
            equipmentTypeCombo.setDisable(!equipment && !uniform);
            weaponTypeCombo.setDisable(!weapon);
            caliberCombo.setDisable(!weapon);
            sizeCombo.setDisable(!uniform);
            attachmentCheck.setDisable(branch != EquipmentBranch.EQUIPMENT);
            if (branch != EquipmentBranch.EQUIPMENT) {
                attachmentCheck.setSelected(false);
            }
        };
        branchCombo.valueProperty().addListener((obs, oldValue, branch) -> applyBranchState.run());
        applyBranchState.run();

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
        grid.add(new Label("Equipment Type"), 0, row);
        grid.add(equipmentTypeCombo, 1, row++);
        grid.add(new Label("Weapon Type"), 0, row);
        grid.add(weaponTypeCombo, 1, row++);
        grid.add(new Label("Caliber"), 0, row);
        grid.add(caliberCombo, 1, row++);
        grid.add(new Label("Uniform Size"), 0, row);
        grid.add(sizeCombo, 1, row++);
        grid.add(new Label("Storage"), 0, row);
        grid.add(storageCombo, 1, row++);
        if (existing == null) {
            grid.add(new Label("Serial Numbers"), 0, row);
            grid.add(serialsArea, 1, row++);
        } else {
            grid.add(new Label("Serial"), 0, row);
            grid.add(serialField, 1, row++);
        }
        grid.add(new Label("Condition"), 0, row);
        grid.add(conditionCombo, 1, row++);
        grid.add(new Label("Status"), 0, row);
        grid.add(statusCombo, 1, row++);
        grid.add(attachmentCheck, 1, row);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }
            return new EquipmentFormData(
                    text(nameField),
                    branchCombo.getValue(),
                    selectedId(equipmentTypeCombo), selectedName(equipmentTypeCombo),
                    selectedId(weaponTypeCombo), selectedName(weaponTypeCombo),
                    selectedId(caliberCombo), selectedName(caliberCombo),
                    selectedId(sizeCombo), selectedName(sizeCombo),
                    selectedId(storageCombo), selectedName(storageCombo),
                    text(serialField),
                    parseSerials(serialsArea),
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
        if (existing == null && formData.serialNumbers().isEmpty()) {
            UiAlerts.error("Equipment", "Enter at least one serial number to add inventory in bulk.");
            return null;
        }
        return formData;
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

    private MaintenanceFormData showMaintenanceDialog(Vehicle vehicle) {
        Dialog<MaintenanceFormData> dialog = new Dialog<>();
        dialog.setTitle("Add Maintenance Log");
        dialog.setHeaderText("Vehicle: " + vehicle.getUnitNumber());
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField mileageField = new TextField();
        TextField costField = new TextField();
        TextField byField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(4);

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

            return new MaintenanceFormData(
                    datePicker.getValue(),
                    mileage,
                    cost,
                    text(descriptionArea),
                    text(byField)
            );
        });

        Optional<MaintenanceFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        MaintenanceFormData formData = result.get();
        if (formData == null || formData.logDate() == null || formData.description().isEmpty()) {
            UiAlerts.error("Maintenance", "Date and description are required.");
            return null;
        }
        return formData;
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
        LookupItem item = comboBox.getSelectionModel().getSelectedItem();
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
        LookupItem item = comboBox.getSelectionModel().getSelectedItem();
        return item == null ? null : item.getName();
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
}
