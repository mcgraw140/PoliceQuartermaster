package com.quartermaster.controller;

import com.quartermaster.model.EquipmentBranch;
import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.EquipmentStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class InventoryViewSupport {
    private InventoryViewSupport() {
    }

    public static boolean matchesInventoryFilter(EquipmentItem item,
                                                 boolean showHistorical,
                                                 String selectedStatus,
                                                 String query) {
        if (!showHistorical && (item.getStatus() == EquipmentStatus.RETIRED || item.getStatus() == EquipmentStatus.DESTROYED)) {
            return false;
        }

        if (selectedStatus != null && !"All".equals(selectedStatus) && !selectedStatus.equals(item.getStatus().name())) {
            return false;
        }

        if (query == null || query.isBlank()) {
            return true;
        }

        String normalizedQuery = query.trim().toLowerCase();
        String serial = item.getSerialNumber() == null ? "" : item.getSerialNumber().toLowerCase();
        String location = item.getStorageLocationName() == null ? "" : item.getStorageLocationName().toLowerCase();
        return item.getName().toLowerCase().contains(normalizedQuery)
                || item.getCategorySummary().toLowerCase().contains(normalizedQuery)
                || serial.contains(normalizedQuery)
                || location.contains(normalizedQuery);
    }

    public static String buildInventorySummaryText(List<EquipmentItem> filtered,
                                                   int byBranchTotal,
                                                   EquipmentBranch activeEquipmentBranch) {
        String base = "Showing " + filtered.size() + " of " + byBranchTotal + " items";
        if (activeEquipmentBranch != EquipmentBranch.UNIFORM) {
            return base;
        }

        Map<String, Long> grouped = filtered.stream()
                .collect(Collectors.groupingBy(EquipmentItem::getCategorySummary, Collectors.counting()));
        if (grouped.isEmpty()) {
            return base;
        }

        String breakdown = grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> entry.getKey() + " x" + entry.getValue())
                .collect(Collectors.joining(" | "));
        return base + "\nUniform counts: " + breakdown;
    }
}
