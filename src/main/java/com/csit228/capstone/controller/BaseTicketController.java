package com.csit228.capstone.controller;

import com.csit228.capstone.dao.NotificationDAO;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.Notification;
import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.observer.NotificationObserver;
import com.csit228.capstone.observer.NotificationWatcher;
import com.csit228.capstone.observer.TicketObserver;
import com.csit228.capstone.observer.TicketWatcher;
import com.csit228.capstone.utils.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTicketController implements TicketObserver, NotificationObserver {

  @FXML
  protected Label profileInitialsLabel;
  
  @FXML
  protected Label profileNameLabel;
  
  @FXML
  protected Label profileRoleLabel;
  
  @FXML
  protected TextField searchField;

  @FXML
  protected AnchorPane mainContentPane;

  private final List<Node> dashboardContentNodes = new ArrayList<>();
  
  @FXML
  protected ComboBox<String> deadlineSortComboBox;

  @FXML
  protected Button profileButton;

  private boolean isProfileViewOpen = false;
  protected final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
  protected List<TicketView> tickets = new ArrayList<>();
  protected List<Notification> notifications = new ArrayList<>();
  protected final NotificationDAO notificationDAO = NotificationDAO.getNotificationDAO();
  @FXML
  protected VBox activityBox;

  protected abstract String getDefaultRoleName();
  protected abstract void refreshDashboard();
  protected abstract void renderDashboard();

  protected void setupProfile() {
    User user = AppSession.currentUser;
    
    if (user == null) {
      profileInitialsLabel.setText("NA");
      profileNameLabel.setText(getDefaultRoleName() + " User");
      profileRoleLabel.setText(getDefaultRoleName());
      return;
    }
    
    profileInitialsLabel.setText(Formatter.getInitials(user));
    profileNameLabel.setText(user.getFullName());
    profileRoleLabel.setText(user.getRole() != null ? user.getRole().toString() : getDefaultRoleName());
  }

  @Override
  public void onTicketChange(List<TicketView> updatedTickets) {
      refreshDashboard();
  }


  @Override
  public void onNotificationsChanged(List<Notification> updatedNotifications) {
      this.notifications = new ArrayList<>(updatedNotifications);
      renderDashboard();
  }

  protected void refreshActivityBox() {
    if (activityBox == null) return;
    activityBox.getChildren().clear();
    int count = 0;
    for (Notification n : notifications) {
        activityBox.getChildren().add(ListRowItem.forActivity(n));
        if (++count >= 8) break;
    }
  }

  @Override
  public int getUserId() {
      return getCurrentUserId();
  }

  protected void startWatching() {
    TicketWatcher ticketWatcher = TicketWatcher.getInstance();
    ticketWatcher.addObserver(this);
    ticketWatcher.start(2);

    NotificationWatcher notifWatcher = NotificationWatcher.getInstance();
    int initialCount = notificationDAO.getNotificationsByUserId(getCurrentUserId()).size();
    notifWatcher.setInitialCount(getCurrentUserId(), initialCount);
    notifWatcher.addObserver(this);
    notifWatcher.start(2);
  }

  protected void stopWatching() {
    TicketWatcher.getInstance().removeObserver(this);
    NotificationWatcher.getInstance().removeObserver(this);
  }

  protected void setupSearch() {
    if (searchField == null) return;
    searchField.setPromptText("Search tickets...");
    searchField.textProperty().addListener((obs, oldVal, newVal) -> onSearchChanged());
  }

  protected void onSearchChanged() {
  
  }

  protected void setupDeadlineSortComboBox() {
    if (deadlineSortComboBox == null)
      return;
    deadlineSortComboBox.getItems().setAll("Nearest Deadline", "Farthest Deadline");
    deadlineSortComboBox.setValue("Nearest Deadline");
  }
  
  @FXML
  public void onDeadlineSortChanged() {
    onDeadlineSortSelected();
  }
  
  protected void onDeadlineSortSelected() {
  }
  
  protected List<TicketView> getSortedTicketsByDeadline() {
    List<TicketView> sorted = new ArrayList<>(tickets);
    TicketDeadlineComparator.SortMode mode = TicketDeadlineComparator.getSortModeFromText(
      deadlineSortComboBox != null ? deadlineSortComboBox.getValue() : null);
    sorted.sort(new TicketDeadlineComparator(mode));
    return sorted;
  }
  
  protected boolean isAvailableTicket(TicketView ticket) {
    if (ticket == null || !isUnassigned(ticket))
      return false;
    return isStatus(ticket, TicketStatus.OPEN.name());
  }
  
  protected boolean isAssignedToCurrentUser(TicketView ticket) {
    User currentUser = AppSession.currentUser;
    if (currentUser == null || ticket.getAssignedToName() == null)
      return false;
    return ticket.getAssignedToName().equalsIgnoreCase(currentUser.getFullName());
  }
  
  protected boolean isStatus(TicketView ticket, String status) {
    return (ticket != null && ticket.getStatus() != null && ticket.getStatus().equalsIgnoreCase(status));
  }
  
  protected boolean isUnassigned(TicketView ticket) {
    return (ticket == null || ticket.getAssignedToName() == null || ticket.getAssignedToName().trim().isEmpty());
  }
  
  protected boolean isAvailableUnderDept(TicketView ticket) {
    User currentUser = AppSession.currentUser;
    if (currentUser == null || ticket == null || !isUnassigned(ticket) || !isStatus(ticket, TicketStatus.OPEN.name())) {
      return false;
    }

    String currentDeptName = DepartmentDAO.getDepartmentDAO().getDepartmentNameByID(currentUser.getDepartment_id());
    String ticketDeptName = ticket.getDepartmentName();

    return currentDeptName != null && ticketDeptName != null &&
           currentDeptName.trim().equalsIgnoreCase(ticketDeptName.trim());
  }
  
  protected boolean isOverdue(TicketView ticket) {
    if (ticket == null || ticket.getDeadline() == null)
      return false;
    if (isResolved(ticket))
      return false;
    return LocalDate.now().isAfter(ticket.getDeadline().toLocalDate());
  }

  protected boolean isInProgress(TicketView ticket) {
    return (isStatus(ticket, TicketStatus.IN_PROGRESS.name()));
  }
  
  protected boolean isCompleted(TicketView ticket) {
    return (isStatus(ticket, TicketStatus.COMPLETED.name()));
  }
  
  protected boolean isResolved(TicketView ticket) {
    return (isStatus(ticket, TicketStatus.RESOLVED.name()));
  }
  
  protected boolean isOverdueInProgress(TicketView ticket) {
    return isInProgress(ticket) && isOverdue(ticket);
  }
  
  protected boolean isVolunteerTicket(TicketView ticket) {
    if (ticket == null)
      return false;
    String dept = ticket.getDepartmentName();
    return dept.equalsIgnoreCase("Volunteer") && isUnassigned(ticket) && isStatus(ticket, TicketStatus.OPEN.name());
  }
  
  protected boolean matchesTicketSearch(TicketView ticket, String keyword) {
    if (keyword == null || keyword.trim().isEmpty())
      return true;
    String search = keyword.trim().toLowerCase();
    return (Formatter.trimOrNA(ticket.getTitle()).toLowerCase().contains(search) ||
            Formatter.trimOrNA(ticket.getDescription()).toLowerCase().contains(search) ||
            Formatter.trimOrNA(ticket.getDepartmentName()).toLowerCase().contains(search) ||
            Formatter.trimOrNA(ticket.getPriority()).toLowerCase().contains(search) ||
            Formatter.trimOrNA(ticket.getStatus()).toLowerCase().contains(search) ||
            Formatter.trimOrNA(ticket.getCreatedBy()).toLowerCase().contains(search) ||
            Formatter.trimOrNA(ticket.getAssignedToName()).toLowerCase().contains(search));
  }
  
  protected int getCurrentUserId() {
    return AppSession.currentUser != null ? AppSession.currentUser.getUserId() : 0;
  }

  @FXML
  public void onClickedProfile() {
        if (isProfileViewOpen) {
            return;
        }

        if (mainContentPane == null) {
            showError("Main content area was not found.");
            return;
        }

        try {
            isProfileViewOpen = true;

            if (profileButton != null) {
                profileButton.setDisable(true);
            }

            dashboardContentNodes.clear();
            dashboardContentNodes.addAll(mainContentPane.getChildren());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/csit228/capstone/view/ProfileView.fxml")
            );

            Parent profileView = loader.load();

            ProfileViewController profileController = loader.getController();
            profileController.setBackAction(this::showDashboardContent);

            AnchorPane.setTopAnchor(profileView, 0.0);
            AnchorPane.setRightAnchor(profileView, 0.0);
            AnchorPane.setBottomAnchor(profileView, 0.0);
            AnchorPane.setLeftAnchor(profileView, 0.0);

            mainContentPane.getChildren().setAll(profileView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Unable to open Profile.");
            resetProfileButton();
        }
  }


  protected void showDashboardContent() {
        if (mainContentPane == null || !isProfileViewOpen) {
            return;
        }

        mainContentPane.getChildren().setAll(dashboardContentNodes);
        dashboardContentNodes.clear();

        resetProfileButton();
  }
  private void resetProfileButton() {
    isProfileViewOpen = false;

    if (profileButton != null) {
      profileButton.setDisable(false);
    }
  }

  @FXML
  public void onClickedLogout() throws IOException {
    stopWatching();
    AppSession.clearSession();
    Controls.switchScreen("LoginView.fxml");
  }

  protected void showInfo(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("TIX.org");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  protected void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("TIX.org");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  protected void loadRecentActivity(VBox activityBox) {
    this.activityBox = activityBox;

    if (this.notifications.isEmpty()) {
      this.notifications = notificationDAO.getNotificationsByUserId(getCurrentUserId());
    }

    refreshActivityBox();
  }
}
