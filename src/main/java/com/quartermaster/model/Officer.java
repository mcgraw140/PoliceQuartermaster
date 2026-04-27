package com.quartermaster.model;

public class Officer {
    private final int officerId;
    private final String name;
    private final String rank;
    private final String badgeNumber;

    public Officer(int officerId, String name, String rank, String badgeNumber) {
        this.officerId = officerId;
        this.name = name;
        this.rank = rank;
        this.badgeNumber = badgeNumber;
    }

    public int getOfficerId() {
        return officerId;
    }

    public String getName() {
        return name;
    }

    public String getRank() {
        return rank;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    @Override
    public String toString() {
        return name + " (" + badgeNumber + ")";
    }
}
