package com.csit228.capstone.controller;

import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.utils.AppSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;

public class MasterTicketDetailModalController {
  
  @FXML
  public Button buttonClose;
  @FXML
  public HBox ticketHeader;
  @FXML
  public HBox statusBadgeContainer;
  @FXML
  public HBox priorityBadgeContainer;
  @FXML
  public Label assignedToSubHeader;
  @FXML
  public HBox assignedContainerOne;
  @FXML
  public HBox assignedContainerTwo;
  @FXML
  public Label createdBy;
  @FXML
  public Label createdDateLabel;
  @FXML
  public Label createdTimeLabel;
  @FXML
  public Label lastUpdatedDateLabel;
  @FXML
  public Label lastUpdatedTimeLabel;
  @FXML
  public Label deadlineDateLabel;
  @FXML
  public Label deadlineTimeLabel;
  @FXML
  public Label departmentLabel;
  @FXML
  public HBox commentsBadgeContainer;
  @FXML
  public VBox activityCommentContainer;
  @FXML
  public HBox leftButtonContainer;
  @FXML
  public HBox rightButtonContainer;
  @FXML
  public HBox changesRequestedNoticeContainer;
  @FXML
  public Label ticketCode;
  @FXML
  public Label ticketTitle;
  
  private TicketView currentTicket;
  
  @FXML
  public void initialize() {
    // Sets the gradient of the header to match the branding colors of the app.
    // This is done programmatically to allow for easier adjustments in the future without needing to modify
    // the FXML or add new resources.
    LinearGradient headerGradient = new LinearGradient(
        0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
        new Stop(0, Color.web("#1f3e8f")),
        new Stop(1, Color.web("#3a7ef3"))
    );
    
    ticketHeader.setBackground(new Background(new BackgroundFill(headerGradient, CornerRadii.EMPTY, Insets.EMPTY)));
  }
  
  public void loadTicket(TicketView ticket) {
    this.currentTicket = ticket;
    
    if (ticket != null) {
      ticketCode.setText("#TIX-" + String.format("%03d", ticket.getId()));
      ticketTitle.setText(ticket.getTitle() != null ? ticket.getTitle() : "Untitled Ticket");
      
      populateBadges(ticket);
      handleChangesRequestedNotice(ticket);
    }
  }
  
  private void handleChangesRequestedNotice(TicketView ticket) {
    changesRequestedNoticeContainer.getChildren().clear();
    changesRequestedNoticeContainer.setVisible(false);
    changesRequestedNoticeContainer.setManaged(false);
    
    // Also hide the parent HBox that wraps the notice container to completely remove its footprint
    if (changesRequestedNoticeContainer.getParent() instanceof HBox) {
      changesRequestedNoticeContainer.getParent().setVisible(false);
      changesRequestedNoticeContainer.getParent().setManaged(false);
    }
    
    if (AppSession.currentUser == null || !AppSession.currentUser.getRole().name().equals("MEMBER")) {
      return;
    }
    
    if (ticket.getStatus() == null ||
        (!ticket.getStatus().equals("SENT_BACK") && !ticket.getStatus().equals("IN_PROGRESS"))) {
      return;
    }
    
    // Determine the user and time who rejected it. Since it's from the comments later, we'll placeholder it for now.
    String returnedByStr = "Editor/Executive";
    String returnedDate = "Recently";
    
    if (ticket.getLastUpdated() != null) {
      returnedDate = ticket.getLastUpdated().format(DateTimeFormatter.ofPattern("MMM d, yyyy • hh:mm a"));
    }
    
    // IF WE REACH HERE, WE MUST SHOW IT.
    changesRequestedNoticeContainer.setVisible(true);
    changesRequestedNoticeContainer.setManaged(true);
    if (changesRequestedNoticeContainer.getParent() instanceof HBox) {
      changesRequestedNoticeContainer.getParent().setVisible(true);
      changesRequestedNoticeContainer.getParent().setManaged(true);
    }
    
    VBox noticeBox = new VBox();
    noticeBox.setSpacing(12);
    noticeBox.setPadding(new Insets(20));
    noticeBox.setStyle(
      "-fx-background-color: #FFF6F1; " + "-fx-background-radius: 12; " + "-fx-border-color: #FFE4D6; " +
      "-fx-border-radius: 12;");
    noticeBox.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(noticeBox, Priority.ALWAYS);
    
    HBox headerBox = new HBox();
    headerBox.setAlignment(Pos.CENTER_LEFT);
    headerBox.setSpacing(10);
    
    FontIcon warningIcon = new FontIcon("fas-exclamation-triangle");
    warningIcon.setIconSize(18);
    warningIcon.setIconColor(Color.web("#F64E60"));
    
    Label headerLabel = new Label("Changes Requested");
    headerLabel.setStyle("-fx-text-fill: #F64E60; -fx-font-family: 'Inter 18pt ExtraBold'; -fx-font-size: 16px;");
    headerBox.getChildren().addAll(warningIcon, headerLabel);
    
    Label bodyLabel = new Label(
      "Your submission was reviewed and requires changes before it can be approved.\nPlease review the feedback below" +
      " and resubmit when ready.");
    bodyLabel.setWrapText(true);
    bodyLabel.setStyle(
      "-fx-text-fill: #4B5563; -fx-font-family: 'Inter 18pt Regular'; -fx-font-size: 13px; -fx-line-spacing: 4px;");
    
    Label footerLabel = new Label("Returned by " + returnedByStr + " • " + returnedDate);
    footerLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-family: 'Inter 18pt Regular'; -fx-font-size: 12px;");
    
    noticeBox.getChildren().addAll(headerBox, bodyLabel, footerLabel);
    
    changesRequestedNoticeContainer.getChildren().add(noticeBox);
  }
  
  private Label createBadge(String text, String bgVar, String textVar) {
    Label badge = new Label(text);
    badge.setStyle(
      "-fx-background-color: " + bgVar + "; " + "-fx-text-fill: " + textVar + "; " + "-fx-padding: 6 12 6 12; " +
      "-fx-background-radius: 6; " + "-fx-font-family: 'Inter 18pt ExtraBold'; " + "-fx-font-size: 11px;");
    return badge;
  }
  
  private void populateBadges(TicketView ticket) {
    statusBadgeContainer.getChildren().clear();
    statusBadgeContainer.setStyle("-fx-background-color: transparent;");
    statusBadgeContainer.setAlignment(Pos.CENTER_LEFT);
    
    priorityBadgeContainer.getChildren().clear();
    priorityBadgeContainer.setStyle("-fx-background-color: transparent;");
    priorityBadgeContainer.setAlignment(Pos.CENTER_LEFT);
    
    // Status Badge
    String status = ticket.getStatus() != null ? ticket.getStatus().toUpperCase() : "OPEN";
    String statusBg = "-dodger-blue-bg-accent";
    String statusText = "-dodger-blue-accent";
    String displayStatus = status.replace("_", " ");
    
    switch (status) {
      case "IN_PROGRESS":
      case "IN PROGRESS":
        statusBg = "-web-orange-bg-accent";
        statusText = "-web-orange-accent";
        break;
      case "COMPLETED":
        statusBg = "-emerald-green-bg-accent";
        statusText = "-emerald-green-accent";
        break;
      case "RESOLVED":
      case "APPROVED":
        statusBg = "-blue-violet-bg-accent";
        statusText = "-blue-violet-accent";
        break;
    }
    statusBadgeContainer.getChildren().add(createBadge(displayStatus, statusBg, statusText));
    
    // Priority Badge
    String priority = ticket.getPriority() != null ? ticket.getPriority().toUpperCase() : "MEDIUM";
    String prioBg = "-web-orange-bg-accent";
    String prioText = "-web-orange-accent";
    
    switch (priority) {
      case "LOW":
        prioBg = "-emerald-green-bg-accent";
        prioText = "-emerald-green-accent";
        break;
      case "HIGH":
      case "URGENT":
        prioBg = "-carnation-red-bg-accent";
        prioText = "-carnation-red-accent";
        break;
    }
    priorityBadgeContainer.getChildren().add(createBadge(priority, prioBg, prioText));
  }
  
  @FXML
  public void onClickedButtonClose(ActionEvent event) {
  }
}
