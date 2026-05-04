package com.quartermaster.controller;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public final class UiNavSupport {
    private UiNavSupport() {
    }

    public static Button createSubNavButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-tab");
        button.setOnAction(event -> action.run());
        return button;
    }

    public static List<Button> getSubNavButtons(HBox moduleNavBar) {
        List<Button> buttons = new ArrayList<>();
        for (javafx.scene.Node node : moduleNavBar.getChildren()) {
            if (node instanceof Button button) {
                buttons.add(button);
            }
        }
        return buttons;
    }

    public static void clearActive(List<Button> buttons) {
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }
    }

    public static void activateButtonByIndex(List<Button> buttons, int index) {
        if (index >= 0 && index < buttons.size()) {
            buttons.get(index).getStyleClass().add("active");
        }
    }

    public static void setModuleNavMessage(HBox moduleNavBar, Label moduleNavLabel, String text) {
        moduleNavLabel.setText(text);
        moduleNavBar.getChildren().setAll(moduleNavLabel);
    }

    public static void setPaneVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    public static void clearActive(ObservableList<Button> buttons) {
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }
    }
}
