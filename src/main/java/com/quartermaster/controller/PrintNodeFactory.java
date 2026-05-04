package com.quartermaster.controller;

import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.IssuanceAdminRow;
import com.quartermaster.model.IssuedItemRow;
import com.quartermaster.model.Officer;
import com.quartermaster.model.Vehicle;
import com.quartermaster.model.VehicleMaintenanceLog;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PrintNodeFactory {
    private PrintNodeFactory() {
    }

    public static VBox buildVehicleMaintenanceHistoryPrintableNode(String agencyName,
                                                                   Vehicle vehicle,
                                                                   List<VehicleMaintenanceLog> logs,
                                                                   double totalCost) {
        Label title = new Label(agencyName);
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

    public static VBox buildEquipmentHistoryPrintableNode(String agencyName,
                                                          EquipmentItem item,
                                                          List<IssuanceAdminRow> history) {
        Label title = new Label(agencyName);
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

    public static VBox buildIssuancePrintableNode(String agencyName,
                                                  String adminName,
                                                  Officer officer,
                                                  List<IssuedItemRow> currentIssued,
                                                  List<IssuedItemRow> returnedItems) {
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
}
