package com.quartermaster.controller;

import com.quartermaster.model.Vehicle;
import com.quartermaster.model.VehicleMaintenanceLog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

public final class MaintenanceLogDialog {
    private MaintenanceLogDialog() {
    }

    public static Result show(Vehicle vehicle, VehicleMaintenanceLog existing) {
        Dialog<Result> dialog = new Dialog<>();
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
                return new Result(true, null);
            }
            if (buttonType != saveButton) {
                return null;
            }

            Integer mileage = null;
            String mileageText = trim(mileageField);
            if (!mileageText.isEmpty()) {
                try {
                    mileage = Integer.parseInt(mileageText);
                } catch (NumberFormatException ex) {
                    UiAlerts.error("Maintenance", "Mileage must be a whole number.");
                    return null;
                }
            }

            Double cost = null;
            String costText = trim(costField);
            if (!costText.isEmpty()) {
                try {
                    cost = Double.parseDouble(costText);
                } catch (NumberFormatException ex) {
                    UiAlerts.error("Maintenance", "Cost must be numeric.");
                    return null;
                }
            }

            return new Result(
                    false,
                    new FormData(
                            datePicker.getValue(),
                            mileage,
                            cost,
                            trim(descriptionArea),
                            trim(byField)
                    )
            );
        });

        Optional<Result> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        Result dialogResult = result.get();
        if (dialogResult == null || dialogResult.deleteRequested()) {
            return dialogResult;
        }

        FormData formData = dialogResult.formData();
        if (formData == null || formData.logDate() == null || formData.description().isEmpty()) {
            UiAlerts.error("Maintenance", "Date and description are required.");
            return null;
        }
        return dialogResult;
    }

    private static String trim(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private static String trim(TextArea textArea) {
        return textArea.getText() == null ? "" : textArea.getText().trim();
    }

    public record FormData(LocalDate logDate, Integer mileage, Double cost, String description, String performedBy) {
    }

    public record Result(boolean deleteRequested, FormData formData) {
    }
}
