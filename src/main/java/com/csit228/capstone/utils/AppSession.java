package com.csit228.capstone.utils;

import com.csit228.capstone.model.Serializer;
import com.csit228.capstone.model.User;

import java.io.File;

// CHANGES MADE: Updated the AppSession.java to be a utility class only by removing `extends Application` and making
// the constructor private. This allows us to use AppSession as a central place for managing user sessions and
// serialization without needing to launch a JavaFX application from it. The getInitialScreen method is added to
// determine which screen to show based on the current user's role.

public final class AppSession {
  
  public static final Serializer serializer = new Serializer();
  public static User currentUser = null;
  
  private AppSession() {
  }
  
  public static void loadSession() {
    serializer.setFilePath("user.ser");
    currentUser = (User) serializer.deserialize();
  }
  
  // Added some more helper methods related to user session managements
  public static void saveSession(User user) {
    currentUser = user;
    serializer.setUser(user);
    serializer.serialize();
  }
  
  public static void clearSession() {
    currentUser = null;
    serializer.setUser(null);
    serializer.setTicket(null);
    serializer.setFilePath("user.ser");
    
    File sessionFile = new File("user.ser");
    
    try {
      if (sessionFile.exists()) {
        boolean deleted = sessionFile.delete();
        
        if (!deleted) {
          // If delete fails, empty the file instead.
          new java.io.FileOutputStream(sessionFile, false).close();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  // This method is what determines which screen to show based on the current user's role. This returns the path of
  // the JavaFX FXML file that should be loaded as the initial screen when the application starts. If there is no
  // user logged in, it defaults to the login screen.
  public static String getInitialScreen() {
    if (currentUser == null || currentUser.getRole() == null) {
      return "LoginView.fxml";
    }
    
    return switch (currentUser.getRole()) {
      case MEMBER -> "DashboardMemberView.fxml";
      case EXECUTIVE -> "DashboardExecutiveView.fxml";
      case EDITOR -> "DashboardEditorView.fxml";
    };
  }
}
