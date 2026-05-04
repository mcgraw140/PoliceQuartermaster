package com.quartermaster.controller;

import com.quartermaster.dao.VehicleMaintenanceLogDao;
import com.quartermaster.model.Vehicle;
import com.quartermaster.model.VehicleMaintenanceLog;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class FleetMaintenanceHistorySupport {
    private FleetMaintenanceHistorySupport() {
    }

    public static void showVehicleMaintenanceHistoryDialog(Vehicle vehicle,
                                                           VehicleMaintenanceLogDao maintenanceLogDao,
                                                           TableView<VehicleMaintenanceLog> maintenanceTable,
                                                           Supplier<String> agencyNameSupplier,
                                                           Supplier<Window> printOwnerSupplier) {
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
                    if (showMaintenanceLogDetailEditDeleteOption(vehicle, selected, maintenanceLogDao)) {
                        List<VehicleMaintenanceLog> refreshed = maintenanceLogDao.findByVehicle(vehicle.getVehicleId());
                        historyTable.setItems(FXCollections.observableArrayList(refreshed));
                        maintenanceTable.setItems(FXCollections.observableArrayList(refreshed));
                        totalLine.setText("Total Maintenance Cost: "
                                + String.format("$%.2f", maintenanceLogDao.findTotalCostByVehicle(vehicle.getVehicleId())));
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
                String agencyName = agencyNameSupplier.get();
                if (openVehicleMaintenanceHistoryPrintPreview(agencyName, vehicle, currentLogs, currentTotal)) {
                    continue;
                }
                VBox printable = PrintNodeFactory.buildVehicleMaintenanceHistoryPrintableNode(
                        agencyName,
                        vehicle,
                        currentLogs,
                        currentTotal
                );
                JavaFxPrintSupport.printNode(printOwnerSupplier.get(), printable);
            }
        }
    }

    private static boolean showMaintenanceLogDetailEditDeleteOption(Vehicle vehicle,
                                                                    VehicleMaintenanceLog log,
                                                                    VehicleMaintenanceLogDao maintenanceLogDao) {
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
            MaintenanceLogDialog.Result dialogResult = MaintenanceLogDialog.show(vehicle, log);
            if (dialogResult == null) {
                return false;
            }

            if (dialogResult.deleteRequested()) {
                maintenanceLogDao.delete(log.getLogId());
                UiAlerts.info("Maintenance", "Maintenance log deleted.");
                return true;
            }

            MaintenanceLogDialog.FormData formData = dialogResult.formData();
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

    private static boolean openVehicleMaintenanceHistoryPrintPreview(String agencyName,
                                                                     Vehicle vehicle,
                                                                     List<VehicleMaintenanceLog> logs,
                                                                     double totalCost) {
        return WindowsPrintPreview.openHtml(
                "quartermaster-vehicle-maintenance-",
                PrintHtmlFactory.buildVehicleMaintenanceHistoryHtml(agencyName, vehicle, logs, totalCost)
        );
    }
}
