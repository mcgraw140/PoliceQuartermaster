package com.quartermaster.model;

public class EquipmentItem {
    private final int itemId;
    private final String name;
    private final EquipmentBranch branch;

    private final Integer equipmentTypeId;
    private final String equipmentTypeName;
    private final Integer weaponTypeId;
    private final String weaponTypeName;
    private final Integer caliberId;
    private final String caliberName;
    private final Integer sizeId;
    private final String sizeName;
    private final Integer storageLocationId;
    private final String storageLocationName;

    private final String serialNumber;
    private final Double replacementCost;
    private final EquipmentCondition condition;
    private final EquipmentStatus status;
    private final boolean attachment;

    public EquipmentItem(int itemId,
                         String name,
                         EquipmentBranch branch,
                         Integer equipmentTypeId, String equipmentTypeName,
                         Integer weaponTypeId, String weaponTypeName,
                         Integer caliberId, String caliberName,
                         Integer sizeId, String sizeName,
                         Integer storageLocationId, String storageLocationName,
                         String serialNumber,
                         Double replacementCost,
                         EquipmentCondition condition,
                         EquipmentStatus status,
                         boolean attachment) {
        this.itemId = itemId;
        this.name = name;
        this.branch = branch;
        this.equipmentTypeId = equipmentTypeId;
        this.equipmentTypeName = equipmentTypeName;
        this.weaponTypeId = weaponTypeId;
        this.weaponTypeName = weaponTypeName;
        this.caliberId = caliberId;
        this.caliberName = caliberName;
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.storageLocationId = storageLocationId;
        this.storageLocationName = storageLocationName;
        this.serialNumber = serialNumber;
        this.replacementCost = replacementCost;
        this.condition = condition;
        this.status = status;
        this.attachment = attachment;
    }

    public int getItemId() { return itemId; }
    public String getName() { return name; }
    public EquipmentBranch getBranch() { return branch; }
    public Integer getEquipmentTypeId() { return equipmentTypeId; }
    public String getEquipmentTypeName() { return equipmentTypeName; }
    public Integer getWeaponTypeId() { return weaponTypeId; }
    public String getWeaponTypeName() { return weaponTypeName; }
    public Integer getCaliberId() { return caliberId; }
    public String getCaliberName() { return caliberName; }
    public Integer getSizeId() { return sizeId; }
    public String getSizeName() { return sizeName; }
    public Integer getStorageLocationId() { return storageLocationId; }
    public String getStorageLocationName() { return storageLocationName; }
    public String getSerialNumber() { return serialNumber; }
    public Double getReplacementCost() { return replacementCost; }
    public EquipmentCondition getCondition() { return condition; }
    public EquipmentStatus getStatus() { return status; }
    public boolean isAttachment() { return attachment; }

    public boolean isWeapon() {
        return branch == EquipmentBranch.WEAPON;
    }

    /**
     * Friendly summary used by tables and combo cells in place of the
     * legacy category path.
     */
    public String getCategorySummary() {
        StringBuilder builder = new StringBuilder();
        switch (branch) {
            case WEAPON -> {
                if (weaponTypeName != null) {
                    builder.append(weaponTypeName);
                }
                if (caliberName != null) {
                    if (builder.length() > 0) builder.append(" \u00b7 ");
                    builder.append(caliberName);
                }
            }
            case UNIFORM -> {
                if (equipmentTypeName != null) {
                    builder.append(equipmentTypeName);
                }
                if (sizeName != null) {
                    if (builder.length() > 0) builder.append(" \u00b7 ");
                    builder.append("Size ").append(sizeName);
                }
            }
            case EQUIPMENT -> {
                if (equipmentTypeName != null) {
                    builder.append(equipmentTypeName);
                }
                if (attachment) {
                    if (builder.length() > 0) builder.append(" \u00b7 ");
                    builder.append("Attachment");
                }
            }
        }
        if (builder.length() == 0) {
            builder.append(branch.getDisplayName());
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return name + " (" + getCategorySummary() + ")";
    }
}
