package com.quartermaster.model;

import java.time.LocalDate;

public class VehicleMaintenanceLog {
    private final int logId;
    private final int vehicleId;
    private final LocalDate logDate;
    private final Integer mileage;
    private final String description;
    private final String performedBy;

    public VehicleMaintenanceLog(int logId, int vehicleId, LocalDate logDate, Integer mileage,
                                 String description, String performedBy) {
        this.logId = logId;
        this.vehicleId = vehicleId;
        this.logDate = logDate;
        this.mileage = mileage;
        this.description = description;
        this.performedBy = performedBy;
    }

    public int getLogId() {
        return logId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public Integer getMileage() {
        return mileage;
    }

    public String getDescription() {
        return description;
    }

    public String getPerformedBy() {
        return performedBy;
    }
}
