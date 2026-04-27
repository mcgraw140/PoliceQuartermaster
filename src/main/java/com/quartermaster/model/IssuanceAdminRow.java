package com.quartermaster.model;

import java.time.LocalDate;

public class IssuanceAdminRow {
    private final int issuanceId;
    private final int officerId;
    private final String officerName;
    private final int itemId;
    private final String itemName;
    private final LocalDate issuedDate;
    private final LocalDate returnedDate;

    public IssuanceAdminRow(int issuanceId, int officerId, String officerName, int itemId,
                            String itemName, LocalDate issuedDate, LocalDate returnedDate) {
        this.issuanceId = issuanceId;
        this.officerId = officerId;
        this.officerName = officerName;
        this.itemId = itemId;
        this.itemName = itemName;
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

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }
}
