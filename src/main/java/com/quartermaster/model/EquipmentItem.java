package com.quartermaster.model;

public class EquipmentItem {
    private final int itemId;
    private final String name;
    private final EquipmentCategory category;
    private final String serialNumber;
    private final EquipmentCondition condition;
    private final EquipmentStatus status;

    public EquipmentItem(int itemId, String name, EquipmentCategory category, String serialNumber,
                         EquipmentCondition condition, EquipmentStatus status) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.serialNumber = serialNumber;
        this.condition = condition;
        this.status = status;
    }

    public int getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public EquipmentCategory getCategory() {
        return category;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public EquipmentCondition getCondition() {
        return condition;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}
