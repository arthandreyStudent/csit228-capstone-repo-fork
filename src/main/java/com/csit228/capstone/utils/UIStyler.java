package com.csit228.capstone.utils;

import com.csit228.capstone.model.TicketView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class UIStyler {
  
  public static void applyNavyBlueHeaderGradient(Region region) {
    LinearGradient gradient = new LinearGradient(
      0, 0, 1, 0, true, CycleMethod.NO_CYCLE,   // Left to right gradient
      new Stop(0, Color.web("#1f3e8f")),
      new Stop(1, Color.web("#3a7ef3"))
    );
    
    region.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
  }
  
  public static void applyLeftSideBarGradient(Region region) {
    LinearGradient gradient = new LinearGradient(
      0, 0, 0, 1, true, CycleMethod.NO_CYCLE,   // Top to bottom gradient
      new Stop(0, Color.web("#0b112c")),
      new Stop(1, Color.web("#1b2c6f"))
    );
    
    region.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
  }
  
  /**
   * A small secondary badge shown beneath the primary status badge when a ticket
   * is both {@code IN_PROGRESS} and past its deadline.
   * Uses a muted red palette to distinguish it from the primary status badge.
   */
  public static Label makeOverdueBadge() {
    String bgColor = "#ffe0e5";
    String textColor = "#f14d5a";
    
    return makeBadge("OVERDUE", bgColor, textColor);
  }
  
  public static Label makeOverdueBadge(int fontSize) {
    String bgColor = "#ffe0e5";
    String textColor = "#f14d5a";
    
    return makeBadge("OVERDUE", bgColor, textColor, fontSize);
  }
  
  public static Label makeReturnedBadge() {
    String bgColor = "#f5ccbc";
    String textColor = "#da472d";
    
    return makeBadge("RETURNED", bgColor, textColor);
  }
  
  public static Label makeReturnedBadge(int fontSize) {
    String bgColor = "#f5ccbc";
    String textColor = "#da472d";
    
    return makeBadge("RETURNED", bgColor, textColor, fontSize);
  }
  
  public static Label makePriorityBadge(String priority) {
    String text = priority != null ? priority.toUpperCase() : "MEDIUM";
    
    String bgColor;
    String textColor;
    switch (text) {
      case "HIGH":   bgColor = "#fddbdf"; textColor = "#F64E60"; break;
      case "LOW":    bgColor = "#d1ffdb"; textColor = "#50CD89"; break;
      default:       bgColor = "#ffedc9"; textColor = "#F59E0B"; break;
    }
    
    return makeBadge(text, bgColor, textColor);
  }
  
  public static Label makePriorityBadge(String priority, int fontSize) {
    String text = priority != null ? priority.toUpperCase() : "MEDIUM";
    
    String bgColor;
    String textColor;
    switch (text) {
      case "HIGH":   bgColor = "#fddbdf"; textColor = "#F64E60"; break;
      case "LOW":    bgColor = "#d1ffdb"; textColor = "#50CD89"; break;
      default:       bgColor = "#ffedc9"; textColor = "#F59E0B"; break;
    }
    
    return makeBadge(text, bgColor, textColor, fontSize);
  }
  
  public static Label makeStatusBadge(String status) {
    String text = status != null ? status.toUpperCase().replace("_", " ") : "OPEN";
    
    String bgColor;
    String textColor;
    switch (text) {
      case "IN PROGRESS": bgColor = "#ffedcc"; textColor = "#ff9900"; break;
      case "RESOLVED":    bgColor = "#ecebf9"; textColor = "#7F77DD"; break;
      case "APPROVED":    bgColor = "#dcffef"; textColor = "#4bcc8a"; break;
      case "OVERDUE":     bgColor = "#ffe0e5"; textColor = "#f14d5a"; break;
      case "COMPLETED":   bgColor = "#d1ffdb"; textColor = "#50CD89"; break;
      default:            bgColor = "#dceeff"; textColor = "#2f95ff"; break;
    }
    
    return makeBadge(text, bgColor, textColor);
  }
  
  public static Label makeStatusBadge(String status, int fontSize) {
    String text = status != null ? status.toUpperCase().replace("_", " ") : "OPEN";
    
    String bgColor;
    String textColor;
    switch (text) {
      case "IN PROGRESS": bgColor = "#ffedcc"; textColor = "#ff9900"; break;
      case "RESOLVED":    bgColor = "#ecebf9"; textColor = "#7F77DD"; break;
      case "APPROVED":    bgColor = "#dcffef"; textColor = "#4bcc8a"; break;
      case "OVERDUE":     bgColor = "#ffe0e5"; textColor = "#f14d5a"; break;
      case "COMPLETED":   bgColor = "#d1ffdb"; textColor = "#50CD89"; break;
      default:            bgColor = "#dceeff"; textColor = "#2f95ff"; break;
    }
    
    return makeBadge(text, bgColor, textColor, fontSize);
  }
  
  public static Label makeBadge(String text, String bgColor, String textColor) {
    String display = text;
    
    Label badge = new Label(display);
    badge.setAlignment(Pos.CENTER);
    badge.setMinWidth(44);
    badge.setPrefHeight(22);
    badge.setPadding(new Insets(4, 10, 4, 10));
    badge.setStyle("-fx-background-color: " + bgColor + ";" +
                   "-fx-text-fill: " + textColor + ";" +
                   "-fx-background-radius: 6;" +
                   "-fx-font-size: 10px;" +
                   "-fx-font-family: 'Inter 18pt ExtraBold'"
    );
    return badge;
  }
  
  public static Label makeBadge(String text, String bgColor, String textColor, int fontSize) {
    String display = text;
    
    Label badge = new Label(display);
    badge.setAlignment(Pos.CENTER);
    badge.setMinWidth(44);
    badge.setPrefHeight(22);
    badge.setPadding(new Insets(4, 10, 4, 10));
    badge.setStyle("-fx-background-color: " + bgColor + ";" +
                   "-fx-text-fill: " + textColor + ";" +
                   "-fx-background-radius: 6;" +
                   "-fx-font-size: " + fontSize + "px;" +
                   "-fx-font-family: 'Inter 18pt ExtraBold';" +
                   "-fx-padding: 5px 15px 5px 15px;"
    );
    return badge;
  }
  
}
