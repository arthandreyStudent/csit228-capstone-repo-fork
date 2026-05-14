package com.csit228.capstone.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
  
  static String url;
  static String user;
  static String password;
  private static boolean envLoaded;
  
  public static Connection getConnection() {
    ensureEnvLoaded();
    try {
      return DriverManager.getConnection(url, user, password);
    } catch (SQLException e) {
      System.out.println("SAD");
      throw new RuntimeException(e);
    }
  }
  
  private static synchronized void ensureEnvLoaded() {
    if (envLoaded) {
      return;
    }
    
    try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
      url = br.readLine().trim();
      user = br.readLine().trim();
      password = br.readLine().trim();
      envLoaded = true;
    } catch (IOException e) {
      throw new RuntimeException("Unable to load database credentials from .env", e);
    }
  }
  
  public static void main(String[] args) {
    getConnection();
  }
}
