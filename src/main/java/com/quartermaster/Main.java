package com.quartermaster;

import com.quartermaster.db.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            DatabaseInitializer.initialize();
        } catch (IllegalStateException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Database Not Ready");
            alert.setHeaderText("App started without database connection");
            alert.setContentText("Configure MariaDB in config.properties and restart. You can still open the app UI now.");
            alert.showAndWait();
        }
        primaryStage = stage;
        switchScene("/com/quartermaster/fxml/login.fxml", "Quartermaster Login", 420, 280);
        primaryStage.show();
    }

    public static void switchScene(String fxmlPath, String title, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(Main.class.getResource("/com/quartermaster/styles/dark-theme.css").toExternalForm());
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
