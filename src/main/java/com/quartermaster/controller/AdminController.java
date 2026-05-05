package com.quartermaster.controller;

import com.quartermaster.Main;
import com.quartermaster.auth.SessionManager;
import com.quartermaster.auth.UserRole;
import com.quartermaster.dao.AmmoIssuanceDao;
import com.quartermaster.dao.AmmoTypeDefinitionDao;
import com.quartermaster.dao.AmmunitionDao;
import com.quartermaster.dao.EquipmentDao;
import com.quartermaster.dao.EquipmentDao.UniformCategory;
import com.quartermaster.dao.IssuanceDao;
import com.quartermaster.dao.LookupDao;
import com.quartermaster.dao.OfficerDao;
import com.quartermaster.dao.SettingsDao;
import com.quartermaster.dao.UniformIssuanceDao;
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
import javafx.util.StringConverter;
import javafx.stage.Window;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminController {

    private enum MainTab {
        EQUIPMENT,
        UNIFORM,
        AMMUNITION,
        ISSUE_RETURN,
        ADMIN,
        VEHICLE
    }

    private enum UniformSubTab {
        STOCK,
        ISSUE
    }

    private enum AmmoSubTab {
        TYPES,
        STOCK,
        ISSUE
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
    private Button topTabUniform;

    @FXML
    private Button topTabAmmunition;

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
    private VBox moduleUniformPane;

    @FXML
    private Label uniformModuleTitleLabel;

    @FXML
    private VBox uniformStockPane;

    @FXML
    private VBox uniformIssuePane;

    @FXML
    private TableView<EquipmentDao.UniformStockRecord> uniformStockTable;

    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformItemColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformCategoryColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformBrandColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformModelColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformSizeColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformQtyColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformUnitCostColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformSupplierColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformStorageColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformConditionColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformStatusColumn;

    @FXML
    private ComboBox<Officer> uniformIssueOfficerCombo;
    @FXML
    private TextField uniformIssueQtyField;
    @FXML
    private TableView<EquipmentDao.UniformStockRecord> uniformIssueAvailableTable;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformIssueItemColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformIssueCategoryColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformIssueBrandColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformIssueModelColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformIssueSizeColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformIssueQtyColumn;
    @FXML
    private TableColumn<EquipmentDao.UniformStockRecord, String> uniformIssueStorageColumn;

    @FXML
    private Label uniformIssuedToOfficerLabel;
    @FXML
    private TableView<UniformIssuanceDao.UniformIssuanceRow> uniformIssuedTable;
    @FXML
    private TableColumn<UniformIssuanceDao.UniformIssuanceRow, String> uniformIssuedItemColumn;
    @FXML
    private TableColumn<UniformIssuanceDao.UniformIssuanceRow, String> uniformIssuedSizeColumn;
    @FXML
    private TableColumn<UniformIssuanceDao.UniformIssuanceRow, String> uniformIssuedQtyColumn;
    @FXML
    private TableColumn<UniformIssuanceDao.UniformIssuanceRow, String> uniformIssuedCostColumn;
    @FXML
    private TableColumn<UniformIssuanceDao.UniformIssuanceRow, String> uniformIssuedDateColumn;
    @FXML
    private TableColumn<UniformIssuanceDao.UniformIssuanceRow, String> uniformIssuedByColumn;

    // ===== Ammunition module =====
    @FXML private VBox moduleAmmunitionPane;
    @FXML private Label ammoModuleTitleLabel;
    @FXML private VBox ammoTypesPane;
    @FXML private VBox ammoStockPane;
    @FXML private VBox ammoIssuePane;

    @FXML private TableView<AmmoTypeDefinitionDao.AmmoTypeDefRecord> ammoTypesTable;
    @FXML private TableColumn<AmmoTypeDefinitionDao.AmmoTypeDefRecord, String> ammoTypeDefCaliberColumn;
    @FXML private TableColumn<AmmoTypeDefinitionDao.AmmoTypeDefRecord, String> ammoTypeDefTypeColumn;
    @FXML private TableColumn<AmmoTypeDefinitionDao.AmmoTypeDefRecord, String> ammoTypeDefUseColumn;
    @FXML private TableColumn<AmmoTypeDefinitionDao.AmmoTypeDefRecord, String> ammoTypeDefCostColumn;
    @FXML private TableColumn<AmmoTypeDefinitionDao.AmmoTypeDefRecord, String> ammoTypeDefRoundsColumn;

    @FXML private TableView<AmmunitionDao.AmmoStockRecord> ammoStockTable;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoTypeColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoCaliberColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoUseColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoRoundsColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoUnitCostColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoStorageColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoNotesColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoStatusColumn;

    @FXML private ComboBox<Officer> ammoIssueOfficerCombo;
    @FXML private ComboBox<LookupItem> ammoIssueReasonCombo;
    @FXML private CheckBox ammoShowHistoryCheckBox;
    @FXML private TextField ammoIssueRoundsField;
    @FXML private TableView<AmmunitionDao.AmmoStockRecord> ammoIssueAvailableTable;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoIssueTypeColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoIssueCaliberColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoIssueUseColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoIssueRoundsColumn;
    @FXML private TableColumn<AmmunitionDao.AmmoStockRecord, String> ammoIssueStorageColumn;

    @FXML private Label ammoIssuedToOfficerLabel;
    @FXML private TableView<AmmoIssuanceDao.AmmoIssuanceRow> ammoIssuedTable;
    @FXML private TableColumn<AmmoIssuanceDao.AmmoIssuanceRow, String> ammoIssuedTypeColumn;
    @FXML private TableColumn<AmmoIssuanceDao.AmmoIssuanceRow, String> ammoIssuedCaliberColumn;
    @FXML private TableColumn<AmmoIssuanceDao.AmmoIssuanceRow, String> ammoIssuedRoundsColumn;
    @FXML private TableColumn<AmmoIssuanceDao.AmmoIssuanceRow, String> ammoIssuedReasonColumn;
    @FXML private TableColumn<AmmoIssuanceDao.AmmoIssuanceRow, String> ammoIssuedDateColumn;
    @FXML private TableColumn<AmmoIssuanceDao.AmmoIssuanceRow, String> ammoIssuedStatusColumn;

    @FXML
    private Label inventoryModuleTitleLabel;

    @FXML
    private TextField inventorySearchField;

    @FXML
    private ComboBox<String> inventoryStatusFilterCombo;

    @FXML
    private CheckBox inventoryHistoricalItemsCheckBox;

    @FXML
    private Label inventorySummaryLabel;

    @FXML
    private HBox inventoryHeaderRow;

    @FXML
    private VBox inventoryFiltersCard;

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
    private VBox inventoryListCard;

    @FXML
    private VBox inventoryAddPagePane;

    @FXML
    private ComboBox<EquipmentBranch> addEqBranchCombo;

    @FXML
    private TextField addEqMakeField;

    @FXML
    private TextField addEqModelField;

    @FXML
    private TextField addEqNameField;

    @FXML
    private ComboBox<LookupItem> addEqTypeCombo;

    @FXML
    private HBox addEqCategoryRow;

    @FXML
    private ComboBox<LookupItem> addEqWeaponTypeCombo;

    @FXML
    private HBox addEqWeaponRow;

    @FXML
    private ComboBox<LookupItem> addEqCaliberCombo;

    @FXML
    private ComboBox<LookupItem> addEqSizeCombo;

    @FXML
    private HBox addEqUniformSizeRow;

    @FXML
    private ComboBox<LookupItem> addEqStorageCombo;

    @FXML
    private TextField addEqReplacementCostField;

    @FXML
    private ComboBox<EquipmentCondition> addEqConditionCombo;

    @FXML
    private ComboBox<EquipmentStatus> addEqStatusCombo;

    @FXML
    private CheckBox addEqAttachmentCheck;

    @FXML
    private HBox addEqAttachmentRow;

    @FXML
    private TextArea addEqSerialsArea;

    @FXML
    private VBox addEqSerialsSection;

    @FXML
    private TextField addEqQuantityField;

    @FXML
    private TextField addEqSerialPrefixField;

    @FXML
    private HBox addEqUniformSection;

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
    private final UniformIssuanceDao uniformIssuanceDao = new UniformIssuanceDao();
    private final AmmunitionDao ammunitionDao = new AmmunitionDao();
    private final AmmoIssuanceDao ammoIssuanceDao = new AmmoIssuanceDao();
    private final AmmoTypeDefinitionDao ammoTypeDefinitionDao = new AmmoTypeDefinitionDao();
    private final java.util.Map<Integer, PendingUniformIssue> pendingUniformIssuesByTempId = new java.util.LinkedHashMap<>();
    private final java.util.Set<Integer> pendingUniformReturnIds = new java.util.LinkedHashSet<>();
    private int nextPendingUniformIssueTempId = -1;
    private final java.util.Map<Integer, PendingAmmoIssue> pendingAmmoIssuesByTempId = new java.util.LinkedHashMap<>();
    private final java.util.Set<Integer> pendingAmmoReturnIds = new java.util.LinkedHashSet<>();
    private final java.util.Set<Integer> pendingAmmoExpendedIds = new java.util.LinkedHashSet<>();
    private int nextPendingAmmoIssueTempId = -1;
    private boolean suppressAmmoOfficerSelectionListener = false;

    private record PendingUniformIssue(int tempId,
                                       int uniformId,
                                       String itemName,
                                       String category,
                                       String brand,
                                       String model,
                                       String sizeName,
                                       int quantity,
                                       Double unitCost) {
    }

    private record PendingAmmoIssue(int tempId,
                                    int ammoId,
                                    String typeName,
                                    String caliberName,
                                    int rounds,
                                    Double unitCost,
                                    Integer reasonId,
                                    String reasonName) {
    }
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
        setupAddEquipmentPage();
        setupUniformTabs();
        setupAmmunitionTabs();
        setupLookupAdmin();
        loadAgencySettings();
        issueDatePicker.setValue(LocalDate.now());

        refreshAll();
        onTopTabEquipment();
    }

    @FXML
    public void onTopTabEquipment() {
        activeMainTab = MainTab.EQUIPMENT;
        UiNavSupport.setPaneVisible(moduleInventoryPane, true);
        UiNavSupport.setPaneVisible(moduleUniformPane, false);
        UiNavSupport.setPaneVisible(moduleAmmunitionPane, false);
        UiNavSupport.setPaneVisible(moduleIssuePane, false);
        UiNavSupport.setPaneVisible(moduleFleetPane, false);
        UiNavSupport.setPaneVisible(moduleAdminPane, false);
        applyTopTabStyles();
        showEquipmentSubNav();
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onTopTabUniform() {
        activeMainTab = MainTab.UNIFORM;
        UiNavSupport.setPaneVisible(moduleInventoryPane, false);
        UiNavSupport.setPaneVisible(moduleUniformPane, true);
        UiNavSupport.setPaneVisible(moduleAmmunitionPane, false);
        UiNavSupport.setPaneVisible(moduleIssuePane, false);
        UiNavSupport.setPaneVisible(moduleFleetPane, false);
        UiNavSupport.setPaneVisible(moduleAdminPane, false);
        applyTopTabStyles();
        showUniformSubNav(UniformSubTab.STOCK);
        refreshUniforms();
    }

    @FXML
    public void onTopTabAmmunition() {
        activeMainTab = MainTab.AMMUNITION;
        UiNavSupport.setPaneVisible(moduleInventoryPane, false);
        UiNavSupport.setPaneVisible(moduleUniformPane, false);
        UiNavSupport.setPaneVisible(moduleAmmunitionPane, true);
        UiNavSupport.setPaneVisible(moduleIssuePane, false);
        UiNavSupport.setPaneVisible(moduleFleetPane, false);
        UiNavSupport.setPaneVisible(moduleAdminPane, false);
        applyTopTabStyles();
        showAmmunitionSubNav(AmmoSubTab.TYPES);
    }

    @FXML
    public void onTopTabIssueReturn() {
        activeMainTab = MainTab.ISSUE_RETURN;
        UiNavSupport.setPaneVisible(moduleInventoryPane, false);
        UiNavSupport.setPaneVisible(moduleUniformPane, false);
        UiNavSupport.setPaneVisible(moduleAmmunitionPane, false);
        UiNavSupport.setPaneVisible(moduleIssuePane, true);
        UiNavSupport.setPaneVisible(moduleFleetPane, false);
        UiNavSupport.setPaneVisible(moduleAdminPane, false);
        applyTopTabStyles();
        UiNavSupport.setModuleNavMessage(moduleNavBar, moduleNavLabel, "Assign equipment by officer.");
        refreshIssueOptions();
    }

    @FXML
    public void onTopTabAdmin() {
        activeMainTab = MainTab.ADMIN;
        UiNavSupport.setPaneVisible(moduleInventoryPane, false);
        UiNavSupport.setPaneVisible(moduleUniformPane, false);
        UiNavSupport.setPaneVisible(moduleAmmunitionPane, false);
        UiNavSupport.setPaneVisible(moduleIssuePane, false);
        UiNavSupport.setPaneVisible(moduleFleetPane, false);
        UiNavSupport.setPaneVisible(moduleAdminPane, true);
        applyTopTabStyles();
        showAdminSubNav(AdminSubTab.PERSONNEL);
    }

    @FXML
    public void onTopTabVehicle() {
        activeMainTab = MainTab.VEHICLE;
        UiNavSupport.setPaneVisible(moduleInventoryPane, false);
        UiNavSupport.setPaneVisible(moduleUniformPane, false);
        UiNavSupport.setPaneVisible(moduleAmmunitionPane, false);
        UiNavSupport.setPaneVisible(moduleIssuePane, false);
        UiNavSupport.setPaneVisible(moduleFleetPane, true);
        UiNavSupport.setPaneVisible(moduleAdminPane, false);
        applyTopTabStyles();
        UiNavSupport.setModuleNavMessage(moduleNavBar, moduleNavLabel, "Manage fleet units and maintenance history.");
        FleetViewSupport.refreshVehiclesAndMaintenanceOptions(
            vehicleDao,
            vehiclesTable,
            maintenanceVehicleCombo,
            this::onMaintenanceVehicleChanged
        );
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
        if (inventoryHistoricalItemsCheckBox != null) {
            inventoryHistoricalItemsCheckBox.setSelected(false);
        }
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
        showAddEquipmentPage(true);
    }

    @FXML
    public void onCancelAddEquipmentPage() {
        showAddEquipmentPage(false);
    }

    @FXML
    public void onSaveAddEquipmentPage() {
        EquipmentFormData formData = buildAddEquipmentPageFormData();
        if (formData == null) {
            return;
        }

        int created = 0;
        if (formData.branch() == EquipmentBranch.UNIFORM) {
            equipmentDao.insertUniformStock(
                    resolveUniformItemName(formData),
                    UniformCategory.UNIFORM,
                    text(addEqMakeField),
                    text(addEqModelField),
                    formData.sizeId(),
                    formData.quantity() == null ? 1 : formData.quantity(),
                    formData.replacementCost(),
                    null,
                    formData.storageLocationId(),
                    formData.condition(),
                    formData.status()
            );
            created = 1;
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
        showAddEquipmentPage(false);
        refreshEquipmentAndIssueOptions();
    }

    @FXML
    public void onEditEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Equipment", "Select an equipment item to edit.");
            return;
        }

        if (selected.getBranch() == EquipmentBranch.UNIFORM) {
            EquipmentDao.UniformStockRecord existing = equipmentDao.findUniformStockById(selected.getItemId());
            if (existing == null) {
                UiAlerts.error("Equipment", "Uniform stock row no longer exists.");
                return;
            }

            UniformStockFormData uniformFormData = showUniformStockDialog(existing);
            if (uniformFormData == null) {
                return;
            }

            equipmentDao.updateUniformStock(
                    existing.uniformId(),
                    uniformFormData.itemName(),
                    uniformFormData.category(),
                    uniformFormData.brand(),
                    uniformFormData.model(),
                    uniformFormData.sizeId(),
                    uniformFormData.quantityOnHand(),
                    uniformFormData.unitCost(),
                    uniformFormData.supplier(),
                    uniformFormData.storageLocationId(),
                    uniformFormData.condition(),
                    uniformFormData.status()
            );
            refreshEquipmentAndIssueOptions();
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

        if (selected.getBranch() == EquipmentBranch.UNIFORM) {
            equipmentDao.deleteUniformStock(selected.getItemId());
            refreshEquipmentAndIssueOptions();
            UiAlerts.info("Equipment", "Uniform stock row deleted.");
            return;
        }

        try {
            if (selected.getStatus() == EquipmentStatus.RETIRED) {
                UiAlerts.info("Equipment", "Item is already RETIRED.");
                return;
            }

            equipmentDao.updateStatus(selected.getItemId(), EquipmentStatus.RETIRED);
            refreshEquipmentAndIssueOptions();
            UiAlerts.info("Equipment", "Item set to RETIRED. Equipment is no longer physically deleted.");
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            UiAlerts.error("Equipment", "Unable to update equipment status.");
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

        IssueWorkflowSupport.clearPendingChanges(
            pendingIssueItemsById,
            pendingIssuedRowsByItemId,
            pendingReturnRowsByIssuanceId,
            pendingReturnIssuanceIds
        );

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

        List<EquipmentItem> selected = IssueWorkflowSupport.selectedOrFocusedAvailableRows(issueAvailableTable);
        if (selected.isEmpty()) {
            UiAlerts.error("Issue / Return", "Select at least one available item.");
            return;
        }

        for (EquipmentItem item : selected) {
            if (item.getItemId() > 0) {
                IssueWorkflowSupport.stageIssue(
                        item,
                        issueDatePicker.getValue(),
                        pendingIssueItemsById,
                        pendingIssuedRowsByItemId,
                        issueAvailableSource,
                        issuedToOfficerItems
                );
            } else {
                int issuanceId = -item.getItemId();
                IssueWorkflowSupport.cancelStagedReturn(
                        issuanceId,
                        pendingReturnRowsByIssuanceId,
                        pendingReturnIssuanceIds,
                        issueAvailableSource,
                        issuedToOfficerItems
                );
            }
        }
        issueAvailableTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void onRemoveIssueSelection() {
        List<IssuedItemRow> selected = IssueWorkflowSupport.selectedOrFocusedIssuedRows(issueSelectedTable);
        if (selected.isEmpty()) {
            UiAlerts.error("Issue / Return", "Select at least one issued item to return.");
            return;
        }

        for (IssuedItemRow row : selected) {
            if (row.getIssuanceId() < 0) {
                IssueWorkflowSupport.cancelStagedIssue(
                        row.getIssuanceId(),
                        pendingIssueItemsById,
                        pendingIssuedRowsByItemId,
                        issueAvailableSource,
                        issuedToOfficerItems
                );
            } else {
                if (row.getQuantity() > 1 && row.getIssuanceIds() != null && !row.getIssuanceIds().isEmpty()) {
                    IssueWorkflowSupport.stageGroupedReturn(
                            row,
                            pendingReturnRowsByIssuanceId,
                            pendingReturnIssuanceIds,
                            issueAvailableSource,
                            issuedToOfficerItems
                    );
                } else {
                    IssueWorkflowSupport.stageReturn(
                            row,
                            pendingReturnRowsByIssuanceId,
                            pendingReturnIssuanceIds,
                            issueAvailableSource,
                            issuedToOfficerItems
                    );
                }
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
        FleetViewSupport.refreshVehiclesAndMaintenanceOptions(
            vehicleDao,
            vehiclesTable,
            maintenanceVehicleCombo,
            this::onMaintenanceVehicleChanged
        );
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
        FleetViewSupport.refreshVehiclesAndMaintenanceOptions(
            vehicleDao,
            vehiclesTable,
            maintenanceVehicleCombo,
            this::onMaintenanceVehicleChanged
        );
    }

    @FXML
    public void onDeleteVehicle() {
        Vehicle selected = vehiclesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Vehicles", "Select a vehicle to delete.");
            return;
        }

        vehicleDao.delete(selected.getVehicleId());
        FleetViewSupport.refreshVehiclesAndMaintenanceOptions(
            vehicleDao,
            vehiclesTable,
            maintenanceVehicleCombo,
            this::onMaintenanceVehicleChanged
        );
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

        MaintenanceLogDialog.Result result = MaintenanceLogDialog.show(selected, null);
        if (result == null || result.deleteRequested()) {
            return;
        }
        MaintenanceLogDialog.FormData formData = result.formData();

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

        MaintenanceLogDialog.Result result = MaintenanceLogDialog.show(selectedVehicle, selectedLog);
        if (result == null) {
            return;
        }

        if (result.deleteRequested()) {
            maintenanceLogDao.delete(selectedLog.getLogId());
            onMaintenanceVehicleChanged();
            UiAlerts.info("Maintenance", "Maintenance log deleted.");
            return;
        }

        MaintenanceLogDialog.FormData formData = result.formData();

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
        FleetMaintenanceHistorySupport.showVehicleMaintenanceHistoryDialog(
                vehicle,
                maintenanceLogDao,
                maintenanceTable,
                this::getAgencyName,
                () -> moduleFleetPane != null && moduleFleetPane.getScene() != null
                        ? moduleFleetPane.getScene().getWindow()
                        : null
        );
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

        issueSearchField.textProperty().addListener((obs, oldValue, newValue) ->
                IssueWorkflowSupport.applyIssueAvailableFilter(issueAvailableFiltered, issueSearchField.getText()));
        issueOfficerCombo.valueProperty().addListener((obs, oldOfficer, newOfficer) -> {
            IssueWorkflowSupport.clearPendingChanges(
                    pendingIssueItemsById,
                    pendingIssuedRowsByItemId,
                    pendingReturnRowsByIssuanceId,
                    pendingReturnIssuanceIds
            );
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
        issueSelectedSerialColumn.setCellValueFactory(new PropertyValueFactory<>("serialOrQty"));
        issueSelectedSerialColumn.setText("Serial / Qty");
    }

    private void setupFilters() {
        inventoryStatusFilterCombo.setItems(FXCollections.observableArrayList(
                "All",
                EquipmentStatus.AVAILABLE.name(),
                EquipmentStatus.ISSUED.name(),
                EquipmentStatus.MAINTENANCE.name(),
            EquipmentStatus.RETIRED.name(),
            EquipmentStatus.DESTROYED.name()
        ));
        inventoryStatusFilterCombo.getSelectionModel().select("All");
        inventorySearchField.textProperty().addListener((obs, o, n) -> refreshEquipmentAndIssueOptions());
        inventoryStatusFilterCombo.valueProperty().addListener((obs, o, n) -> refreshEquipmentAndIssueOptions());
        if (inventoryHistoricalItemsCheckBox != null) {
            inventoryHistoricalItemsCheckBox.setSelected(false);
            inventoryHistoricalItemsCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshEquipmentAndIssueOptions());
        }
    }

    private void setupAddEquipmentPage() {
        if (addEqBranchCombo == null) {
            return;
        }

        addEqBranchCombo.setItems(FXCollections.observableArrayList(EquipmentBranch.values()));
        addEqConditionCombo.setItems(FXCollections.observableArrayList(EquipmentCondition.values()));
        addEqStatusCombo.setItems(FXCollections.observableArrayList(EquipmentStatus.values()));

        addEqTypeCombo.setEditable(true);
        addEqWeaponTypeCombo.setEditable(true);
        addEqCaliberCombo.setEditable(true);
        addEqSizeCombo.setEditable(true);
        addEqStorageCombo.setEditable(true);
        enableLookupTypeAhead(addEqTypeCombo);
        enableLookupTypeAhead(addEqWeaponTypeCombo);
        enableLookupTypeAhead(addEqCaliberCombo);
        enableLookupTypeAhead(addEqSizeCombo);
        enableLookupTypeAhead(addEqStorageCombo);

        addEqBranchCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            refreshAddEquipmentTypeOptions(newValue);
            applyAddEquipmentBranchState(newValue);
            refreshAddEquipmentGeneratedName();
        });

        addEqMakeField.textProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqModelField.textProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqTypeCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqWeaponTypeCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqCaliberCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqSizeCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqTypeCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqWeaponTypeCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqCaliberCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());
        addEqSizeCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshAddEquipmentGeneratedName());

        showAddEquipmentPage(false);
    }

    private void showAddEquipmentPage(boolean show) {
        if (inventoryAddPagePane == null || inventoryListCard == null) {
            return;
        }

        if (inventoryHeaderRow != null) {
            UiNavSupport.setPaneVisible(inventoryHeaderRow, !show);
        }
        if (inventoryFiltersCard != null) {
            UiNavSupport.setPaneVisible(inventoryFiltersCard, !show);
        }
        UiNavSupport.setPaneVisible(inventoryAddPagePane, show);
        UiNavSupport.setPaneVisible(inventoryListCard, !show);

        if (!show) {
            return;
        }

        addEqBranchCombo.getSelectionModel().select(activeEquipmentBranch);
        refreshAddEquipmentTypeOptions(addEqBranchCombo.getValue());

        addEqWeaponTypeCombo.setItems(FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.WEAPON_TYPES)));
        addEqCaliberCombo.setItems(FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.CALIBERS)));
        addEqSizeCombo.setItems(FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.UNIFORM_SIZES)));
        addEqStorageCombo.setItems(FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.STORAGE_LOCATIONS)));

        addEqMakeField.setText("");
        addEqModelField.setText("");
        addEqNameField.setText("");
        addEqSerialsArea.setText("");
        addEqQuantityField.setText("1");
        addEqSerialPrefixField.setText("");
        addEqReplacementCostField.setText("");
        addEqAttachmentCheck.setSelected(false);

        addEqTypeCombo.getSelectionModel().clearSelection();
        addEqWeaponTypeCombo.getSelectionModel().clearSelection();
        addEqCaliberCombo.getSelectionModel().clearSelection();
        addEqSizeCombo.getSelectionModel().clearSelection();
        addEqStorageCombo.getSelectionModel().selectFirst();

        addEqConditionCombo.getSelectionModel().select(EquipmentCondition.GOOD);
        addEqStatusCombo.getSelectionModel().select(EquipmentStatus.AVAILABLE);

        applyAddEquipmentBranchState(addEqBranchCombo.getValue());
        refreshAddEquipmentGeneratedName();
    }

    private void refreshAddEquipmentTypeOptions(EquipmentBranch branch) {
        LookupCategory category = branch == EquipmentBranch.UNIFORM
                ? LookupCategory.UNIFORM_ITEMS
                : LookupCategory.EQUIPMENT_TYPES;
        addEqTypeCombo.setItems(FXCollections.observableArrayList(lookupDao.findAll(category)));
        addEqTypeCombo.getSelectionModel().clearSelection();
    }

    private void applyAddEquipmentBranchState(EquipmentBranch branch) {
        boolean weapon = branch == EquipmentBranch.WEAPON;
        boolean uniform = branch == EquipmentBranch.UNIFORM;
        boolean equipment = branch == EquipmentBranch.EQUIPMENT;

        addEqTypeCombo.setDisable(weapon);
        addEqWeaponTypeCombo.setDisable(!weapon);
        addEqCaliberCombo.setDisable(!weapon);
        addEqSizeCombo.setDisable(!uniform);
        addEqAttachmentCheck.setDisable(!equipment);
        if (!equipment) {
            addEqAttachmentCheck.setSelected(false);
        }

        addEqSerialsArea.setDisable(uniform);
        addEqQuantityField.setDisable(!uniform);
        addEqSerialPrefixField.setDisable(!uniform);

        if (addEqCategoryRow != null) {
            UiNavSupport.setPaneVisible(addEqCategoryRow, !weapon);
        }
        if (addEqWeaponRow != null) {
            UiNavSupport.setPaneVisible(addEqWeaponRow, weapon);
        }
        if (addEqUniformSizeRow != null) {
            UiNavSupport.setPaneVisible(addEqUniformSizeRow, uniform);
        }
        if (addEqAttachmentRow != null) {
            UiNavSupport.setPaneVisible(addEqAttachmentRow, equipment);
        }
        if (addEqSerialsSection != null) {
            UiNavSupport.setPaneVisible(addEqSerialsSection, !uniform);
        }
        if (addEqUniformSection != null) {
            UiNavSupport.setPaneVisible(addEqUniformSection, uniform);
        }

        if (weapon) {
            addEqTypeCombo.getSelectionModel().clearSelection();
            addEqTypeCombo.getEditor().clear();
            addEqSizeCombo.getSelectionModel().clearSelection();
            addEqSizeCombo.getEditor().clear();
        }
        if (uniform) {
            addEqWeaponTypeCombo.getSelectionModel().clearSelection();
            addEqWeaponTypeCombo.getEditor().clear();
            addEqCaliberCombo.getSelectionModel().clearSelection();
            addEqCaliberCombo.getEditor().clear();
            addEqSerialsArea.clear();
        }
        if (equipment) {
            addEqWeaponTypeCombo.getSelectionModel().clearSelection();
            addEqWeaponTypeCombo.getEditor().clear();
            addEqCaliberCombo.getSelectionModel().clearSelection();
            addEqCaliberCombo.getEditor().clear();
            addEqSizeCombo.getSelectionModel().clearSelection();
            addEqSizeCombo.getEditor().clear();
            addEqQuantityField.setText("1");
            addEqSerialPrefixField.clear();
        }
    }

    private void refreshAddEquipmentGeneratedName() {
        if (addEqNameField == null || !text(addEqNameField).isEmpty()) {
            return;
        }

        EquipmentBranch branch = addEqBranchCombo.getValue();
        addEqNameField.setText(buildGeneratedEquipmentName(
                text(addEqMakeField),
                text(addEqModelField),
                branch,
                selectedName(addEqTypeCombo),
                selectedName(addEqWeaponTypeCombo),
                selectedName(addEqCaliberCombo),
                selectedName(addEqSizeCombo)
        ));
    }

    private EquipmentFormData buildAddEquipmentPageFormData() {
        EquipmentBranch branch = addEqBranchCombo.getValue();
        if (branch == null) {
            UiAlerts.error("Equipment", "Branch is required.");
            return null;
        }

        Double replacementCost;
        String replacementCostText = text(addEqReplacementCostField);
        if (replacementCostText.isEmpty()) {
            UiAlerts.error("Equipment", "Replacement cost is required.");
            return null;
        }
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

        Integer quantity = null;
        if (branch == EquipmentBranch.UNIFORM) {
            try {
                quantity = Integer.parseInt(text(addEqQuantityField));
                if (quantity <= 0) {
                    UiAlerts.error("Equipment", "Uniform quantity must be greater than zero.");
                    return null;
                }
            } catch (NumberFormatException ex) {
                UiAlerts.error("Equipment", "Uniform quantity must be a whole number.");
                return null;
            }
        }

        List<String> serials = parseSerials(addEqSerialsArea);
        if (branch != EquipmentBranch.UNIFORM && serials.isEmpty()) {
            UiAlerts.error("Equipment", "Enter at least one serial number to add inventory in bulk.");
            return null;
        }

        String effectiveName = text(addEqNameField);
        if (effectiveName.isEmpty()) {
            effectiveName = buildGeneratedEquipmentName(
                    text(addEqMakeField),
                    text(addEqModelField),
                    branch,
                    selectedName(addEqTypeCombo),
                    selectedName(addEqWeaponTypeCombo),
                    selectedName(addEqCaliberCombo),
                    selectedName(addEqSizeCombo)
            );
        }
        if (effectiveName.isEmpty()) {
            UiAlerts.error("Equipment", "Name is required.");
            return null;
        }

        EquipmentCondition condition = addEqConditionCombo.getValue();
        EquipmentStatus status = addEqStatusCombo.getValue();
        if (condition == null || status == null) {
            UiAlerts.error("Equipment", "Condition and status are required.");
            return null;
        }

        Integer effectiveEquipmentTypeId = branch == EquipmentBranch.WEAPON ? null : selectedId(addEqTypeCombo);
        String effectiveEquipmentTypeName = branch == EquipmentBranch.WEAPON ? null : selectedName(addEqTypeCombo);

        return new EquipmentFormData(
                effectiveName,
                branch,
                effectiveEquipmentTypeId, effectiveEquipmentTypeName,
                selectedId(addEqWeaponTypeCombo), selectedName(addEqWeaponTypeCombo),
                selectedId(addEqCaliberCombo), selectedName(addEqCaliberCombo),
                selectedId(addEqSizeCombo), selectedName(addEqSizeCombo),
                selectedId(addEqStorageCombo), selectedName(addEqStorageCombo),
                "",
                serials,
                quantity,
                text(addEqSerialPrefixField),
                replacementCost,
                condition,
                status,
                branch == EquipmentBranch.EQUIPMENT && addEqAttachmentCheck.isSelected()
        );
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
                    case "DESTROYED" -> getStyleClass().add("status-retired");
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
        FleetViewSupport.refreshVehiclesAndMaintenanceOptions(
            vehicleDao,
            vehiclesTable,
            maintenanceVehicleCombo,
            this::onMaintenanceVehicleChanged
        );
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

        VBox printable = PrintNodeFactory.buildEquipmentHistoryPrintableNode(getAgencyName(), item, history);
        Window owner = moduleInventoryPane != null && moduleInventoryPane.getScene() != null
                ? moduleInventoryPane.getScene().getWindow()
                : null;
        JavaFxPrintSupport.printNode(owner, printable);
    }

    private boolean openEquipmentHistoryPrintPreview(EquipmentItem item, List<IssuanceAdminRow> history) {
        return WindowsPrintPreview.openHtml(
                "quartermaster-item-history-",
                                PrintHtmlFactory.buildEquipmentHistoryHtml(getAgencyName(), item, history)
        );
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
        boolean showHistorical = inventoryHistoricalItemsCheckBox != null && inventoryHistoricalItemsCheckBox.isSelected();
        String selectedStatus = inventoryStatusFilterCombo.getSelectionModel().getSelectedItem();
        String query = inventorySearchField.getText();
        List<EquipmentItem> filtered = byBranch.stream()
            .filter(item -> InventoryViewSupport.matchesInventoryFilter(item, showHistorical, selectedStatus, query))
                .collect(Collectors.toList());

        List<EquipmentItem> displayRows = InventoryViewSupport.buildInventoryDisplayRows(
                filtered,
                activeEquipmentBranch,
                selectedStatus
        );

        equipmentTable.setItems(FXCollections.observableArrayList(displayRows));
        inventorySummaryLabel.setText(InventoryViewSupport.buildInventorySummaryText(displayRows, byBranch.size(), activeEquipmentBranch));

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

        for (IssuedItemRow pendingReturn : pendingReturnRowsByIssuanceId.values()) {
            available.add(IssueWorkflowSupport.toPendingAvailablePlaceholder(pendingReturn));
        }
        available.sort(Comparator.comparing(EquipmentItem::getName, String.CASE_INSENSITIVE_ORDER));

        issueAvailableSource.setAll(available);
        IssueWorkflowSupport.applyIssueAvailableFilter(issueAvailableFiltered, issueSearchField.getText());
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

        String adminName = SessionManager.getCurrentUser() == null
            ? ""
            : SessionManager.getCurrentUser().getUsername();
        VBox printable = PrintNodeFactory.buildIssuancePrintableNode(
            getAgencyName(),
            adminName,
            officer,
            currentIssued,
            returnedItems
        );
        Window owner = moduleIssuePane != null && moduleIssuePane.getScene() != null
                ? moduleIssuePane.getScene().getWindow()
                : null;
        JavaFxPrintSupport.printNode(owner, printable);
    }

        private boolean openWindowsPrintPreview(Officer officer, List<IssuedItemRow> currentIssued,
                            List<IssuedItemRow> returnedItems) {
            String adminName = SessionManager.getCurrentUser() == null
                    ? ""
                    : SessionManager.getCurrentUser().getUsername();
            return WindowsPrintPreview.openHtml(
                "quartermaster-signoff-",
                PrintHtmlFactory.buildIssuancePrintHtml(getAgencyName(), adminName, officer, currentIssued, returnedItems)
            );
        }

                    private Integer currentUserIdOrNull() {
                        return SessionManager.getCurrentUser() == null ? null : SessionManager.getCurrentUser().getUserId();
                    }

    private void showEquipmentSubNav() {
        moduleNavLabel.setText("Equipment Categories");

        Button weaponsButton = UiNavSupport.createSubNavButton("Weapons", () -> setEquipmentBranch(EquipmentBranch.WEAPON));
        Button dutyButton = UiNavSupport.createSubNavButton("Duty", () -> setEquipmentBranch(EquipmentBranch.EQUIPMENT));
        Button addButton = UiNavSupport.createSubNavButton("Add Equipment", this::onAddEquipment);

        moduleNavBar.getChildren().setAll(moduleNavLabel, weaponsButton, dutyButton, addButton);
        if (activeEquipmentBranch == EquipmentBranch.UNIFORM) {
            activeEquipmentBranch = EquipmentBranch.WEAPON;
        }
        setEquipmentBranch(activeEquipmentBranch);
    }

    private void setEquipmentBranch(EquipmentBranch branch) {
        showAddEquipmentPage(false);
        this.activeEquipmentBranch = branch;
        inventoryModuleTitleLabel.setText(branch.getDisplayName());

        List<Button> buttons = UiNavSupport.getSubNavButtons(moduleNavBar);
        if (buttons.size() >= 3) {
            UiNavSupport.clearActive(buttons);
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

        Button personnelButton = UiNavSupport.createSubNavButton("Officers", () -> switchAdminSubTab(AdminSubTab.PERSONNEL));
        Button lookupsButton = UiNavSupport.createSubNavButton("Lookups", () -> switchAdminSubTab(AdminSubTab.LOOKUPS));
        Button historicalButton = UiNavSupport.createSubNavButton("Historical", () -> switchAdminSubTab(AdminSubTab.HISTORICAL));
        Button settingsButton = UiNavSupport.createSubNavButton("Settings", () -> switchAdminSubTab(AdminSubTab.SETTINGS));

        moduleNavBar.getChildren().setAll(moduleNavLabel, personnelButton, lookupsButton, historicalButton, settingsButton);
        switchAdminSubTab(subTab);
    }

    private void switchAdminSubTab(AdminSubTab subTab) {
        List<Button> buttons = UiNavSupport.getSubNavButtons(moduleNavBar);
        UiNavSupport.clearActive(buttons);
        switch (subTab) {
            case PERSONNEL -> {
                adminSubModuleTitle.setText("Personnel");
                UiNavSupport.setPaneVisible(adminPersonnelPane, true);
                UiNavSupport.setPaneVisible(adminLookupsPane, false);
                UiNavSupport.setPaneVisible(adminSettingsPane, false);
                UiNavSupport.setPaneVisible(adminHistoryPane, false);
                UiNavSupport.activateButtonByIndex(buttons, 0);
            }
            case LOOKUPS -> {
                adminSubModuleTitle.setText("Lookups");
                UiNavSupport.setPaneVisible(adminPersonnelPane, false);
                UiNavSupport.setPaneVisible(adminLookupsPane, true);
                UiNavSupport.setPaneVisible(adminSettingsPane, false);
                UiNavSupport.setPaneVisible(adminHistoryPane, false);
                UiNavSupport.activateButtonByIndex(buttons, 1);
            }
            case HISTORICAL -> {
                adminSubModuleTitle.setText("Historical Issuance");
                UiNavSupport.setPaneVisible(adminPersonnelPane, false);
                UiNavSupport.setPaneVisible(adminLookupsPane, false);
                UiNavSupport.setPaneVisible(adminSettingsPane, false);
                UiNavSupport.setPaneVisible(adminHistoryPane, true);
                refreshIssuances();
                UiNavSupport.activateButtonByIndex(buttons, 2);
            }
            case SETTINGS -> {
                adminSubModuleTitle.setText("Settings");
                UiNavSupport.setPaneVisible(adminPersonnelPane, false);
                UiNavSupport.setPaneVisible(adminLookupsPane, false);
                UiNavSupport.setPaneVisible(adminSettingsPane, true);
                UiNavSupport.setPaneVisible(adminHistoryPane, false);
                UiNavSupport.activateButtonByIndex(buttons, 3);
            }
        }
    }

    private void applyTopTabStyles() {
        List<Button> buttons = List.of(topTabEquipment, topTabUniform, topTabAmmunition, topTabIssueReturn, topTabAdmin, topTabVehicle);
        UiNavSupport.clearActive(buttons);

        switch (activeMainTab) {
            case EQUIPMENT -> topTabEquipment.getStyleClass().add("active");
            case UNIFORM -> topTabUniform.getStyleClass().add("active");
            case AMMUNITION -> topTabAmmunition.getStyleClass().add("active");
            case ISSUE_RETURN -> topTabIssueReturn.getStyleClass().add("active");
            case ADMIN -> topTabAdmin.getStyleClass().add("active");
            case VEHICLE -> topTabVehicle.getStyleClass().add("active");
        }
    }

    private void setupAmmunitionTabs() {
        if (ammoStockTable == null) {
            return;
        }
        // Ammo type definitions table
        if (ammoTypesTable != null) {
            ammoTypeDefCaliberColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().caliber())));
            ammoTypeDefTypeColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().typeName())));
            ammoTypeDefUseColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().useType())));
            ammoTypeDefCostColumn.setCellValueFactory(c -> Bindings.createStringBinding(() ->
                c.getValue().costPerBox() == null ? "" : String.format("$%.2f", c.getValue().costPerBox())));
            ammoTypeDefRoundsColumn.setCellValueFactory(c -> Bindings.createStringBinding(() ->
                c.getValue().roundsPerBox() == null ? "" : String.valueOf(c.getValue().roundsPerBox())));
        }
        ammoTypeColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().typeName())));
        ammoCaliberColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().caliberName())));
        ammoUseColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().useName())));
        ammoRoundsColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> String.valueOf(c.getValue().roundsOnHand())));
        ammoUnitCostColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> c.getValue().unitCost() == null ? "" : String.format("$%.2f", c.getValue().unitCost())));
        ammoStorageColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().storageLocationName())));
        ammoNotesColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().notes())));
        ammoStatusColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().status())));

        ammoIssueTypeColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().typeName())));
        ammoIssueCaliberColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().caliberName())));
        ammoIssueUseColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().useName())));
        ammoIssueRoundsColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> String.valueOf(c.getValue().roundsOnHand())));
        ammoIssueStorageColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().storageLocationName())));

        ammoIssuedTypeColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().typeName())));
        ammoIssuedCaliberColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().caliberName())));
        ammoIssuedRoundsColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> String.valueOf(c.getValue().rounds())));
        ammoIssuedReasonColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().reasonName())));
        ammoIssuedDateColumn.setCellValueFactory(c -> Bindings.createStringBinding(() ->
            c.getValue().issuedAt() == null ? "" :
                c.getValue().issuedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        ammoIssuedStatusColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().status())));

        ammoIssueOfficerCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Officer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getBadgeNumber() + ")");
            }
        });
        ammoIssueOfficerCombo.setButtonCell(ammoIssueOfficerCombo.getCellFactory().call(null));
        ammoIssueOfficerCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressAmmoOfficerSelectionListener) {
                return;
            }
            clearPendingAmmoChanges();
            refreshAmmunition(false);
        });

        ammoIssueReasonCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(LookupItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        ammoIssueReasonCombo.setButtonCell(ammoIssueReasonCombo.getCellFactory().call(null));

        if (ammoShowHistoryCheckBox != null) {
            ammoShowHistoryCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> refreshAmmunitionIssuedToOfficer());
        }
    }

    private void showAmmunitionSubNav(AmmoSubTab subTab) {
        if (moduleNavBar == null || moduleNavLabel == null) {
            return;
        }
        moduleNavLabel.setText("Ammunition");
        Button typesButton = UiNavSupport.createSubNavButton("Ammo Types", () -> switchAmmunitionSubTab(AmmoSubTab.TYPES));
        Button stockButton = UiNavSupport.createSubNavButton("Ammo Stock", () -> switchAmmunitionSubTab(AmmoSubTab.STOCK));
        Button issueButton = UiNavSupport.createSubNavButton("Issue", () -> switchAmmunitionSubTab(AmmoSubTab.ISSUE));
        moduleNavBar.getChildren().setAll(moduleNavLabel, typesButton, stockButton, issueButton);
        switchAmmunitionSubTab(subTab);
    }

    private void switchAmmunitionSubTab(AmmoSubTab subTab) {
        List<Button> buttons = UiNavSupport.getSubNavButtons(moduleNavBar);
        UiNavSupport.clearActive(buttons);
        switch (subTab) {
            case TYPES -> {
                ammoModuleTitleLabel.setText("Ammo Type Definitions");
                UiNavSupport.setPaneVisible(ammoTypesPane, true);
                UiNavSupport.setPaneVisible(ammoStockPane, false);
                UiNavSupport.setPaneVisible(ammoIssuePane, false);
                UiNavSupport.activateButtonByIndex(buttons, 0);
                refreshAmmoTypeDefs();
            }
            case STOCK -> {
                ammoModuleTitleLabel.setText("Ammunition Stock");
                UiNavSupport.setPaneVisible(ammoTypesPane, false);
                UiNavSupport.setPaneVisible(ammoStockPane, true);
                UiNavSupport.setPaneVisible(ammoIssuePane, false);
                UiNavSupport.activateButtonByIndex(buttons, 1);
            }
            case ISSUE -> {
                ammoModuleTitleLabel.setText("Issue / Return Ammunition");
                UiNavSupport.setPaneVisible(ammoTypesPane, false);
                UiNavSupport.setPaneVisible(ammoStockPane, false);
                UiNavSupport.setPaneVisible(ammoIssuePane, true);
                UiNavSupport.activateButtonByIndex(buttons, 2);
                refreshAmmunition();
            }
        }
    }

    private void refreshAmmunition() {
        refreshAmmunition(true);
    }

    private void refreshAmmunition(boolean reloadOfficerChoices) {
        if (ammoStockTable == null) {
            return;
        }
        List<AmmunitionDao.AmmoStockRecord> rows = ammunitionDao.findAll();
        ammoStockTable.setItems(FXCollections.observableArrayList(rows));

        Map<Integer, Integer> pendingByAmmoId = new HashMap<>();
        for (PendingAmmoIssue pending : pendingAmmoIssuesByTempId.values()) {
            pendingByAmmoId.merge(pending.ammoId(), pending.rounds(), Integer::sum);
        }
        List<AmmunitionDao.AmmoStockRecord> available = rows.stream()
                .filter(r -> "ACTIVE".equalsIgnoreCase(r.status()))
                .map(r -> {
                    int reserved = pendingByAmmoId.getOrDefault(r.ammoId(), 0);
                    int adjusted = r.roundsOnHand() - reserved;
                    if (adjusted == r.roundsOnHand()) {
                        return r;
                    }
                    return new AmmunitionDao.AmmoStockRecord(
                            r.ammoId(),
                            r.typeDefId(),
                            r.typeName(),
                            r.caliberName(),
                            r.useName(),
                            r.costPerBox(),
                            r.roundsPerBox(),
                            Math.max(0, adjusted),
                            r.unitCost(),
                            r.storageLocationId(), r.storageLocationName(),
                            r.notes(),
                            r.status()
                    );
                })
                .filter(r -> r.roundsOnHand() > 0)
                .collect(Collectors.toList());
        ammoIssueAvailableTable.setItems(FXCollections.observableArrayList(available));

        if (reloadOfficerChoices && ammoIssueOfficerCombo != null) {
            Officer previouslySelected = ammoIssueOfficerCombo.getSelectionModel().getSelectedItem();
            List<Officer> allOfficers = officerDao.findAll();
            suppressAmmoOfficerSelectionListener = true;
            try {
                ammoIssueOfficerCombo.setItems(FXCollections.observableArrayList(allOfficers));
                if (previouslySelected != null) {
                    allOfficers.stream()
                            .filter(o -> o.getOfficerId() == previouslySelected.getOfficerId())
                            .findFirst()
                            .ifPresent(o -> ammoIssueOfficerCombo.getSelectionModel().select(o));
                }
            } finally {
                suppressAmmoOfficerSelectionListener = false;
            }
        }
        if (ammoIssueReasonCombo != null) {
            ammoIssueReasonCombo.setItems(FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.AMMO_REASONS)));
        }
        refreshAmmunitionIssuedToOfficer();
    }

    private void refreshAmmunitionIssuedToOfficer() {
        if (ammoIssuedTable == null) {
            return;
        }
        Officer officer = ammoIssueOfficerCombo == null ? null : ammoIssueOfficerCombo.getValue();
        if (officer == null) {
            ammoIssuedTable.setItems(FXCollections.observableArrayList());
            if (ammoIssuedToOfficerLabel != null) {
                ammoIssuedToOfficerLabel.setText("Select an officer to view issued ammunition.");
            }
            return;
        }
        boolean showHistory = ammoShowHistoryCheckBox != null && ammoShowHistoryCheckBox.isSelected();
        List<AmmoIssuanceDao.AmmoIssuanceRow> active = showHistory
            ? ammoIssuanceDao.findAllByOfficer(officer.getOfficerId())
            : ammoIssuanceDao.findActiveByOfficer(officer.getOfficerId());
        List<AmmoIssuanceDao.AmmoIssuanceRow> display = new ArrayList<>(active.size() + pendingAmmoIssuesByTempId.size());
        for (AmmoIssuanceDao.AmmoIssuanceRow row : active) {
            if (pendingAmmoReturnIds.contains(row.id())) {
                display.add(new AmmoIssuanceDao.AmmoIssuanceRow(
                    row.id(), row.ammoId(), row.typeName(), row.caliberName(),
                        row.rounds(), row.unitCostAtIssue(), row.reasonId(), row.reasonName(),
                        row.issuedAt(), row.issuedByUsername(), row.returnedAt(), row.returnedByUsername(),
                        "PENDING RETURN", row.officerId(), row.officerName(), row.officerBadge()
                ));
            } else if (pendingAmmoExpendedIds.contains(row.id())) {
                display.add(new AmmoIssuanceDao.AmmoIssuanceRow(
                    row.id(), row.ammoId(), row.typeName(), row.caliberName(),
                        row.rounds(), row.unitCostAtIssue(), row.reasonId(), row.reasonName(),
                        row.issuedAt(), row.issuedByUsername(), row.returnedAt(), row.returnedByUsername(),
                        "PENDING EXPENDED", row.officerId(), row.officerName(), row.officerBadge()
                ));
            } else {
                display.add(row);
            }
        }
        for (PendingAmmoIssue pending : pendingAmmoIssuesByTempId.values()) {
            display.add(new AmmoIssuanceDao.AmmoIssuanceRow(
                    pending.tempId(), pending.ammoId(), pending.typeName(),
                    pending.caliberName(), pending.rounds(), pending.unitCost(), pending.reasonId(),
                    pending.reasonName(), null, null, null, null,
                    "PENDING ISSUE", officer.getOfficerId(), officer.getName(), officer.getBadgeNumber()
            ));
        }
        ammoIssuedTable.setItems(FXCollections.observableArrayList(display));
        if (ammoIssuedToOfficerLabel != null) {
            int activeRounds = active.stream()
                    .filter(r -> !pendingAmmoReturnIds.contains(r.id()) && !pendingAmmoExpendedIds.contains(r.id()))
                    .mapToInt(AmmoIssuanceDao.AmmoIssuanceRow::rounds).sum();
            int pendingAdd = pendingAmmoIssuesByTempId.values().stream().mapToInt(PendingAmmoIssue::rounds).sum();
            int pendingRet = pendingAmmoReturnIds.size();
                int pendingExp = pendingAmmoExpendedIds.size();
                String pendingTxt = (pendingAdd == 0 && pendingRet == 0 && pendingExp == 0)
                    ? ""
                    : String.format("  |  Pending: +%d to issue, %d to return, %d to expend", pendingAdd, pendingRet, pendingExp);
            ammoIssuedToOfficerLabel.setText(officer.getName() + " (" + officer.getBadgeNumber()
                    + ") - " + activeRounds + " rounds currently issued" + pendingTxt);
        }
    }

    private void clearPendingAmmoChanges() {
        pendingAmmoIssuesByTempId.clear();
        pendingAmmoReturnIds.clear();
            pendingAmmoExpendedIds.clear();
    }

    private void refreshAmmoTypeDefs() {
        if (ammoTypesTable == null) return;
        ammoTypesTable.setItems(FXCollections.observableArrayList(ammoTypeDefinitionDao.findAll()));
    }

    @FXML
    public void onAddAmmoTypeDef() {
        AmmoTypeDefinitionDao.AmmoTypeDefRecord result = showAmmoTypeDefDialog(null);
        if (result != null) {
            ammoTypeDefinitionDao.insert(result.caliber(), result.typeName(), result.useType(), result.costPerBox(), result.roundsPerBox());
            refreshAmmoTypeDefs();
        }
    }

    @FXML
    public void onEditAmmoTypeDef() {
        AmmoTypeDefinitionDao.AmmoTypeDefRecord selected = ammoTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Ammo Types", "Select a row to edit.");
            return;
        }
        AmmoTypeDefinitionDao.AmmoTypeDefRecord result = showAmmoTypeDefDialog(selected);
        if (result != null) {
            ammoTypeDefinitionDao.update(selected.id(), result.caliber(), result.typeName(), result.useType(), result.costPerBox(), result.roundsPerBox());
            refreshAmmoTypeDefs();
        }
    }

    @FXML
    public void onDeleteAmmoTypeDef() {
        AmmoTypeDefinitionDao.AmmoTypeDefRecord selected = ammoTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Ammo Types", "Select a row to delete.");
            return;
        }
        ammoTypeDefinitionDao.delete(selected.id());
        refreshAmmoTypeDefs();
    }

    private AmmoTypeDefinitionDao.AmmoTypeDefRecord showAmmoTypeDefDialog(AmmoTypeDefinitionDao.AmmoTypeDefRecord existing) {
        Dialog<AmmoTypeDefinitionDao.AmmoTypeDefRecord> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Ammo Type" : "Edit Ammo Type");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField caliberField = new TextField(existing == null ? "" : existing.caliber());
        caliberField.setPromptText("e.g. 9mm, .45 ACP, 5.56x45");
        TextField typeField = new TextField(existing == null ? "" : existing.typeName());
        typeField.setPromptText("e.g. Speer Gold Dot, Federal HST");
        ComboBox<String> useCombo = new ComboBox<>(FXCollections.observableArrayList("Duty", "Practice"));
        useCombo.getSelectionModel().select(existing == null ? "Duty" : existing.useType());
        TextField costPerBoxField = new TextField(existing == null || existing.costPerBox() == null ? "" : String.format("%.2f", existing.costPerBox()));
        costPerBoxField.setPromptText("Cost per box (optional)");
        TextField roundsPerBoxField = new TextField(existing == null || existing.roundsPerBox() == null ? "" : String.valueOf(existing.roundsPerBox()));
        roundsPerBoxField.setPromptText("Rounds per box (optional)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int row = 0;
        grid.add(new Label("Caliber"), 0, row);   grid.add(caliberField, 1, row++);
        grid.add(new Label("Type"), 0, row);       grid.add(typeField, 1, row++);
        grid.add(new Label("Use"), 0, row);        grid.add(useCombo, 1, row++);
        grid.add(new Label("Cost / Box"), 0, row); grid.add(costPerBoxField, 1, row++);
        grid.add(new Label("Rounds / Box"), 0, row); grid.add(roundsPerBoxField, 1, row++);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != saveButton) return null;
            String caliber = text(caliberField);
            String typeName = text(typeField);
            if (caliber.isEmpty() || typeName.isEmpty()) {
                UiAlerts.error("Ammo Types", "Caliber and Type are required.");
                return null;
            }
            Double costPerBox = null;
            if (!text(costPerBoxField).isEmpty()) {
                try { costPerBox = Double.parseDouble(text(costPerBoxField)); }
                catch (NumberFormatException ex) { UiAlerts.error("Ammo Types", "Cost per box must be numeric."); return null; }
            }
            Integer roundsPerBox = null;
            if (!text(roundsPerBoxField).isEmpty()) {
                try { roundsPerBox = Integer.parseInt(text(roundsPerBoxField)); }
                catch (NumberFormatException ex) { UiAlerts.error("Ammo Types", "Rounds per box must be a whole number."); return null; }
            }
            return new AmmoTypeDefinitionDao.AmmoTypeDefRecord(0, caliber, typeName, useCombo.getValue(), costPerBox, roundsPerBox);
        });

        Optional<AmmoTypeDefinitionDao.AmmoTypeDefRecord> result = dialog.showAndWait();
        return result.orElse(null);
    }

    @FXML
    public void onAddAmmoStock() {
        AmmoStockFormData formData = showAmmoStockDialog(null);
        if (formData == null) {
            return;
        }

        // Counter behavior: if same ammo profile exists, add rounds to that row
        // instead of creating another duplicate line item.
        AmmunitionDao.AmmoStockRecord existing = ammunitionDao.findAll().stream()
                .filter(r -> Objects.equals(r.typeDefId(), formData.typeDefId()))
                .filter(r -> Objects.equals(r.status(), formData.status()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            int newRounds = existing.roundsOnHand() + formData.roundsOnHand();
            Double unitCost = formData.unitCost() == null ? existing.unitCost() : formData.unitCost();
            Integer storageId = formData.storageLocationId() == null ? existing.storageLocationId() : formData.storageLocationId();
            String notes = (formData.notes() == null || formData.notes().isBlank()) ? existing.notes() : formData.notes();
            ammunitionDao.update(
                    existing.ammoId(),
                    existing.typeDefId(),
                    newRounds,
                    unitCost,
                    storageId,
                    notes,
                    existing.status()
            );
        } else {
            ammunitionDao.insert(
                    formData.typeDefId(),
                    formData.roundsOnHand(),
                    formData.unitCost(),
                    formData.storageLocationId(),
                    formData.notes(),
                    formData.status()
            );
        }
        refreshAmmunition();
    }

    @FXML
    public void onEditAmmoStock() {
        AmmunitionDao.AmmoStockRecord selected = ammoStockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Ammunition Stock", "Select a row to edit.");
            return;
        }
        AmmoStockFormData formData = showAmmoStockDialog(selected);
        if (formData == null) {
            return;
        }
        ammunitionDao.update(
                selected.ammoId(),
                formData.typeDefId(),
                formData.roundsOnHand(),
                formData.unitCost(),
                formData.storageLocationId(),
                formData.notes(),
                formData.status()
        );
        refreshAmmunition();
    }

    @FXML
    public void onDeleteAmmoStock() {
        AmmunitionDao.AmmoStockRecord selected = ammoStockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Ammunition Stock", "Select a row to delete.");
            return;
        }
        ammunitionDao.delete(selected.ammoId());
        refreshAmmunition();
    }

    @FXML
    public void onAddAmmoIssue() {
        Officer officer = ammoIssueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            UiAlerts.error("Issue Ammunition", "Select an officer first.");
            return;
        }
        AmmunitionDao.AmmoStockRecord selected = ammoIssueAvailableTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Issue Ammunition", "Select an ammunition stock row.");
            return;
        }
        int rounds;
        try {
            rounds = Integer.parseInt(text(ammoIssueRoundsField));
        } catch (NumberFormatException ex) {
            UiAlerts.error("Issue Ammunition", "Rounds must be a whole number.");
            return;
        }
        if (rounds <= 0) {
            UiAlerts.error("Issue Ammunition", "Rounds must be greater than zero.");
            return;
        }
        if (rounds > selected.roundsOnHand()) {
            UiAlerts.error("Issue Ammunition", "Only " + selected.roundsOnHand() + " rounds available (after pending).");
            return;
        }
        LookupItem reason = ammoIssueReasonCombo.getSelectionModel().getSelectedItem();
        PendingAmmoIssue pending = new PendingAmmoIssue(
                nextPendingAmmoIssueTempId--,
                selected.ammoId(),
            selected.typeName(),
                selected.caliberName(),
                rounds,
                selected.unitCost(),
                reason == null ? null : reason.getId(),
                reason == null ? null : reason.getName()
        );
        pendingAmmoIssuesByTempId.put(pending.tempId(), pending);
        refreshAmmunition();
    }

    @FXML
    public void onRemoveAmmoIssue() {
        if (ammoIssuedTable == null) {
            return;
        }
        AmmoIssuanceDao.AmmoIssuanceRow selected = ammoIssuedTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Remove Ammunition", "Select a row from the issued list.");
            return;
        }
        if (selected.id() < 0) {
            pendingAmmoIssuesByTempId.remove(selected.id());
        } else if (pendingAmmoReturnIds.contains(selected.id())) {
            pendingAmmoReturnIds.remove(selected.id());
        } else if (pendingAmmoExpendedIds.contains(selected.id())) {
            pendingAmmoExpendedIds.remove(selected.id());
        } else {
            pendingAmmoExpendedIds.remove(selected.id());
            pendingAmmoReturnIds.add(selected.id());
        }
        refreshAmmunition();
    }

    @FXML
    public void onMarkAmmoExpended() {
        if (ammoIssuedTable == null) {
            return;
        }
        AmmoIssuanceDao.AmmoIssuanceRow selected = ammoIssuedTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Mark Expended", "Select a row from the issued list.");
            return;
        }
        if (selected.id() < 0) {
            UiAlerts.error("Mark Expended", "Save pending issues before marking expended.");
            return;
        }
        if (pendingAmmoExpendedIds.contains(selected.id())) {
            pendingAmmoExpendedIds.remove(selected.id());
        } else {
            pendingAmmoReturnIds.remove(selected.id());
            pendingAmmoExpendedIds.add(selected.id());
        }
        refreshAmmunition();
    }

    @FXML
    public void onSaveAmmoIssue() {
        Officer officer = ammoIssueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            UiAlerts.error("Save Ammunition", "Select an officer first.");
            return;
        }
        if (pendingAmmoIssuesByTempId.isEmpty() && pendingAmmoReturnIds.isEmpty()) {
            UiAlerts.error("Save Ammunition", "Nothing staged to save. Use Add to Issue or Remove Selected first.");
            return;
        }

        Integer userId = currentUserIdOrNull();
        List<Integer> newIssuanceIds = new ArrayList<>();
        for (PendingAmmoIssue pending : pendingAmmoIssuesByTempId.values()) {
            int newId = ammoIssuanceDao.issue(
                    pending.ammoId(),
                    officer.getOfficerId(),
                    pending.rounds(),
                    pending.reasonId(),
                    pending.unitCost(),
                    userId
            );
            if (newId < 0) {
                UiAlerts.error("Save Ammunition",
                    "Insufficient stock for " + safe(pending.typeName()) + " " + safe(pending.caliberName())
                                + ". Some changes were not saved.");
                break;
            }
            newIssuanceIds.add(newId);
        }
        for (Integer issuanceId : new ArrayList<>(pendingAmmoReturnIds)) {
            ammoIssuanceDao.returnIssuance(issuanceId, userId);
        }
        for (Integer issuanceId : new ArrayList<>(pendingAmmoExpendedIds)) {
            ammoIssuanceDao.markExpended(issuanceId, userId);
        }

        clearPendingAmmoChanges();
        refreshAmmunition();

        List<AmmoIssuanceDao.AmmoIssuanceRow> newlyIssued = ammoIssuanceDao.findByIds(newIssuanceIds);
        showAmmoSignOffSheet(officer, newlyIssued);
    }

    @FXML
    public void onPrintAmmoSignOff() {
        Officer officer = ammoIssueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            UiAlerts.error("Print Sign-Off", "Select an officer first.");
            return;
        }
        showAmmoSignOffSheet(officer, List.of());
    }

    private void showAmmoSignOffSheet(Officer officer,
                                      List<AmmoIssuanceDao.AmmoIssuanceRow> issuedThisAction) {
        List<AmmoIssuanceDao.AmmoIssuanceRow> currentIssued = ammoIssuanceDao.findActiveByOfficer(officer.getOfficerId());
        if (currentIssued.isEmpty() && (issuedThisAction == null || issuedThisAction.isEmpty())) {
            UiAlerts.info("Print Sign-Off", "Officer has no currently issued ammunition.");
            return;
        }
        String adminName = SessionManager.getCurrentUser() == null
                ? ""
                : SessionManager.getCurrentUser().getUsername();
        boolean opened = WindowsPrintPreview.openHtml(
                "quartermaster-ammo-signoff-",
                PrintHtmlFactory.buildAmmoIssuancePrintHtml(
                        getAgencyName(), adminName, officer, currentIssued, issuedThisAction)
        );
        if (!opened) {
            UiAlerts.info("Print Sign-Off",
                    "Unable to open the print preview window. Please check Windows print settings.");
        }
    }

    private AmmoStockFormData showAmmoStockDialog(AmmunitionDao.AmmoStockRecord existing) {
        Dialog<AmmoStockFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Ammunition Stock" : "Edit Ammunition Stock");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        List<AmmoTypeDefinitionDao.AmmoTypeDefRecord> allTypeDefs = ammoTypeDefinitionDao.findAll();
        ComboBox<AmmoTypeDefinitionDao.AmmoTypeDefRecord> typeDefCombo = new ComboBox<>(FXCollections.observableArrayList(allTypeDefs));
        typeDefCombo.setConverter(new StringConverter<>() {
            @Override public String toString(AmmoTypeDefinitionDao.AmmoTypeDefRecord r) { return r == null ? "" : r.displayLabel(); }
            @Override public AmmoTypeDefinitionDao.AmmoTypeDefRecord fromString(String s) { return null; }
        });
        typeDefCombo.setPrefWidth(280);

        ComboBox<LookupItem> storageCombo = new ComboBox<>(FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.STORAGE_LOCATIONS)));
        storageCombo.setEditable(true);
        enableLookupTypeAhead(storageCombo);

        if (existing != null) {
            allTypeDefs.stream().filter(r -> Objects.equals(r.id(), existing.typeDefId())).findFirst()
                    .ifPresent(typeDefCombo::setValue);
            selectLookupById(storageCombo, existing.storageLocationId());
        }

        TextField roundsField = new TextField(existing == null ? "0" : String.valueOf(existing.roundsOnHand()));
        TextField unitCostField = new TextField(existing == null || existing.unitCost() == null ? "" : String.format("%.2f", existing.unitCost()));
        TextField notesField = new TextField(existing == null || existing.notes() == null ? "" : existing.notes());
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "ARCHIVED"));
        statusCombo.getSelectionModel().select(existing == null || existing.status() == null ? "ACTIVE" : existing.status());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int row = 0;
        grid.add(new Label("Ammo Type"), 0, row);
        grid.add(typeDefCombo, 1, row++);
        grid.add(new Label("Rounds On Hand"), 0, row);
        grid.add(roundsField, 1, row++);
        grid.add(new Label("Unit Cost"), 0, row);
        grid.add(unitCostField, 1, row++);
        grid.add(new Label("Storage Location"), 0, row);
        grid.add(storageCombo, 1, row++);
        grid.add(new Label("Notes"), 0, row);
        grid.add(notesField, 1, row++);
        grid.add(new Label("Status"), 0, row);
        grid.add(statusCombo, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }

            AmmoTypeDefinitionDao.AmmoTypeDefRecord selectedTypeDef = typeDefCombo.getValue();
            if (selectedTypeDef == null) {
                UiAlerts.error("Ammunition Stock", "Select an ammo type. Add types first in the Ammo Types tab.");
                return null;
            }

            int rounds;
            try {
                rounds = Integer.parseInt(text(roundsField));
            } catch (NumberFormatException ex) {
                UiAlerts.error("Ammunition Stock", "Rounds must be a whole number.");
                return null;
            }
            if (rounds < 0) {
                UiAlerts.error("Ammunition Stock", "Rounds cannot be negative.");
                return null;
            }

            Double unitCost = null;
            String unitCostText = text(unitCostField);
            if (!unitCostText.isEmpty()) {
                try {
                    unitCost = Double.parseDouble(unitCostText);
                } catch (NumberFormatException ex) {
                    UiAlerts.error("Ammunition Stock", "Unit cost must be numeric.");
                    return null;
                }
                if (unitCost < 0) {
                    UiAlerts.error("Ammunition Stock", "Unit cost cannot be negative.");
                    return null;
                }
            }

            return new AmmoStockFormData(
                    selectedTypeDef.id(),
                    rounds,
                    unitCost,
                    selectedId(storageCombo),
                    text(notesField),
                    statusCombo.getValue()
            );
        });

        Optional<AmmoStockFormData> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }
        return result.get();
    }

    private void refreshLookupItems(LookupCategory category) {
        lookupEditorTitle.setText(category.getDisplayName());
        lookupItemList.setItems(FXCollections.observableArrayList(lookupDao.findAll(category)));
    }

    // ===== Uniform tab =====

    private void setupUniformTabs() {
        if (uniformStockTable == null) {
            return;
        }
        uniformItemColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().itemName())));
        uniformCategoryColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> c.getValue().category() == null ? "" : c.getValue().category().name()));
        uniformBrandColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().brand())));
        uniformModelColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().model())));
        uniformSizeColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().sizeName())));
        uniformQtyColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> String.valueOf(c.getValue().quantityOnHand())));
        uniformUnitCostColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> c.getValue().unitCost() == null ? "" : String.format("$%.2f", c.getValue().unitCost())));
        uniformSupplierColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().supplier())));
        uniformStorageColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().storageLocationName())));
        uniformConditionColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> c.getValue().condition() == null ? "" : c.getValue().condition().name()));
        uniformStatusColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> c.getValue().status() == null ? "" : c.getValue().status().name()));

        uniformIssueItemColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().itemName())));
        uniformIssueCategoryColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> c.getValue().category() == null ? "" : c.getValue().category().name()));
        uniformIssueBrandColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().brand())));
        uniformIssueModelColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().model())));
        uniformIssueSizeColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().sizeName())));
        uniformIssueQtyColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> String.valueOf(c.getValue().quantityOnHand())));
        uniformIssueStorageColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().storageLocationName())));

        uniformIssueOfficerCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Officer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getBadgeNumber() + ")");
            }
        });
        uniformIssueOfficerCombo.setButtonCell(uniformIssueOfficerCombo.getCellFactory().call(null));
        uniformIssueOfficerCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            clearPendingUniformChanges();
            refreshUniformIssuedToOfficer();
            refreshUniforms();
        });

        if (uniformIssuedTable != null) {
            uniformIssuedItemColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().itemName())));
            uniformIssuedSizeColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().sizeName())));
            uniformIssuedQtyColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> String.valueOf(c.getValue().quantity())));
            uniformIssuedCostColumn.setCellValueFactory(c -> Bindings.createStringBinding(() ->
                    c.getValue().unitCostAtIssue() == null ? "" : String.format("$%.2f", c.getValue().unitCostAtIssue())));
            uniformIssuedDateColumn.setCellValueFactory(c -> Bindings.createStringBinding(() ->
                    c.getValue().issuedAt() == null ? "" :
                            c.getValue().issuedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            uniformIssuedByColumn.setCellValueFactory(c -> Bindings.createStringBinding(() -> safe(c.getValue().status())));
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private void showUniformSubNav(UniformSubTab subTab) {
        moduleNavLabel.setText("Uniforms");
        Button stockButton = UiNavSupport.createSubNavButton("Uniform Stock", () -> switchUniformSubTab(UniformSubTab.STOCK));
        Button issueButton = UiNavSupport.createSubNavButton("Issue", () -> switchUniformSubTab(UniformSubTab.ISSUE));
        moduleNavBar.getChildren().setAll(moduleNavLabel, stockButton, issueButton);
        switchUniformSubTab(subTab);
    }

    private void switchUniformSubTab(UniformSubTab subTab) {
        List<Button> buttons = UiNavSupport.getSubNavButtons(moduleNavBar);
        UiNavSupport.clearActive(buttons);
        switch (subTab) {
            case STOCK -> {
                uniformModuleTitleLabel.setText("Uniform Stock");
                UiNavSupport.setPaneVisible(uniformStockPane, true);
                UiNavSupport.setPaneVisible(uniformIssuePane, false);
                UiNavSupport.activateButtonByIndex(buttons, 0);
            }
            case ISSUE -> {
                uniformModuleTitleLabel.setText("Issue Uniform");
                UiNavSupport.setPaneVisible(uniformStockPane, false);
                UiNavSupport.setPaneVisible(uniformIssuePane, true);
                UiNavSupport.activateButtonByIndex(buttons, 1);
            }
        }
        refreshUniforms();
    }

    private void refreshUniforms() {
        if (uniformStockTable == null) {
            return;
        }
        List<EquipmentDao.UniformStockRecord> rows = equipmentDao.findAllUniformStockRecords();
        uniformStockTable.setItems(FXCollections.observableArrayList(rows));

        // Subtract any pending (unsaved) issued quantities from displayed availability so users
        // can't over-allocate stock from the staging area.
        Map<Integer, Integer> pendingByUniformId = new HashMap<>();
        for (PendingUniformIssue pending : pendingUniformIssuesByTempId.values()) {
            pendingByUniformId.merge(pending.uniformId(), pending.quantity(), Integer::sum);
        }
        List<EquipmentDao.UniformStockRecord> available = rows.stream()
                .filter(r -> r.status() == EquipmentStatus.AVAILABLE)
                .map(r -> {
                    int reserved = pendingByUniformId.getOrDefault(r.uniformId(), 0);
                    int adjusted = r.quantityOnHand() - reserved;
                    if (adjusted == r.quantityOnHand()) {
                        return r;
                    }
                    return new EquipmentDao.UniformStockRecord(
                            r.uniformId(), r.itemName(), r.category(), r.brand(), r.model(),
                            r.sizeId(), r.sizeName(), Math.max(0, adjusted), r.unitCost(),
                            r.supplier(), r.storageLocationId(), r.storageLocationName(),
                            r.condition(), r.status()
                    );
                })
                .filter(r -> r.quantityOnHand() > 0)
                .collect(Collectors.toList());
        uniformIssueAvailableTable.setItems(FXCollections.observableArrayList(available));

        Officer previouslySelected = uniformIssueOfficerCombo.getSelectionModel().getSelectedItem();
        List<Officer> allOfficers = officerDao.findAll();
        uniformIssueOfficerCombo.setItems(FXCollections.observableArrayList(allOfficers));
        if (previouslySelected != null) {
            allOfficers.stream()
                    .filter(o -> o.getOfficerId() == previouslySelected.getOfficerId())
                    .findFirst()
                    .ifPresent(o -> uniformIssueOfficerCombo.getSelectionModel().select(o));
        }
        refreshUniformIssuedToOfficer();
    }

    private void refreshUniformIssuedToOfficer() {
        if (uniformIssuedTable == null) {
            return;
        }
        Officer officer = uniformIssueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            uniformIssuedTable.setItems(FXCollections.observableArrayList());
            if (uniformIssuedToOfficerLabel != null) {
                uniformIssuedToOfficerLabel.setText("Select an officer to view issued uniforms.");
            }
            return;
        }
        List<UniformIssuanceDao.UniformIssuanceRow> active = uniformIssuanceDao.findActiveByOfficer(officer.getOfficerId());
        List<UniformIssuanceDao.UniformIssuanceRow> display = new ArrayList<>(active.size() + pendingUniformIssuesByTempId.size());
        // Existing active rows; mark those staged for return.
        for (UniformIssuanceDao.UniformIssuanceRow row : active) {
            if (pendingUniformReturnIds.contains(row.id())) {
                display.add(new UniformIssuanceDao.UniformIssuanceRow(
                        row.id(), row.uniformId(), row.itemName(), row.category(), row.brand(),
                        row.model(), row.sizeName(), row.quantity(), row.unitCostAtIssue(),
                        row.issuedAt(), row.issuedByUsername(), row.returnedAt(), row.returnedByUsername(),
                        "PENDING RETURN", row.officerId(), row.officerName(), row.officerBadge()
                ));
            } else {
                display.add(row);
            }
        }
        // Pending new issues as synthetic rows (negative id).
        for (PendingUniformIssue pending : pendingUniformIssuesByTempId.values()) {
            display.add(new UniformIssuanceDao.UniformIssuanceRow(
                    pending.tempId(), pending.uniformId(), pending.itemName(), pending.category(),
                    pending.brand(), pending.model(), pending.sizeName(), pending.quantity(),
                    pending.unitCost(), null, null, null, null,
                    "PENDING ISSUE", officer.getOfficerId(), officer.getName(), officer.getBadgeNumber()
            ));
        }
        uniformIssuedTable.setItems(FXCollections.observableArrayList(display));
        if (uniformIssuedToOfficerLabel != null) {
            int activeQty = active.stream()
                    .filter(r -> !pendingUniformReturnIds.contains(r.id()))
                    .mapToInt(UniformIssuanceDao.UniformIssuanceRow::quantity).sum();
            int pendingAdd = pendingUniformIssuesByTempId.values().stream()
                    .mapToInt(PendingUniformIssue::quantity).sum();
            int pendingRet = pendingUniformReturnIds.size();
            String pendingTxt = (pendingAdd == 0 && pendingRet == 0)
                    ? ""
                    : String.format("  |  Pending: +%d to issue, %d to return", pendingAdd, pendingRet);
            uniformIssuedToOfficerLabel.setText(officer.getName() + " (" + officer.getBadgeNumber()
                    + ") - " + activeQty + " currently issued" + pendingTxt);
        }
    }

    private void clearPendingUniformChanges() {
        pendingUniformIssuesByTempId.clear();
        pendingUniformReturnIds.clear();
    }

    @FXML
    public void onAddUniformIssue() {
        Officer officer = uniformIssueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            UiAlerts.error("Issue Uniform", "Select an officer first.");
            return;
        }
        EquipmentDao.UniformStockRecord selected = uniformIssueAvailableTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Issue Uniform", "Select a uniform stock row.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(text(uniformIssueQtyField));
        } catch (NumberFormatException ex) {
            UiAlerts.error("Issue Uniform", "Quantity must be a whole number.");
            return;
        }
        if (qty <= 0) {
            UiAlerts.error("Issue Uniform", "Quantity must be greater than zero.");
            return;
        }
        if (qty > selected.quantityOnHand()) {
            UiAlerts.error("Issue Uniform", "Only " + selected.quantityOnHand() + " available (after pending).");
            return;
        }
        PendingUniformIssue pending = new PendingUniformIssue(
                nextPendingUniformIssueTempId--,
                selected.uniformId(),
                selected.itemName(),
                selected.category() == null ? "" : selected.category().name(),
                selected.brand(),
                selected.model(),
                selected.sizeName(),
                qty,
                selected.unitCost()
        );
        pendingUniformIssuesByTempId.put(pending.tempId(), pending);
        refreshUniforms();
    }

    @FXML
    public void onRemoveUniformIssue() {
        if (uniformIssuedTable == null) {
            return;
        }
        UniformIssuanceDao.UniformIssuanceRow selected = uniformIssuedTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Remove Uniform", "Select a row from the issued list.");
            return;
        }
        if (selected.id() < 0) {
            // Pending new issue - cancel it.
            pendingUniformIssuesByTempId.remove(selected.id());
        } else if (pendingUniformReturnIds.contains(selected.id())) {
            // Already staged for return - un-stage.
            pendingUniformReturnIds.remove(selected.id());
        } else {
            // Real active issuance - stage for return.
            pendingUniformReturnIds.add(selected.id());
        }
        refreshUniforms();
    }

    @FXML
    public void onSaveUniformIssue() {
        Officer officer = uniformIssueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            UiAlerts.error("Save Uniform", "Select an officer first.");
            return;
        }
        if (pendingUniformIssuesByTempId.isEmpty() && pendingUniformReturnIds.isEmpty()) {
            UiAlerts.error("Save Uniform", "Nothing staged to save. Use Add to Issue or Remove Selected first.");
            return;
        }

        Integer userId = currentUserIdOrNull();
        List<Integer> newIssuanceIds = new ArrayList<>();
        for (PendingUniformIssue pending : pendingUniformIssuesByTempId.values()) {
            int newId = uniformIssuanceDao.issue(
                    pending.uniformId(),
                    officer.getOfficerId(),
                    pending.quantity(),
                    pending.unitCost(),
                    userId
            );
            if (newId < 0) {
                UiAlerts.error("Save Uniform",
                        "Insufficient stock for " + pending.itemName() + ". Some changes were not saved.");
                break;
            }
            newIssuanceIds.add(newId);
        }
        for (Integer issuanceId : new ArrayList<>(pendingUniformReturnIds)) {
            uniformIssuanceDao.returnIssuance(issuanceId, userId);
        }

        clearPendingUniformChanges();
        refreshUniforms();

        List<UniformIssuanceDao.UniformIssuanceRow> newlyIssued = uniformIssuanceDao.findByIds(newIssuanceIds);
        showUniformSignOffSheet(officer, newlyIssued);
    }

    @FXML
    public void onPrintUniformSignOff() {
        Officer officer = uniformIssueOfficerCombo.getSelectionModel().getSelectedItem();
        if (officer == null) {
            UiAlerts.error("Print Sign-Off", "Select an officer first.");
            return;
        }
        showUniformSignOffSheet(officer, List.of());
    }

    private void showUniformSignOffSheet(Officer officer,
                                         List<UniformIssuanceDao.UniformIssuanceRow> issuedThisAction) {
        List<UniformIssuanceDao.UniformIssuanceRow> currentIssued =
                uniformIssuanceDao.findActiveByOfficer(officer.getOfficerId());
        if (currentIssued.isEmpty() && (issuedThisAction == null || issuedThisAction.isEmpty())) {
            UiAlerts.info("Print Sign-Off", "Officer has no currently issued uniforms.");
            return;
        }
        String adminName = SessionManager.getCurrentUser() == null
                ? ""
                : SessionManager.getCurrentUser().getUsername();
        boolean opened = WindowsPrintPreview.openHtml(
                "quartermaster-uniform-signoff-",
                PrintHtmlFactory.buildUniformIssuancePrintHtml(
                        getAgencyName(), adminName, officer, currentIssued, issuedThisAction)
        );
        if (!opened) {
            UiAlerts.info("Print Sign-Off",
                    "Unable to open the print preview window. Please check Windows print settings.");
        }
    }

    @FXML
    public void onAddUniformStock() {
        UniformStockFormData formData = showUniformStockDialog(null);
        if (formData == null) {
            return;
        }
        equipmentDao.insertUniformStock(
                formData.itemName(),
                formData.category(),
                formData.brand(),
                formData.model(),
                formData.sizeId(),
                formData.quantityOnHand(),
                formData.unitCost(),
                formData.supplier(),
                formData.storageLocationId(),
                formData.condition(),
                formData.status()
        );
        refreshUniforms();
    }

    @FXML
    public void onEditUniformStock() {
        EquipmentDao.UniformStockRecord selected = uniformStockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Uniform Stock", "Select a row to edit.");
            return;
        }
        UniformStockFormData formData = showUniformStockDialog(selected);
        if (formData == null) {
            return;
        }
        equipmentDao.updateUniformStock(
                selected.uniformId(),
                formData.itemName(),
                formData.category(),
                formData.brand(),
                formData.model(),
                formData.sizeId(),
                formData.quantityOnHand(),
                formData.unitCost(),
                formData.supplier(),
                formData.storageLocationId(),
                formData.condition(),
                formData.status()
        );
        refreshUniforms();
    }

    @FXML
    public void onDeleteUniformStock() {
        EquipmentDao.UniformStockRecord selected = uniformStockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.error("Uniform Stock", "Select a row to delete.");
            return;
        }
        equipmentDao.deleteUniformStock(selected.uniformId());
        refreshUniforms();
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

    private UniformStockFormData showUniformStockDialog(EquipmentDao.UniformStockRecord existing) {
        Dialog<UniformStockFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Uniform Stock" : "Edit Uniform Stock");
        UiAlerts.applyTheme(dialog);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField itemNameField = new TextField(existing == null ? "" : existing.itemName());
        ComboBox<UniformCategory> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(UniformCategory.values()));
        categoryCombo.getSelectionModel().select(existing == null ? UniformCategory.UNIFORM : existing.category());
        TextField brandField = new TextField(existing == null || existing.brand() == null ? "" : existing.brand());
        TextField modelField = new TextField(existing == null || existing.model() == null ? "" : existing.model());

        ComboBox<LookupItem> sizeCombo = new ComboBox<>(
                FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.UNIFORM_SIZES)));
        sizeCombo.setEditable(true);
        enableLookupTypeAhead(sizeCombo);
        if (existing != null) {
            selectLookupById(sizeCombo, existing.sizeId());
        }

        TextField quantityField = new TextField(existing == null ? "1" : String.valueOf(existing.quantityOnHand()));
        TextField unitCostField = new TextField(existing == null || existing.unitCost() == null
                ? "" : String.format("%.2f", existing.unitCost()));
        TextField supplierField = new TextField(existing == null || existing.supplier() == null ? "" : existing.supplier());

        ComboBox<LookupItem> storageCombo = new ComboBox<>(
                FXCollections.observableArrayList(lookupDao.findAll(LookupCategory.STORAGE_LOCATIONS)));
        storageCombo.setEditable(true);
        enableLookupTypeAhead(storageCombo);
        if (existing != null) {
            selectLookupById(storageCombo, existing.storageLocationId());
        }

        ComboBox<EquipmentStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentStatus.values()));
        statusCombo.getSelectionModel().select(existing == null ? EquipmentStatus.AVAILABLE : existing.status());

        ComboBox<EquipmentCondition> conditionCombo = new ComboBox<>(FXCollections.observableArrayList(EquipmentCondition.values()));
        conditionCombo.getSelectionModel().select(existing == null ? EquipmentCondition.GOOD : existing.condition());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int row = 0;
        grid.add(new Label("Item Name"), 0, row);
        grid.add(itemNameField, 1, row++);
        grid.add(new Label("Category"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Brand"), 0, row);
        grid.add(brandField, 1, row++);
        grid.add(new Label("Model"), 0, row);
        grid.add(modelField, 1, row++);
        grid.add(new Label("Size"), 0, row);
        grid.add(sizeCombo, 1, row++);
        grid.add(new Label("Quantity On Hand"), 0, row);
        grid.add(quantityField, 1, row++);
        grid.add(new Label("Unit Cost"), 0, row);
        grid.add(unitCostField, 1, row++);
        grid.add(new Label("Supplier"), 0, row);
        grid.add(supplierField, 1, row++);
        grid.add(new Label("Storage Location"), 0, row);
        grid.add(storageCombo, 1, row++);
        grid.add(new Label("Status"), 0, row);
        grid.add(statusCombo, 1, row++);
        grid.add(new Label("Condition"), 0, row);
        grid.add(conditionCombo, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }

            int qty;
            try {
                qty = Integer.parseInt(text(quantityField));
            } catch (NumberFormatException ex) {
                UiAlerts.error("Uniform Stock", "Quantity must be a whole number.");
                return null;
            }
            if (qty < 0) {
                UiAlerts.error("Uniform Stock", "Quantity cannot be negative.");
                return null;
            }

            Double unitCost = null;
            String unitCostText = text(unitCostField);
            if (!unitCostText.isEmpty()) {
                try {
                    unitCost = Double.parseDouble(unitCostText);
                } catch (NumberFormatException ex) {
                    UiAlerts.error("Uniform Stock", "Unit cost must be numeric.");
                    return null;
                }
                if (unitCost < 0) {
                    UiAlerts.error("Uniform Stock", "Unit cost cannot be negative.");
                    return null;
                }
            }

            String itemName = text(itemNameField);
            if (itemName.isEmpty()) {
                UiAlerts.error("Uniform Stock", "Item name is required.");
                return null;
            }

            if (categoryCombo.getValue() == null || statusCombo.getValue() == null || conditionCombo.getValue() == null) {
                UiAlerts.error("Uniform Stock", "Category, status, and condition are required.");
                return null;
            }

            return new UniformStockFormData(
                    itemName,
                    categoryCombo.getValue(),
                    text(brandField),
                    text(modelField),
                    selectedId(sizeCombo),
                    qty,
                    unitCost,
                    text(supplierField),
                    selectedId(storageCombo),
                    conditionCombo.getValue(),
                    statusCombo.getValue()
            );
        });

        Optional<UniformStockFormData> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private String resolveUniformItemName(EquipmentFormData formData) {
        if (formData.equipmentTypeName() != null && !formData.equipmentTypeName().isBlank()) {
            return formData.equipmentTypeName().trim();
        }
        if (formData.name() != null && !formData.name().isBlank()) {
            return formData.name().trim();
        }
        return "Uniform Item";
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
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(LookupItem item) {
                return item == null || item.getName() == null ? "" : item.getName();
            }

            @Override
            public LookupItem fromString(String string) {
                if (string == null || string.isBlank()) {
                    return null;
                }
                String query = string.trim();
                return comboBox.getItems().stream()
                        .filter(item -> item.getName() != null)
                        .filter(item -> item.getName().trim().equalsIgnoreCase(query))
                        .findFirst()
                        .orElse(null);
            }
        });

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

    private record UniformStockFormData(String itemName,
                                        UniformCategory category,
                                        String brand,
                                        String model,
                                        Integer sizeId,
                                        int quantityOnHand,
                                        Double unitCost,
                                        String supplier,
                                        Integer storageLocationId,
                                        EquipmentCondition condition,
                                        EquipmentStatus status) {
    }

    private record AmmoStockFormData(Integer typeDefId,
                                     int roundsOnHand,
                                     Double unitCost,
                                     Integer storageLocationId,
                                     String notes,
                                     String status) {
    }

}
