package com.quartermaster.model;

import java.time.LocalDate;

public class IssuedItemRow {
    private final int issuanceId;
    private final String itemName;
    private final EquipmentCategory category;
    private final String serialNumber;
    private final LocalDate issuedDate;
    private final String attachedToWeapon;

    public IssuedItemRow(int issuanceId, String itemName, EquipmentCategory category, String serialNumber,
                         LocalDate issuedDate, String attachedToWeapon) {
        this.issuanceId = issuanceId;
        this.itemName = itemName;
        this.category = category;
        this.serialNumber = serialNumber;
        this.issuedDate = issuedDate;
        this.attachedToWeapon = attachedToWeapon;
    }

    public int getIssuanceId() {
        return issuanceId;
    }

    public String getItemName() {
        return itemName;
    }

    public EquipmentCategory getCategory() {
        return category;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public String getAttachedToWeapon() {
        return attachedToWeapon;
    }
}
