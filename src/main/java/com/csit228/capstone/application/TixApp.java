package com.csit228.capstone.application;

import com.csit228.capstone.model.Role;
import com.csit228.capstone.model.Serializer;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.Controls;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;


import java.io.IOException;

public class TixApp extends Application {
    public static Serializer serializer = new Serializer();
    public static User currentUser = null;
    public static Stage stage = null;    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        initialize();
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case MEMBER:
                    Controls.switchScreen("DashboardMemberView.fxml");
                    break;

                case EXECUTIVE:
                    Controls.switchScreen("DashboardExecutiveView.fxml");
                    break;

                case EDITOR:
                    Controls.switchScreen("DashboardEditorView.fxml");
                    break;

                default:
                    Controls.switchScreen("LoginView.fxml");
                    break;
            }
        } else {
            Controls.switchScreen("LoginView.fxml");
        }
    }


    public void initialize(){
        serializer.setFilePath("user.ser");
        currentUser = (User) serializer.deserialize();
    }
}
