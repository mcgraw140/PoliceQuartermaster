package com.quartermaster.controller;

import com.quartermaster.model.EquipmentCondition;
import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.EquipmentStatus;
import com.quartermaster.model.IssuedItemRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class IssueWorkflowSupport {
    private IssueWorkflowSupport() {
    }

    public static List<EquipmentItem> selectedOrFocusedAvailableRows(TableView<EquipmentItem> issueAvailableTable) {
        List<EquipmentItem> selected = new ArrayList<>(issueAvailableTable.getSelectionModel().getSelectedItems());
        if (!selected.isEmpty()) {
            return selected;
        }

        EquipmentItem single = issueAvailableTable.getSelectionModel().getSelectedItem();
        if (single != null) {
            return List.of(single);
        }

        int focusedIndex = issueAvailableTable.getFocusModel().getFocusedIndex();
        if (focusedIndex >= 0 && focusedIndex < issueAvailableTable.getItems().size()) {
            EquipmentItem focused = issueAvailableTable.getItems().get(focusedIndex);
            return focused == null ? List.of() : List.of(focused);
        }
        return List.of();
    }

    public static List<IssuedItemRow> selectedOrFocusedIssuedRows(TableView<IssuedItemRow> issueSelectedTable) {
        List<IssuedItemRow> selected = new ArrayList<>(issueSelectedTable.getSelectionModel().getSelectedItems());
        if (!selected.isEmpty()) {
            return selected;
        }

        IssuedItemRow single = issueSelectedTable.getSelectionModel().getSelectedItem();
        if (single != null) {
            return List.of(single);
        }

        int focusedIndex = issueSelectedTable.getFocusModel().getFocusedIndex();
        if (focusedIndex >= 0 && focusedIndex < issueSelectedTable.getItems().size()) {
            IssuedItemRow focused = issueSelectedTable.getItems().get(focusedIndex);
            return focused == null ? List.of() : List.of(focused);
        }
        return List.of();
    }

    public static void applyIssueAvailableFilter(FilteredList<EquipmentItem> issueAvailableFiltered, String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        issueAvailableFiltered.setPredicate(item -> {
            if (normalizedQuery.isEmpty()) {
                return true;
            }
            String serial = item.getSerialNumber() == null ? "" : item.getSerialNumber().toLowerCase();
            return item.getName().toLowerCase().contains(normalizedQuery)
                    || item.getCategorySummary().toLowerCase().contains(normalizedQuery)
                    || serial.contains(normalizedQuery);
        });
    }

    public static void stageIssue(EquipmentItem item,
                                  LocalDate issueDate,
                                  Map<Integer, EquipmentItem> pendingIssueItemsById,
                                  Map<Integer, IssuedItemRow> pendingIssuedRowsByItemId,
                                  ObservableList<EquipmentItem> issueAvailableSource,
                                  ObservableList<IssuedItemRow> issuedToOfficerItems) {
        pendingIssueItemsById.put(item.getItemId(), item);
        IssuedItemRow staged = new IssuedItemRow(
                -item.getItemId(),
                item.getName(),
                item.getBranch(),
                item.getCategorySummary(),
                item.getSerialNumber(),
                item.getReplacementCost(),
                issueDate == null ? LocalDate.now() : issueDate,
                null
        );
        pendingIssuedRowsByItemId.put(item.getItemId(), staged);
        issueAvailableSource.remove(item);
        issuedToOfficerItems.add(staged);
    }

    public static void cancelStagedIssue(int stagedIssuanceId,
                                         Map<Integer, EquipmentItem> pendingIssueItemsById,
                                         Map<Integer, IssuedItemRow> pendingIssuedRowsByItemId,
                                         ObservableList<EquipmentItem> issueAvailableSource,
                                         ObservableList<IssuedItemRow> issuedToOfficerItems) {
        int itemId = -stagedIssuanceId;
        EquipmentItem original = pendingIssueItemsById.remove(itemId);
        pendingIssuedRowsByItemId.remove(itemId);

        issuedToOfficerItems.removeIf(row -> row.getIssuanceId() == stagedIssuanceId);
        if (original != null) {
            issueAvailableSource.add(original);
            sortIssueAvailableSource(issueAvailableSource);
        }
    }

    public static void stageReturn(IssuedItemRow row,
                                   Map<Integer, IssuedItemRow> pendingReturnRowsByIssuanceId,
                                   Set<Integer> pendingReturnIssuanceIds,
                                   ObservableList<EquipmentItem> issueAvailableSource,
                                   ObservableList<IssuedItemRow> issuedToOfficerItems) {
        pendingReturnIssuanceIds.add(row.getIssuanceId());
        pendingReturnRowsByIssuanceId.put(row.getIssuanceId(), row);
        issuedToOfficerItems.remove(row);
        issueAvailableSource.add(toPendingAvailablePlaceholder(row));
        sortIssueAvailableSource(issueAvailableSource);
    }

    public static void stageGroupedReturn(IssuedItemRow groupedRow,
                                          Map<Integer, IssuedItemRow> pendingReturnRowsByIssuanceId,
                                          Set<Integer> pendingReturnIssuanceIds,
                                          ObservableList<EquipmentItem> issueAvailableSource,
                                          ObservableList<IssuedItemRow> issuedToOfficerItems) {
        issuedToOfficerItems.remove(groupedRow);
        for (Integer issuanceId : groupedRow.getIssuanceIds()) {
            IssuedItemRow singleRow = groupedRow.asSingleIssuance(issuanceId);
            pendingReturnIssuanceIds.add(issuanceId);
            pendingReturnRowsByIssuanceId.put(issuanceId, singleRow);
            issueAvailableSource.add(toPendingAvailablePlaceholder(singleRow));
        }
        sortIssueAvailableSource(issueAvailableSource);
    }

    public static void cancelStagedReturn(int issuanceId,
                                          Map<Integer, IssuedItemRow> pendingReturnRowsByIssuanceId,
                                          Set<Integer> pendingReturnIssuanceIds,
                                          ObservableList<EquipmentItem> issueAvailableSource,
                                          ObservableList<IssuedItemRow> issuedToOfficerItems) {
        IssuedItemRow row = pendingReturnRowsByIssuanceId.remove(issuanceId);
        pendingReturnIssuanceIds.remove(issuanceId);
        issueAvailableSource.removeIf(item -> item.getItemId() == -issuanceId);
        if (row != null) {
            issuedToOfficerItems.add(row);
        }
    }

    public static EquipmentItem toPendingAvailablePlaceholder(IssuedItemRow row) {
        return new EquipmentItem(
                -row.getIssuanceId(),
                row.getItemName(),
                row.getBranch(),
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                row.getSerialNumber(),
                row.getReplacementCost(),
                EquipmentCondition.GOOD,
                EquipmentStatus.AVAILABLE,
                false
        );
    }

    public static void sortIssueAvailableSource(ObservableList<EquipmentItem> issueAvailableSource) {
        FXCollections.sort(issueAvailableSource, Comparator.comparing(EquipmentItem::getName, String.CASE_INSENSITIVE_ORDER));
    }

    public static void clearPendingChanges(Map<Integer, EquipmentItem> pendingIssueItemsById,
                                           Map<Integer, IssuedItemRow> pendingIssuedRowsByItemId,
                                           Map<Integer, IssuedItemRow> pendingReturnRowsByIssuanceId,
                                           Set<Integer> pendingReturnIssuanceIds) {
        pendingIssueItemsById.clear();
        pendingIssuedRowsByItemId.clear();
        pendingReturnRowsByIssuanceId.clear();
        pendingReturnIssuanceIds.clear();
    }
}
