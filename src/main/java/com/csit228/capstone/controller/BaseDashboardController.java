package com.csit228.capstone.controller;

import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.observer.DashboardObserver;
import com.csit228.capstone.observer.TicketWatcher;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.Controls;
import com.csit228.capstone.utils.Formatter;
import com.csit228.capstone.utils.TicketDeadlineComparator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDashboardController implements DashboardObserver {

  @FXML
  protected Label profileInitialsLabel;
  
  @FXML
  protected Label profileNameLabel;
  
  @FXML
  protected Label profileRoleLabel;
  
  @FXML
  protected TextField searchField;
  
  @FXML
  protected ComboBox<String> deadlineSortComboBox;
  
  protected final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
  protected List<TicketView> tickets = new ArrayList<>();

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
  public void onDataChanged(List<TicketView> updatedTickets) {
    this.tickets = updatedTickets != null ? new ArrayList<>(updatedTickets) : new ArrayList<>();
    renderDashboard();
  }

  protected void startWatching() {
    TicketWatcher.getInstance().addObserver(this);
    TicketWatcher.getInstance().start(2);
  }

  protected void stopWatching() {
    TicketWatcher.getInstance().removeObserver(this);
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
  
  protected boolean isToDoTicket(TicketView ticket) {
    return isAssignedToCurrentUser(ticket) && isStatus(ticket, TicketStatus.OPEN.name());
  }
  
  protected boolean isOverdue(TicketView ticket) {
    if (ticket == null || ticket.getDeadline() == null)
      return false;
    if (isResolved(ticket))
      return false;
    return LocalDate.now().isAfter(ticket.getDeadline().toLocalDate());
  }
  
  protected boolean isReturned(TicketView ticket) {
    return isAssignedToCurrentUser(ticket) && isStatus(ticket, TicketStatus.IN_PROGRESS.name()) && ticket.getReturnReason() != null && !ticket.getReturnReason().trim().isEmpty();
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


}
