package com.csit228.capstone.application;

import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.Controls;
import com.csit228.capstone.utils.FontInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

// CHANGES MADE: MainApplication is now the real launcher for the application, and AppSession is just a utility class for managing the session and providing helper methods. This allows us to initialize fonts and load the session before showing any UI, which is necessary for the initial screen to be correct.

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FontInitializer.initializeFonts();  // Initializes all required fonts for the app.

        Controls.setPrimaryStage(stage);    // This method sets the primary stage that the Controls util will be referencing with the current stage provided by the JavaFX runtime.

        stage.setMinWidth(400);
        stage.setMinHeight(500);
        AppSession.loadSession();   // Calls the load session method from the AppSession class that's now a utility class.

        Controls.switchScreen(AppSession.getInitialScreen());   // Switches to the initial screen based on whether the user is logged in or not, which is determined by the loadSession method that sets the currentUser variable in AppSession.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
