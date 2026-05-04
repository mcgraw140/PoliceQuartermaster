package com.quartermaster.controller;

import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.IssuanceAdminRow;
import com.quartermaster.model.IssuedItemRow;
import com.quartermaster.model.Officer;
import com.quartermaster.model.Vehicle;
import com.quartermaster.model.VehicleMaintenanceLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PrintHtmlFactory {
    private PrintHtmlFactory() {
    }

    public static String buildVehicleMaintenanceHistoryHtml(String agencyName,
                                                            Vehicle vehicle,
                                                            List<VehicleMaintenanceLog> logs,
                                                            double totalCost) {
        String printedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder rows = new StringBuilder();
        for (VehicleMaintenanceLog log : logs) {
            rows.append("<tr>")
                    .append("<td>").append(escapeHtml(log.getLogDate() == null ? "" : log.getLogDate().toString())).append("</td>")
                    .append("<td>").append(escapeHtml(log.getMileage() == null ? "" : String.valueOf(log.getMileage()))).append("</td>")
                    .append("<td>").append(escapeHtml(log.getDescription())).append("</td>")
                    .append("<td>").append(escapeHtml(log.getPerformedBy())).append("</td>")
                    .append("<td>").append(escapeHtml(log.getCost() == null ? "" : String.format("$%.2f", log.getCost()))).append("</td>")
                    .append("</tr>");
        }

        return """
                <!doctype html>
                <html lang=\"en\">
                <head>
                  <meta charset=\"utf-8\" />
                  <title>Vehicle Maintenance History</title>
                  <style>
                    body { font-family: Segoe UI, Tahoma, Arial, sans-serif; margin: 20px; color: #111; }
                    h1 { margin: 0; font-size: 28px; }
                    h2 { margin: 6px 0 14px 0; font-size: 20px; }
                    .meta { margin: 4px 0; font-size: 14px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                    th, td { border: 1px solid #444; padding: 8px; text-align: left; font-size: 13px; }
                    th { background: #efefef; }
                  </style>
                </head>
                <body onload=\"setTimeout(function(){ window.print(); }, 200);\">
                  <h1>%s</h1>
                  <h2>Vehicle Maintenance History</h2>
                  <div class=\"meta\"><strong>Printed:</strong> %s</div>
                  <div class=\"meta\"><strong>Vehicle:</strong> %s - %s %s</div>
                  <div class=\"meta\"><strong>Type:</strong> %s</div>
                  <div class=\"meta\"><strong>VIN:</strong> %s</div>
                  <div class=\"meta\"><strong>Plate:</strong> %s</div>
                  <div class=\"meta\"><strong>Total Maintenance Cost:</strong> %s</div>
                  <table>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Mileage</th>
                        <th>Description</th>
                        <th>Performed By</th>
                        <th>Cost</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(agencyName),
                escapeHtml(printedAt),
                escapeHtml(vehicle.getUnitNumber()),
                escapeHtml(vehicle.getMake()),
                escapeHtml(vehicle.getModel()),
                escapeHtml(vehicle.getVehicleTypeName()),
                escapeHtml(vehicle.getVin()),
                escapeHtml(vehicle.getPlateNumber()),
                escapeHtml(String.format("$%.2f", totalCost)),
                rows
        );
    }

    public static String buildEquipmentHistoryHtml(String agencyName,
                                                   EquipmentItem item,
                                                   List<IssuanceAdminRow> history) {
        String printedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        StringBuilder rows = new StringBuilder();
        for (IssuanceAdminRow row : history) {
            rows.append("<tr>")
                    .append("<td>").append(escapeHtml(row.getOfficerName())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getIssuedByUsername())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getIssuedDate() == null ? "" : row.getIssuedDate().toString())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getReturnedByUsername())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getReturnedDate() == null ? "" : row.getReturnedDate().toString())).append("</td>")
                    .append("</tr>");
        }

        return """
                <!doctype html>
                <html lang=\"en\">
                <head>
                  <meta charset=\"utf-8\" />
                  <title>Equipment History</title>
                  <style>
                    body { font-family: Segoe UI, Tahoma, Arial, sans-serif; margin: 20px; color: #111; }
                    h1 { margin: 0; font-size: 28px; }
                    h2 { margin: 6px 0 14px 0; font-size: 20px; }
                    .meta { margin: 4px 0; font-size: 14px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                    th, td { border: 1px solid #444; padding: 8px; text-align: left; font-size: 13px; }
                    th { background: #efefef; }
                  </style>
                </head>
                <body onload=\"setTimeout(function(){ window.print(); }, 200);\">
                  <h1>%s</h1>
                  <h2>Equipment Item History</h2>
                  <div class=\"meta\"><strong>Printed:</strong> %s</div>
                  <div class=\"meta\"><strong>Item:</strong> %s</div>
                  <div class=\"meta\"><strong>Category:</strong> %s</div>
                  <div class=\"meta\"><strong>Serial:</strong> %s</div>
                  <div class=\"meta\"><strong>Replacement Cost:</strong> %s</div>
                  <table>
                    <thead>
                      <tr>
                        <th>Issued To</th>
                        <th>Issued By</th>
                        <th>Issued Date</th>
                        <th>Returned By</th>
                        <th>Returned Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>
                                    <p style=\"margin-top: 14px; font-size: 12px;\"><strong>Officer Responsibility:</strong> Officers are responsible for all issued items. Lost or damaged items will be replaced at the listed replacement cost.</p>
                </body>
                </html>
                """.formatted(
                escapeHtml(agencyName),
                escapeHtml(printedAt),
                escapeHtml(item.getName()),
                escapeHtml(item.getCategorySummary()),
                escapeHtml(item.getSerialNumber()),
                escapeHtml(item.getReplacementCost() == null ? "" : String.format("$%.2f", item.getReplacementCost())),
                rows
        );
    }

    public static String buildIssuancePrintHtml(String agencyName,
                                                String adminName,
                                                Officer officer,
                                                List<IssuedItemRow> currentIssued,
                                                List<IssuedItemRow> returnedItems) {
        String printedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder rows = new StringBuilder();
        for (IssuedItemRow row : currentIssued) {
            String issuedDate = row.getIssuedDate() == null ? "" : row.getIssuedDate().toString();
            String replacementCost = row.getReplacementCost() == null
                    ? ""
                    : String.format("$%.2f", row.getReplacementCost());
            rows.append("<tr>")
                    .append("<td>").append(escapeHtml(row.getItemName())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getCategorySummary())).append("</td>")
                    .append("<td>").append(escapeHtml(row.getSerialNumber())).append("</td>")
                    .append("<td>").append(escapeHtml(replacementCost)).append("</td>")
                    .append("<td>").append(escapeHtml(issuedDate)).append("</td>")
                    .append("</tr>");
        }

        StringBuilder returnedRows = new StringBuilder();
        if (returnedItems != null) {
            for (IssuedItemRow row : returnedItems) {
                String issuedDate = row.getIssuedDate() == null ? "" : row.getIssuedDate().toString();
                String replacementCost = row.getReplacementCost() == null
                        ? ""
                        : String.format("$%.2f", row.getReplacementCost());
                returnedRows.append("<tr>")
                        .append("<td>").append(escapeHtml(row.getItemName())).append("</td>")
                        .append("<td>").append(escapeHtml(row.getCategorySummary())).append("</td>")
                        .append("<td>").append(escapeHtml(row.getSerialNumber())).append("</td>")
                        .append("<td>").append(escapeHtml(replacementCost)).append("</td>")
                        .append("<td>").append(escapeHtml(issuedDate)).append("</td>")
                        .append("</tr>");
            }
        }

        String returnedSection = returnedRows.isEmpty()
                ? ""
                : """
                                    <h2 style=\"margin-top: 20px; margin-bottom: 6px; font-size: 20px;\">Returned Items (This Save)</h2>
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Name</th>
                                                <th>Category</th>
                                                <th>Serial</th>
                                                <th>Replacement Cost</th>
                                                <th>Originally Issued</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            %s
                                        </tbody>
                                    </table>
                                """.formatted(returnedRows);

        return """
                                <!doctype html>
                                <html lang=\"en\">
                                <head>
                                    <meta charset=\"utf-8\" />
                                    <title>%s - Officer Equipment Sign-Off</title>
                                    <style>
                                        body { font-family: Segoe UI, Tahoma, Arial, sans-serif; margin: 20px; color: #111; }
                                        h1 { margin: 0; font-size: 28px; }
                                        h2 { margin: 6px 0 10px 0; font-size: 20px; }
                                        .meta { margin: 4px 0; font-size: 14px; }
                                        table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                                        th, td { border: 1px solid #444; padding: 8px; text-align: left; font-size: 13px; }
                                        th { background: #efefef; }
                                        .sign { margin-top: 24px; font-size: 16px; }
                                        @media print {
                                            body { margin: 14px; }
                                        }
                                    </style>
                                </head>
                                <body onload=\"setTimeout(function(){ window.print(); }, 200);\">
                                    <h1>%s</h1>
                                    <h2>Equipment Issue / Return Sign-Off</h2>
                                    <div class=\"meta\"><strong>Printed:</strong> %s</div>
                                    <div class=\"meta\"><strong>Officer:</strong> %s (%s)</div>
                                    <div class=\"meta\"><strong>Prepared By:</strong> %s</div>
                                    <div class=\"meta\"><strong>Issued Items:</strong> %s</div>
                                    <div class=\"meta\"><strong>Returned Items:</strong> %s</div>
                                    <h2 style=\"margin-top: 16px; margin-bottom: 6px; font-size: 20px;\">Currently Issued</h2>
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Name</th>
                                                <th>Category</th>
                                                <th>Serial</th>
                                                <th>Replacement Cost</th>
                                                <th>Issued Date</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            %s
                                        </tbody>
                                    </table>
                                    %s
                                    <div class=\"sign\">Issued By (%s): ________________________________</div>
                                    <div class=\"sign\">Officer Signature (%s): ________________________________</div>
                                    <p style=\"margin-top: 14px; font-size: 12px;\"><strong>Officer Responsibility:</strong> Officers are responsible for all issued items. Lost or damaged items will be replaced at the listed replacement cost.</p>
                                </body>
                                </html>
                                """.formatted(
                escapeHtml(agencyName),
                escapeHtml(agencyName),
                escapeHtml(printedAt),
                escapeHtml(officer.getName()),
                escapeHtml(officer.getBadgeNumber()),
                escapeHtml(adminName),
                currentIssued.size(),
                returnedItems == null ? 0 : returnedItems.size(),
                rows,
                returnedSection,
                escapeHtml(adminName),
                escapeHtml(officer.getName())
        );
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
