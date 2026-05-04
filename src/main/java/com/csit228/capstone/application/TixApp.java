package com.csit228.capstone.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;


import java.io.IOException;

public class TixApp extends Application {
    public static Stage stage = null;    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.sizeToScene();
        stage.setScene(scene);
        stage.setTitle("TIX.org");
        stage.centerOnScreen();
        stage.show();
    }
}
