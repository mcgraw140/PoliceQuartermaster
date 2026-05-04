package com.quartermaster.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WindowsPrintPreview {
    private WindowsPrintPreview() {
    }

    public static boolean openHtml(String filePrefix, String html) {
        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                return false;
            }

            Path tempHtml = Files.createTempFile(filePrefix, ".html");
            Files.writeString(tempHtml, html, StandardCharsets.UTF_8);
            tempHtml.toFile().deleteOnExit();
            desktop.browse(tempHtml.toUri());
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            UiAlerts.error("Print Preview", "Could not open Windows print preview. Falling back to print dialog.");
            return false;
        }
    }
}
