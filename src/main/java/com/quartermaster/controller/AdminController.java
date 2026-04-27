package com.quartermaster.controller;

import com.quartermaster.Main;
import com.quartermaster.auth.SessionManager;
import com.quartermaster.auth.UserRole;
import com.quartermaster.dao.EquipmentDao;
import com.quartermaster.dao.EquipmentCategoryDao;
import com.quartermaster.dao.IssuanceDao;
import com.quartermaster.dao.OfficerDao;
import com.quartermaster.dao.UserDao;
import com.quartermaster.dao.VehicleDao;
import com.quartermaster.dao.VehicleMaintenanceLogDao;
import com.quartermaster.model.EquipmentCategory;
import com.quartermaster.model.EquipmentCondition;
import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.EquipmentStatus;
import com.quartermaster.model.IssuanceAdminRow;
import com.quartermaster.model.Officer;
import com.quartermaster.model.User;
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
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminController {
    private static final String MODULE_WEAPONS = "Weapons";
    private static final String MODULE_EQUIPMENT = "Equipment";
    private static final String MODULE_UNIFORMS = "Uniforms";
    private static final String MODULE_VEHICLES = "Vehicles";
    private static final String MODULE_ADMIN_SETTINGS = "Admin Settings";

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<String> moduleNavList;

    @FXML
    private VBox moduleSettingsPane;

    @FXML
    private VBox moduleInventoryPane;

    @FXML
    private VBox moduleFleetPane;

    @FXML
    private Label detailTitleLabel;

    @FXML
    private Label detailLine1Label;

    @FXML
    private Label detailLine2Label;

    @FXML
    private Label detailLine3Label;

    @FXML
    private Label detailLine4Label;

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
    private TableView<EquipmentItem> equipmentTable;

    @FXML
    private TableView<EquipmentCategory> categoryTable;

    @FXML
    private TableColumn<EquipmentCategory, String> categoryPathColumn;

    @FXML
    private TableColumn<EquipmentCategory, String> categoryTypeColumn;

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
    private final UserDao userDao = new UserDao();
    private final EquipmentCategoryDao equipmentCategoryDao = new EquipmentCategoryDao();
    private final EquipmentDao equipmentDao = new EquipmentDao();
    private final IssuanceDao issuanceDao = new IssuanceDao();
    private final VehicleDao vehicleDao = new VehicleDao();
    private final VehicleMaintenanceLogDao maintenanceLogDao = new VehicleMaintenanceLogDao();
    private String currentInventoryBranchKey = "WEAPON";

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null || SessionManager.getCurrentUser().getRole() != UserRole.ADMIN) {
            forceLogoutToLogin();
            return;
        }

        welcomeLabel.setText("Admin: " + SessionManager.getCurrentUser().getUsername());

        setupOfficerTable();
        setupUsersTable();
        setupCategoryTable();
        setupEquipmentTable();
        setupIssuanceTable();
        setupVehicleTable();
        setupMaintenanceTable();
        setupNavigation();
        setupDetailPanel();

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
    public void onOpenAdminSettings() {
        showModule(MODULE_ADMIN_SETTINGS);
    }

    @FXML
    public void onAddOfficer() {
        OfficerFormData formData = showOfficerDialog(null);
        if (formData == null) {
            return;
        }

        officerDao.insert(formData.name(), formData.rank(), formData.badgeNumber());
        refreshOfficers();
        refreshUsers();
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
        refreshUsers();
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
        refreshUsers();
    }

    @FXML
    public void onAddLoginPersonnel() {
        Dialog<UserFormData> dialog = new Dialog<>();
        dialog.setTitle("Add Login Personnel");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<UserRole> roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        ComboBox<Officer> officerCombo = new ComboBox<>(FXCollections.observableArrayList(officerDao.findAll()));

        roleCombo.getSelectionModel().select(UserRole.OFFICER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Role"), 0, 2);
        grid.add(roleCombo, 1, 2);
        grid.add(new Label("Linked Officer"), 0, 3);
        grid.add(officerCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new UserFormData(
                        usernameField.getText() == null ? "" : usernameField.getText().trim(),
                        passwordField.getText() == null ? "" : passwordField.getText().trim(),
                        roleCombo.getSelectionModel().getSelectedItem(),
                        officerCombo.getSelectionModel().getSelectedItem()
                );
            }
            return null;
        });

        Optional<UserFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        UserFormData formData = result.get();
        if (formData.username().isEmpty() || formData.password().isEmpty() || formData.role() == null) {
            UiAlerts.error("Login Personnel", "Username, password, and role are required.");
            return;
        }
        if (formData.role() == UserRole.OFFICER && formData.officer() == null) {
            UiAlerts.error("Login Personnel", "Officer role must be linked to an officer record.");
            return;
        }

        Integer officerId = formData.officer() == null ? null : formData.officer().getOfficerId();
        String passwordHash = BCrypt.hashpw(formData.password(), BCrypt.gensalt());
        userDao.createUser(formData.username(), passwordHash, formData.role(), officerId);
        refreshUsers();
    }

    @FXML
    public void onDeleteLoginPersonnel() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Login Personnel", "Select a user to delete.");
            return;
        }

        if ("admin".equalsIgnoreCase(selected.getUsername())) {
            UiAlerts.error("Login Personnel", "Default admin account cannot be deleted.");
            return;
        }

        userDao.deleteUser(selected.getUserId());
        refreshUsers();
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
    public void onAddSubcategory() {
        EquipmentCategory selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Categories", "Select a parent category first.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Subcategory");
        dialog.setHeaderText("Parent: " + selected.getCategoryPath());
        UiAlerts.applyTheme(dialog);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Subcategory Name"), 0, 0);
        grid.add(nameField, 1, 0);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> buttonType == saveButtonType
                ? (nameField.getText() == null ? "" : nameField.getText().trim())
                : null);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        if (result.get().isEmpty()) {
            UiAlerts.error("Categories", "Subcategory name is required.");
            return;
        }

        equipmentCategoryDao.createSubcategory(selected.getCategoryId(), result.get());
        refreshCategories();
    }

    @FXML
    public void onDeleteCategory() {
        EquipmentCategory selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Categories", "Select a category to remove.");
            return;
        }

        try {
            equipmentCategoryDao.deleteCategory(selected.getCategoryId());
            refreshCategories();
        } catch (IllegalStateException ex) {
            UiAlerts.error("Categories", ex.getMessage());
        }
    }

    @FXML
    public void onAttachItemToWeapon() {
        EquipmentItem selectedWeapon = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedWeapon == null || !selectedWeapon.getCategory().isWeapon()) {
            UiAlerts.error("Weapon Attachments", "Select a weapon first.");
            return;
        }

        List<EquipmentItem> attachmentCandidates = equipmentDao.findAll().stream()
                .filter(i -> i.getCategory().isWeaponAttachment())
                .collect(Collectors.toList());

        if (attachmentCandidates.isEmpty()) {
            UiAlerts.error("Weapon Attachments", "No attachment items found.");
            return;
        }

        ChoiceDialog<EquipmentItem> dialog = new ChoiceDialog<>(attachmentCandidates.get(0), attachmentCandidates);
        dialog.setTitle("Attach Item");
        dialog.setHeaderText("Attach item to weapon: " + selectedWeapon.getName());
        dialog.setContentText("Attachment:");
        UiAlerts.applyTheme(dialog);

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
        if (selectedWeapon == null || !selectedWeapon.getCategory().isWeapon()) {
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
        UiAlerts.applyTheme(dialog);

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
        UiAlerts.applyTheme(dialog);

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

        officersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            setDetail(
                    "Officer",
                    "ID: " + selected.getOfficerId(),
                    "Name: " + selected.getName(),
                    "Rank: " + selected.getRank(),
                    "Badge: " + selected.getBadgeNumber()
            );
        });
    }

    private void setupUsersTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getRole().name()
        ));
        linkedOfficerColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getOfficerId() == null ? "" : String.valueOf(cellData.getValue().getOfficerId())
        ));

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            setDetail(
                    "Login Personnel",
                    "User ID: " + selected.getUserId(),
                    "Username: " + selected.getUsername(),
                    "Role: " + selected.getRole().name(),
                    "Linked Officer ID: " + (selected.getOfficerId() == null ? "None" : selected.getOfficerId())
            );
        });
    }

    private void setupCategoryTable() {
        categoryPathColumn.setCellValueFactory(new PropertyValueFactory<>("categoryPath"));
        categoryTypeColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> {
            if (cellData.getValue().isWeaponAttachment()) {
                return "Weapon Attachment";
            }
            if (cellData.getValue().isWeapon()) {
                return "Weapons";
            }
            return cellData.getValue().isRootCategory() ? "Main" : "Subcategory";
        }));

        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            setDetail(
                    "Category",
                    "Path: " + selected.getCategoryPath(),
                    "Type: " + (selected.isRootCategory() ? "Main Category" : "Subcategory"),
                    "Branch: " + selected.getBranchKey(),
                    selected.isSystemCategory() ? "Protected system category" : "User-managed category"
            );
        });
    }

    private void setupEquipmentTable() {
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        equipmentCategoryColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
            () -> cellData.getValue().getCategory().getCategoryPath()
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
                setText(empty || item == null ? null : item.getName() + " (" + item.getCategory().getCategoryPath() + ")");
            }
        });
        issueItemCombo.setButtonCell(issueItemCombo.getCellFactory().call(null));

        equipmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            setDetail(
                    "Equipment Item",
                    "Name: " + selected.getName(),
                    "Category: " + selected.getCategory().getCategoryPath(),
                    "Serial: " + (selected.getSerialNumber() == null ? "" : selected.getSerialNumber()),
                    "Status: " + selected.getStatus().name()
            );
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

        issueOfficerCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Officer officer, boolean empty) {
                super.updateItem(officer, empty);
                setText(empty || officer == null ? null : officer.getName() + " (" + officer.getBadgeNumber() + ")");
            }
        });
        issueOfficerCombo.setButtonCell(issueOfficerCombo.getCellFactory().call(null));

        issuanceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            setDetail(
                    "Issuance",
                    "Officer: " + selected.getOfficerName(),
                    "Item: " + selected.getItemName(),
                    "Issued: " + selected.getIssuedDate(),
                    "Returned: " + (selected.getReturnedDate() == null ? "Active" : selected.getReturnedDate().toString())
            );
        });
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

        vehiclesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            setDetail(
                    "Vehicle",
                    "Unit: " + selected.getUnitNumber(),
                    "Model: " + selected.getMake() + " " + selected.getModel(),
                    "Year: " + selected.getYear(),
                    "Plate: " + (selected.getPlateNumber() == null ? "" : selected.getPlateNumber())
            );
        });
    }

    private void setupMaintenanceTable() {
        maintenanceDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getLogDate().toString()
        ));
        maintenanceMileageColumn.setCellValueFactory(new PropertyValueFactory<>("mileage"));
        maintenanceDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        maintenanceByColumn.setCellValueFactory(new PropertyValueFactory<>("performedBy"));

        maintenanceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            setDetail(
                    "Maintenance Log",
                    "Date: " + selected.getLogDate(),
                    "Mileage: " + (selected.getMileage() == null ? "" : selected.getMileage()),
                    "Performed By: " + (selected.getPerformedBy() == null ? "" : selected.getPerformedBy()),
                    "Description: " + selected.getDescription()
            );
        });
    }

    private void setupNavigation() {
        moduleNavList.setItems(FXCollections.observableArrayList(
                MODULE_WEAPONS,
                MODULE_EQUIPMENT,
                MODULE_UNIFORMS,
                MODULE_VEHICLES
        ));

        moduleNavList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            showModule(selected);
        });

        moduleNavList.getSelectionModel().selectFirst();
    }

    private void setupDetailPanel() {
        setDetail(
                "Record Details",
                "Select a row in any module.",
                "Details appear here.",
                "",
                ""
        );
    }

    private void showModule(String moduleName) {
        hideAllModules();
        switch (moduleName) {
            case MODULE_WEAPONS -> showInventoryModule("WEAPON");
            case MODULE_EQUIPMENT -> showInventoryModule("EQUIPMENT");
            case MODULE_UNIFORMS -> showInventoryModule("UNIFORM");
            case MODULE_VEHICLES -> setPaneVisible(moduleFleetPane, true);
            case MODULE_ADMIN_SETTINGS -> setPaneVisible(moduleSettingsPane, true);
            default -> showInventoryModule("WEAPON");
        }
    }

    private void hideAllModules() {
        setPaneVisible(moduleSettingsPane, false);
        setPaneVisible(moduleInventoryPane, false);
        setPaneVisible(moduleFleetPane, false);
    }

    private void setPaneVisible(VBox pane, boolean visible) {
        if (pane == null) {
            return;
        }
        pane.setVisible(visible);
        pane.setManaged(visible);
    }

    private void showInventoryModule(String branchKey) {
        currentInventoryBranchKey = branchKey;
        setPaneVisible(moduleInventoryPane, true);
        refreshEquipmentAndIssueOptions();
        refreshIssuances();
    }

    private void setDetail(String title, String line1, String line2, String line3, String line4) {
        detailTitleLabel.setText(title == null ? "" : title);
        detailLine1Label.setText(line1 == null ? "" : line1);
        detailLine2Label.setText(line2 == null ? "" : line2);
        detailLine3Label.setText(line3 == null ? "" : line3);
        detailLine4Label.setText(line4 == null ? "" : line4);
    }

    private void refreshAll() {
        refreshOfficers();
        refreshUsers();
        refreshCategories();
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

    private void refreshUsers() {
        usersTable.setItems(FXCollections.observableArrayList(userDao.findAll()));
    }

    private void refreshCategories() {
        categoryTable.setItems(FXCollections.observableArrayList(equipmentCategoryDao.findAll()));
    }

    private void refreshEquipmentAndIssueOptions() {
        List<EquipmentItem> equipment = equipmentDao.findAll().stream()
            .filter(this::isInActiveInventoryBranch)
            .collect(Collectors.toList());
        equipmentTable.setItems(FXCollections.observableArrayList(equipment));
        List<EquipmentItem> availableItems = equipmentDao.findAvailable().stream()
            .filter(this::isInActiveInventoryBranch)
            .collect(Collectors.toList());
        issueItemCombo.setItems(FXCollections.observableArrayList(availableItems));
    }

    private void refreshIssuances() {
        Set<Integer> visibleItemIds = equipmentDao.findAll().stream()
            .filter(this::isInActiveInventoryBranch)
            .map(EquipmentItem::getItemId)
            .collect(Collectors.toSet());

        issuanceTable.setItems(FXCollections.observableArrayList(
            issuanceDao.findAll().stream()
                .filter(row -> visibleItemIds.contains(row.getItemId()))
                .collect(Collectors.toList())
        ));
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
        UiAlerts.applyTheme(dialog);

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
        UiAlerts.applyTheme(dialog);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(existing == null ? "" : existing.getName());
        TextField serialField = new TextField(existing == null ? "" : existing.getSerialNumber());
        List<EquipmentCategory> filteredCategories = equipmentCategoryDao.findAll().stream()
                .filter(this::isInActiveInventoryBranch)
                .collect(Collectors.toList());
        ComboBox<EquipmentCategory> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(filteredCategories));
        ComboBox<EquipmentCondition> conditionCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentCondition.values()));
        ComboBox<EquipmentStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentStatus.values()));

        if (existing == null) {
            categoryCombo.getSelectionModel().select(
                filteredCategories.stream()
                    .filter(EquipmentCategory::isRootCategory)
                    .findFirst()
                    .or(() -> filteredCategories.stream().findFirst())
                    .orElse(null)
            );
        } else {
            categoryCombo.getSelectionModel().select(
                filteredCategories.stream()
                    .filter(category -> category.getCategoryId() == existing.getCategory().getCategoryId())
                    .findFirst()
                    .orElse(null)
            );
        }
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

    private boolean isInActiveInventoryBranch(EquipmentItem item) {
        return item != null
                && item.getCategory() != null
                && currentInventoryBranchKey.equals(item.getCategory().getBranchKey());
    }

    private boolean isInActiveInventoryBranch(EquipmentCategory category) {
        return category != null && currentInventoryBranchKey.equals(category.getBranchKey());
    }

    private VehicleFormData showVehicleDialog(Vehicle existing) {
        Dialog<VehicleFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Vehicle" : "Edit Vehicle");
        UiAlerts.applyTheme(dialog);

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

    private record UserFormData(String username, String password, UserRole role, Officer officer) {
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
