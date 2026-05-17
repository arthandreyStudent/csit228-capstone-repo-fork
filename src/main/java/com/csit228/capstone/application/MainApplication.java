package com.csit228.capstone.application;

import com.csit228.capstone.observer.TicketWatcher;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.Controls;
import com.csit228.capstone.utils.FontInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
  
  @Override
  public void start(Stage stage) throws IOException {
    FontInitializer.initializeFonts();

    Controls.setPrimaryStage(stage);
    
    AppSession.loadSession();

    Controls.switchScreen(AppSession.getInitialScreen());
  }

  public void stop() throws Exception {
    TicketWatcher.getInstance().shutdown();
    super.stop();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
