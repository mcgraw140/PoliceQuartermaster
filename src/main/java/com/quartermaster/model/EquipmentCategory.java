package com.quartermaster.model;

public class EquipmentCategory {
    private final int categoryId;
    private final String name;
    private final Integer parentCategoryId;
    private final String systemKey;
    private final String branchKey;
    private final String categoryPath;

    public EquipmentCategory(int categoryId, String name, Integer parentCategoryId,
                             String systemKey, String branchKey, String categoryPath) {
        this.categoryId = categoryId;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.systemKey = systemKey;
        this.branchKey = branchKey;
        this.categoryPath = categoryPath;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public String getSystemKey() {
        return systemKey;
    }

    public String getBranchKey() {
        return branchKey;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public boolean isRootCategory() {
        return parentCategoryId == null;
    }

    public boolean isSystemCategory() {
        return systemKey != null && !systemKey.isBlank();
    }

    public boolean isWeapon() {
        return "WEAPON".equals(branchKey);
    }

    public boolean isWeaponAttachment() {
        return "ATTACHMENT".equals(branchKey);
    }

    @Override
    public String toString() {
        return categoryPath;
    }
}
