package com.quartermaster.model;

public class Vehicle {
    private final int vehicleId;
    private final String unitNumber;
    private final String make;
    private final String model;
    private final int year;
    private final String vin;
    private final String plateNumber;

    public Vehicle(int vehicleId, String unitNumber, String make, String model, int year, String vin, String plateNumber) {
        this.vehicleId = vehicleId;
        this.unitNumber = unitNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.plateNumber = plateNumber;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public String getVin() {
        return vin;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    @Override
    public String toString() {
        return unitNumber + " - " + make + " " + model;
    }
}
