package com.quartermaster.controller;

import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.stage.Window;

public final class JavaFxPrintSupport {
    private JavaFxPrintSupport() {
    }

    public static boolean printNode(Window owner, Node node) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob == null) {
            UiAlerts.error("Print", "No printer is available.");
            return false;
        }
        if (!printerJob.showPrintDialog(owner)) {
            return false;
        }

        Printer printer = printerJob.getPrinter();
        PageLayout pageLayout = printer.createPageLayout(
                Paper.NA_LETTER,
                PageOrientation.PORTRAIT,
                Printer.MarginType.DEFAULT
        );
        printerJob.getJobSettings().setPageLayout(pageLayout);

        // Attach to a temporary scene to ensure CSS and layout are applied before measuring for print scale.
        Group printRoot = new Group(node);
        new javafx.scene.Scene(printRoot);
        printRoot.applyCss();
        printRoot.layout();

        double width = Math.max(1, node.getLayoutBounds().getWidth());
        double height = Math.max(1, node.getLayoutBounds().getHeight());
        double scaleX = pageLayout.getPrintableWidth() / width;
        double scaleY = pageLayout.getPrintableHeight() / height;
        double scale = Math.min(1.0, Math.min(scaleX, scaleY));

        Scale transform = new Scale(scale, scale);
        node.getTransforms().add(transform);
        boolean printed = printerJob.printPage(pageLayout, node);
        node.getTransforms().remove(transform);

        if (printed) {
            printerJob.endJob();
        }

        printRoot.getChildren().clear();
        return printed;
    }
}
