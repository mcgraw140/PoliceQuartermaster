package com.quartermaster.model;

/**
 * Top-level inventory bucket. Replaces the recursive equipment_categories
 * branchKey concept with a fixed, schema-level enum.
 */
public enum EquipmentBranch {
    WEAPON("Weapons"),
    EQUIPMENT("Equipment"),
    UNIFORM("Uniforms");

    private final String displayName;

    EquipmentBranch(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EquipmentBranch fromName(String name) {
        if (name == null) {
            return EQUIPMENT;
        }
        try {
            return EquipmentBranch.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return EQUIPMENT;
        }
    }
}
