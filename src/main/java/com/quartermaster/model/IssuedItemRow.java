package com.quartermaster.model;

import java.time.LocalDate;

public class IssuedItemRow {
    private final int issuanceId;
    private final String itemName;
    private final EquipmentBranch branch;
    private final String categorySummary;
    private final String serialNumber;
    private final Double replacementCost;
    private final LocalDate issuedDate;
    private final String attachedToWeapon;

    public IssuedItemRow(int issuanceId, String itemName, EquipmentBranch branch, String categorySummary,
                         String serialNumber, Double replacementCost, LocalDate issuedDate, String attachedToWeapon) {
        this.issuanceId = issuanceId;
        this.itemName = itemName;
        this.branch = branch;
        this.categorySummary = categorySummary;
        this.serialNumber = serialNumber;
        this.replacementCost = replacementCost;
        this.issuedDate = issuedDate;
        this.attachedToWeapon = attachedToWeapon;
    }

    public int getIssuanceId() { return issuanceId; }
    public String getItemName() { return itemName; }
    public EquipmentBranch getBranch() { return branch; }
    public String getCategorySummary() { return categorySummary; }
    public String getSerialNumber() { return serialNumber; }
    public Double getReplacementCost() { return replacementCost; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public String getAttachedToWeapon() { return attachedToWeapon; }
}
