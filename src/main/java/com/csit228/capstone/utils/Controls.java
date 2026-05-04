package com.csit228.capstone.utils;

import com.csit228.capstone.application.TixApp;
import javafx.scene.control.Control;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Controls {
    public static void switchScreen(String screen) throws IOException {
        Stage stage = TixApp.stage;
        FXMLLoader fxmlLoader = new FXMLLoader(Controls.class.getResource("/com/csit228/capstone/view/" + screen));
        Scene scene = new Scene(fxmlLoader.load());
        stage.sizeToScene();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("TIX.org");
        stage.show();
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
