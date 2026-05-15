package com.csit228.capstone.utils;

import java.io.IOException;
import java.util.Objects;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;

public class Controls {

  private static Stage primaryStage;

  public static void switchScreen(String screen) throws IOException {
    Stage stage = getPrimaryStage();
    if (stage == null) {
      throw new IllegalStateException("Primary stage has not been set.");
    }

    FXMLLoader fxmlLoader = new FXMLLoader(
      Objects.requireNonNull(
        Controls.class.getResource("/com/csit228/capstone/view/" + screen),
        "Missing FXML resource: " + screen
      )
    );

    Scene scene = new Scene(fxmlLoader.load());

    stage.setTitle("TIX.org");
    stage.setScene(scene);
    stage.sizeToScene();

    if (!stage.isShowing()) {
      stage.show();
    }

    // Center the window every time a screen is loaded.

    Platform.runLater(() -> {
      stage.sizeToScene();
      stage.centerOnScreen();
    });
  }

  public static void setPrimaryStage(Stage primaryStage) {
    Controls.primaryStage = primaryStage;
  }

  public static Stage getPrimaryStage() {
    return primaryStage;
  }

  public static <T extends Control> void hide(T element) {
    element.setManaged(false);
    element.setVisible(false);
  }

  public static <T extends Control> void show(T element) {
    element.setManaged(true);
    element.setVisible(true);
  }
}
