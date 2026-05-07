package com.csit228.capstone.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Controls {

    private static Stage primaryStage;

    public static void switchScreen(String screen) throws IOException {
        Stage stage = getPrimaryStage();    // Gets the available primary stage set if there's one. This class should receive the correct stage on what the MainApplication class, our main launcher, has passed on it.
        if (stage == null) {
            throw new IllegalStateException("Primary stage has not been set.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(
                Controls.class.getResource("/com/csit228/capstone/view/" + screen),
                "Missing FXML resource: " + screen
        ));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setOnShown(event -> stage.centerOnScreen());
        stage.setTitle("TIX.org");
        stage.show();
    }

    public static void setPrimaryStage(Stage primaryStage) {
        Controls.primaryStage = primaryStage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static <T extends Control> void hide(T element){
        element.setManaged(false);
        element.setVisible(false);
    }

    public static <T extends Control> void show(T element){
        element.setManaged(true);
        element.setVisible(true);
    }
}
