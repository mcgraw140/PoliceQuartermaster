package com.quartermaster.controller;

import com.quartermaster.dao.VehicleDao;
import com.quartermaster.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

import java.util.List;

public final class FleetViewSupport {
    private FleetViewSupport() {
    }

    public static void refreshVehiclesAndMaintenanceOptions(VehicleDao vehicleDao,
                                                            TableView<Vehicle> vehiclesTable,
                                                            ComboBox<Vehicle> maintenanceVehicleCombo,
                                                            Runnable onMaintenanceVehicleChanged) {
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
        onMaintenanceVehicleChanged.run();
    }
}
