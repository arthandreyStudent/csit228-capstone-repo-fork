package com.csit228.capstone.controller;

import com.csit228.capstone.model.Department;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.Formatter;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TicketExecutiveController extends StaffTicketController {
  
  @FXML
  private Label unassignedLabel;
  
  @FXML
  private Label inProgressLabel;
  
  @FXML
  private Label resolvedLabel;
  
  @FXML
  private Label overdueLabel;
  
  @FXML
  private Label resolutionRateLabel;
  
  @FXML
  private Label resolvedRatePercentLabel;
  
  @FXML
  private Label inProgressRatePercentLabel;
  
  @FXML
  private Label overdueRatePercentLabel;
  
  @FXML
  private ProgressBar resolvedProgressBar;
  
  @FXML
  private ProgressBar inProgressProgressBar;
  
  @FXML
  private ProgressBar overdueProgressBar;
  
  @FXML
  private HBox departmentTabsBox;
  
  @FXML
  private VBox pendingAssignmentQueueBox;
  
  @FXML
  private VBox recentActivityBox;
  
  private static final String VOLUNTEER_TAB_NAME = "Volunteer";
  
  private String selectedDepartmentName = null;
  
  @Override
  protected String getDefaultRoleName() {
    return "EXECUTIVE";
  }
  
  @Override
  protected void refreshDashboard() {
    ticketDAO.getTicketViews();
    tickets = new ArrayList<>(ticketDAO.getViews());

    renderDashboard();
  }

  @Override
  protected void renderDashboard() {
    updateSummaryCardsAndResolutionRate();
    loadPendingAssignmentQueue();
    refreshActivityBox();
  }
  
  @Override
  protected void onSearchChanged() {
    loadPendingAssignmentQueue();
  }
  
  @Override
  protected void onDeadlineSortSelected() {
    loadPendingAssignmentQueue();
  }
  
  @FXML
  public void initialize() {
    setupProfile();
    setupSearch();
    setupDeadlineSortComboBox();
    loadDepartmentsAndTabs();
    loadRecentActivity(recentActivityBox);
    refreshDashboard();
    startWatching();
  }
  
  private void loadDepartmentsAndTabs() {
    loadDepartments();
    renderDepartmentTabs();
  }
  
  private void renderDepartmentTabs() {
    departmentTabsBox.getChildren().clear();
    
    Button allButton = createTabButton("All Depts", selectedDepartmentName == null);
    allButton.setOnAction(event -> {
      selectedDepartmentName = null;
      renderDepartmentTabs();
      loadPendingAssignmentQueue();
    });
    departmentTabsBox.getChildren().add(allButton);
    
    Button volunteerButton =
      createTabButton(VOLUNTEER_TAB_NAME, VOLUNTEER_TAB_NAME.equalsIgnoreCase(selectedDepartmentName));
    volunteerButton.setOnAction(event -> {
      selectedDepartmentName = VOLUNTEER_TAB_NAME;
      renderDepartmentTabs();
      loadPendingAssignmentQueue();
    });
    departmentTabsBox.getChildren().add(volunteerButton);
    
    for (Department department : departments) {
      String name = department.getName();
      if (name != null && name.equalsIgnoreCase(VOLUNTEER_TAB_NAME))
        continue;
      
      Button btn = createTabButton(name, name != null && name.equalsIgnoreCase(selectedDepartmentName));
      btn.setOnAction(event -> {
        selectedDepartmentName = name;
        renderDepartmentTabs();
        loadPendingAssignmentQueue();

      });
      departmentTabsBox.getChildren().add(btn);
    }
  }
  
  private Button createTabButton(String text, boolean selected) {
    Button button = new Button(text);
    button.setPrefHeight(32.0);
    button.setMinWidth(58.0);
    button.setCursor(Cursor.HAND);
    
    if (selected) {
      button.setStyle("-fx-background-color: #2f95ff;" + "-fx-background-radius: 20;" + "-fx-text-fill: white;" +
                      "-fx-font-size: 12px;" + "-fx-font-weight: bold;" + "-fx-padding: 0 18 0 18;");
    } else {
      button.setStyle("-fx-background-color: white;" + "-fx-border-color: #dfe7f5;" + "-fx-border-radius: 20;" +
                      "-fx-background-radius: 20;" + "-fx-text-fill: #9faad2;" + "-fx-font-size: 12px;" +
                      "-fx-font-weight: bold;" + "-fx-padding: 0 18 0 18;");
    }
    
    return button;
  }
  
  private void updateSummaryCardsAndResolutionRate() {
    int total = tickets.size();
    int unassigned = 0, inProgress = 0, resolved = 0, overdue = 0;
    
    for (TicketView ticket : tickets) {
      if (isUnassigned(ticket))
        unassigned++;
      if (isStatus(ticket, TicketStatus.IN_PROGRESS.name()))
        inProgress++;
      if (isResolved(ticket))
        resolved++;
      if (isOverdue(ticket))
        overdue++;
    }
    
    unassignedLabel.setText(String.valueOf(unassigned));
    inProgressLabel.setText(String.valueOf(inProgress));
    resolvedLabel.setText(String.valueOf(resolved));
    overdueLabel.setText(String.valueOf(overdue));
    
    double resolvedRate = rate(resolved, total);
    double inProgressRate = rate(inProgress, total);
    double overdueRate = rate(overdue, total);
    
    resolutionRateLabel.setText(Formatter.formatPercent(resolvedRate));
    resolvedRatePercentLabel.setText(Formatter.formatPercent(resolvedRate));
    inProgressRatePercentLabel.setText(Formatter.formatPercent(inProgressRate));
    overdueRatePercentLabel.setText(Formatter.formatPercent(overdueRate));
    
    resolvedProgressBar.setProgress(resolvedRate);
    inProgressProgressBar.setProgress(inProgressRate);
    overdueProgressBar.setProgress(overdueRate);
  }
  
  private void loadPendingAssignmentQueue() {
    pendingAssignmentQueueBox.getChildren().clear();
    
    String keyword = searchField != null ? searchField.getText() : "";
    
    for (TicketView ticket : getSortedTicketsByDeadline()) {
      if (!isAssignableTicket(ticket))
        continue;
      if (!matchesSelectedDepartment(ticket))
        continue;
      if (!matchesTicketSearch(ticket, keyword))
        continue;
      
      List<User> assignableUsers = getAssignableMembersForTicket(ticket);
      ListRowItem row = ListRowItem.forExecutiveAssignment(ticket, assignableUsers);

      row.setAction(event -> assignTicketToUser(ticket, row.getSelectedAssignedUser()));
      row.setRowClick(event -> {
        if (isInteractiveTarget(event.getTarget())) {
          return;
        }
        openTicketDetailModal(ticket);
      });

      pendingAssignmentQueueBox.getChildren().add(row);
    }
  }
  
  private boolean isAssignableTicket(TicketView ticket) {
    if (ticket == null)
      return false;
    return (!isStatus(ticket, TicketStatus.COMPLETED.name()) && !isStatus(ticket, TicketStatus.RESOLVED.name()));
  }
  
  private boolean matchesSelectedDepartment(TicketView ticket) {
    if (selectedDepartmentName == null || selectedDepartmentName.trim().isEmpty())
      return true;
    if (VOLUNTEER_TAB_NAME.equalsIgnoreCase(selectedDepartmentName))
      return ticket.isVolunteerTicket();
    return (ticket.getDepartmentName() != null && ticket.getDepartmentName().equalsIgnoreCase(selectedDepartmentName));
  }
  
  @FXML
  public void handleCreateTicket() {
    try {
      FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/CreateTicketModalExecView.fxml"));
      Parent root = loader.load();
      CreateTicketModalExecController controller = loader.getController();
      
      openModal(root, "Create New Ticket");
      
      if (controller != null && controller.isSubmitted()) {
        refreshDashboard();
      }
    } catch (IOException e) {
      showError("Unable to open Create Ticket modal.");
    }
  }
  
  private void openTicketDetailModal(TicketView ticket) {
    try {
      FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/MasterTicketDetailModalView.fxml"));
      Parent root = loader.load();

      TicketDetailModelController controller = loader.getController();
      controller.loadTicket(ticket);

      openModal(root, "Ticket Details");
    } catch (IOException e) {
      showError("Unable to open Ticket Details modal.");
    }
  }

  private boolean isInteractiveTarget(Object target) {
    if (!(target instanceof Node)) {
      return false;
    }

    Node node = (Node) target;
    while (node != null) {
      if (node instanceof ButtonBase || node instanceof ComboBoxBase || node instanceof TextInputControl) {
        return true;
      }
      node = node.getParent();
    }

    return false;
  }

  private double rate(int value, int total) {
    return total <= 0 ? 0 : (double) value / total;
  }
}
