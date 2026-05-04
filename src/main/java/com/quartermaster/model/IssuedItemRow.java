package com.quartermaster.model;

import java.time.LocalDate;
import java.util.List;

public class IssuedItemRow {
    private final int issuanceId;
    private final List<Integer> issuanceIds;
    private final int quantity;
    private final String itemName;
    private final EquipmentBranch branch;
    private final String categorySummary;
    private final String serialNumber;
    private final Double replacementCost;
    private final LocalDate issuedDate;
    private final String attachedToWeapon;

    public IssuedItemRow(int issuanceId, String itemName, EquipmentBranch branch, String categorySummary,
                         String serialNumber, Double replacementCost, LocalDate issuedDate, String attachedToWeapon) {
        this(issuanceId, List.of(issuanceId), 1, itemName, branch, categorySummary, serialNumber, replacementCost, issuedDate, attachedToWeapon);
    }

    public IssuedItemRow(int issuanceId,
                         List<Integer> issuanceIds,
                         int quantity,
                         String itemName,
                         EquipmentBranch branch,
                         String categorySummary,
                         String serialNumber,
                         Double replacementCost,
                         LocalDate issuedDate,
                         String attachedToWeapon) {
        this.issuanceId = issuanceId;
        this.issuanceIds = issuanceIds;
        this.quantity = quantity;
        this.itemName = itemName;
        this.branch = branch;
        this.categorySummary = categorySummary;
        this.serialNumber = serialNumber;
        this.replacementCost = replacementCost;
        this.issuedDate = issuedDate;
        this.attachedToWeapon = attachedToWeapon;
    }

    public int getIssuanceId() { return issuanceId; }
    public List<Integer> getIssuanceIds() { return issuanceIds; }
    public int getQuantity() { return quantity; }
    public String getItemName() { return itemName; }
    public EquipmentBranch getBranch() { return branch; }
    public String getCategorySummary() { return categorySummary; }
    public String getSerialNumber() { return serialNumber; }
    public String getSerialOrQty() { return quantity > 1 ? String.valueOf(quantity) : (serialNumber == null ? "" : serialNumber); }
    public Double getReplacementCost() { return replacementCost; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public String getAttachedToWeapon() { return attachedToWeapon; }

    public IssuedItemRow asSingleIssuance(int singleIssuanceId) {
        return new IssuedItemRow(
                singleIssuanceId,
                List.of(singleIssuanceId),
                1,
                itemName,
                branch,
                categorySummary,
                serialNumber,
                replacementCost,
                issuedDate,
                attachedToWeapon
        );
    }
}
