package com.quartermaster.controller;

import com.quartermaster.Main;
import com.quartermaster.auth.AuthenticatedUser;
import com.quartermaster.auth.SessionManager;
import com.quartermaster.auth.UserRole;
import com.quartermaster.dao.IssuanceDao;
import com.quartermaster.model.IssuedItemRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

public class MyEquipmentController {
    @FXML
    private TableView<IssuedItemRow> myEquipmentTable;

    @FXML
    private TableColumn<IssuedItemRow, String> itemNameColumn;

    @FXML
    private TableColumn<IssuedItemRow, String> categoryColumn;

    @FXML
    private TableColumn<IssuedItemRow, String> serialColumn;

    @FXML
    private TableColumn<IssuedItemRow, String> issuedDateColumn;

    @FXML
    private TableColumn<IssuedItemRow, String> attachedToColumn;

    private final IssuanceDao issuanceDao = new IssuanceDao();

    @FXML
    public void initialize() {
        AuthenticatedUser currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.OFFICER || currentUser.getOfficerId() == null) {
            forceLogoutToLogin();
            return;
        }

        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getCategory().name()
        ));
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        issuedDateColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getIssuedDate().toString()
        ));
        attachedToColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    String attachedTo = cellData.getValue().getAttachedToWeapon();
                    return attachedTo == null ? "" : attachedTo;
                }
        ));

        refresh();
    }

    @FXML
    public void onRefresh() {
        refresh();
    }

    @FXML
    public void onLogout() {
        forceLogoutToLogin();
    }

    private void refresh() {
        AuthenticatedUser currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getOfficerId() == null) {
            return;
        }

        List<IssuedItemRow> rows = issuanceDao.findActiveIssuedItemsByOfficer(currentUser.getOfficerId());
        myEquipmentTable.setItems(FXCollections.observableArrayList(rows));
    }

    private void forceLogoutToLogin() {
        SessionManager.logout();
        try {
            Main.switchScene("/com/quartermaster/fxml/login.fxml", "Quartermaster Login", 420, 280);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to return to login", ex);
        }
    }
}
