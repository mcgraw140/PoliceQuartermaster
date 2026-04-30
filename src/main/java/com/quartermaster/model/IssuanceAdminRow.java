package com.quartermaster.model;

import java.time.LocalDate;

public class IssuanceAdminRow {
    private final int issuanceId;
    private final int officerId;
    private final String officerName;
    private final int itemId;
    private final String itemName;
    private final String branchName;
    private final String categorySummary;
    private final String serialNumber;
    private final Double replacementCost;
    private final String itemCondition;
    private final String itemStatus;
    private final String issuedByUsername;
    private final String returnedByUsername;
    private final LocalDate issuedDate;
    private final LocalDate returnedDate;

    public IssuanceAdminRow(int issuanceId, int officerId, String officerName, int itemId,
                            String itemName, String branchName, String categorySummary,
                            String serialNumber, Double replacementCost, String itemCondition,
                            String itemStatus, String issuedByUsername, String returnedByUsername,
                            LocalDate issuedDate, LocalDate returnedDate) {
        this.issuanceId = issuanceId;
        this.officerId = officerId;
        this.officerName = officerName;
        this.itemId = itemId;
        this.itemName = itemName;
        this.branchName = branchName;
        this.categorySummary = categorySummary;
        this.serialNumber = serialNumber;
        this.replacementCost = replacementCost;
        this.itemCondition = itemCondition;
        this.itemStatus = itemStatus;
        this.issuedByUsername = issuedByUsername;
        this.returnedByUsername = returnedByUsername;
        this.issuedDate = issuedDate;
        this.returnedDate = returnedDate;
    }

    public int getIssuanceId() {
        return issuanceId;
    }

    public int getOfficerId() {
        return officerId;
    }

    public String getOfficerName() {
        return officerName;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getCategorySummary() {
        return categorySummary;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Double getReplacementCost() {
        return replacementCost;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public String getIssuedByUsername() {
        return issuedByUsername;
    }

    public String getReturnedByUsername() {
        return returnedByUsername;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }
}
