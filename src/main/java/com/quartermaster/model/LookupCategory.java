package com.quartermaster.model;

/**
 * Catalog of lookup tables exposed through the Lookup Admin UI.
 * Each entry pairs a friendly display label with the underlying SQL table.
 */
public enum LookupCategory {
    EQUIPMENT_TYPES("Equipment Types", "equipment_types"),
    UNIFORM_ITEMS("Uniform Items", "uniform_items"),
    WEAPON_TYPES("Weapon Types", "weapon_types"),
    CALIBERS("Calibers", "calibers"),
    UNIFORM_SIZES("Uniform Sizes", "uniform_sizes"),
    VEHICLE_TYPES("Vehicle Types", "vehicle_types"),
    STORAGE_LOCATIONS("Storage Locations", "storage_locations"),
    AMMO_REASONS("Ammo Reasons", "ammo_reasons");

    private final String displayName;
    private final String tableName;

    LookupCategory(String displayName, String tableName) {
        this.displayName = displayName;
        this.tableName = tableName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
