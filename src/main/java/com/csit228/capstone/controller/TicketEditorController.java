package com.csit228.capstone.controller;
import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.Department;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;
import com.csit228.capstone.dao.NotificationDAO;
import com.csit228.capstone.enums.Role;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicketEditorController extends StaffTicketController {
  private static final String STATUS_OPEN = "#3B82F6";
  private static final String STATUS_IN_PROGRESS = "#F59E0B";
  private static final String STATUS_COMPLETED = "#22C55E";
  private static final String STATUS_RESOLVED = "#8B5CF6";
  private static final String FILTER_ALL = "#1c2b63";

  
  @FXML
  private Label awaitingReviewLabel;
  
  @FXML
  private Label inProgressLabel;
  
  @FXML
  private Label approvedTodayLabel;
  
  @FXML
  private Label sentBackLabel;
  
  @FXML
  private VBox reviewQueueBox;
  
  @FXML
  private Button allFilterButton;
  
  @FXML
  private Button openFilterButton;
  
  @FXML
  private Button inProgressFilterButton;
  
  @FXML
  private Button completedFilterButton;
  
  @FXML
  private Button resolvedFilterButton;

  @FXML
  private TextField titleField;

  @FXML
  private TextField descriptionTextField;

  private final NotificationDAO notificationDAO = NotificationDAO.getNotificationDAO();
  
  private ReviewQueueFilter currentFilter = ReviewQueueFilter.ALL;
  
  private enum ReviewQueueFilter {
    ALL, OPEN, IN_PROGRESS, TO_BE_REVIEWED, RESOLVED,
  }
  
  @Override
  protected String getDefaultRoleName() {
    return "EDITOR";
  }
  
  @Override
  protected void refreshDashboard() {
    ticketDAO.getTicketViews();
    DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    Department department = departmentDAO.getDepartmentByID(AppSession.currentUser.getDepartment_id());
    tickets = new ArrayList<>(ticketDAO.getTicketByDepartment(department));

    renderDashboard();
  }

  @Override
  protected void renderDashboard() {
    updateSummaryCardsAndReviewStats();
    loadReviewQueue();
  }
  
  @Override
  protected void onSearchChanged() {
    loadReviewQueue();
  }
  
  @Override
  protected void onDeadlineSortSelected() {
    loadReviewQueue();
  }
  
  @FXML
  public void initialize() {
    setupProfile();
    setupSearch();
    setupFilterButtons();
    setupDeadlineSortComboBox();
    loadDepartments();
    refreshDashboard();
    startWatching();
  }
  
  private void setupFilterButtons() {
    setActiveFilterButton(allFilterButton);
  }
  
  @FXML
  public void showAllTickets() {
    currentFilter = ReviewQueueFilter.ALL;
    setActiveFilterButton(allFilterButton);
    loadReviewQueue();
  }
  
  @FXML
  public void showOpenTickets() {
    currentFilter = ReviewQueueFilter.OPEN;
    setActiveFilterButton(openFilterButton);
    loadReviewQueue();
  }
  
  @FXML
  public void showInProgressTickets() {
    currentFilter = ReviewQueueFilter.IN_PROGRESS;
    setActiveFilterButton(inProgressFilterButton);
    loadReviewQueue();
  }
  
  @FXML
  public void showCompletedTickets() {
    currentFilter = ReviewQueueFilter.TO_BE_REVIEWED;
    setActiveFilterButton(completedFilterButton);
    loadReviewQueue();
  }
  
  @FXML
  public void showResolvedTickets() {
    currentFilter = ReviewQueueFilter.RESOLVED;
    setActiveFilterButton(resolvedFilterButton);
    loadReviewQueue();
  }
  
  private void setActiveFilterButton(Button activeButton) {
    setInactiveFilterStyle(allFilterButton);
    setInactiveFilterStyle(openFilterButton);
    setInactiveFilterStyle(inProgressFilterButton);
    setInactiveFilterStyle(completedFilterButton);
    setInactiveFilterStyle(resolvedFilterButton);
    
    if (activeButton != null) {
      activeButton.setStyle("-fx-background-color: " + getActiveFilterColor() + ";" + "-fx-background-radius: 18;" + "-fx-text-fill: white;" +
                            "-fx-font-size: 11px;" + "-fx-font-weight: bold;");
    }
  }

  private String getActiveFilterColor() {
    switch (currentFilter) {
      case OPEN:
        return STATUS_OPEN;
      case IN_PROGRESS:
        return STATUS_IN_PROGRESS;
      case TO_BE_REVIEWED:
        return STATUS_COMPLETED;
      case RESOLVED:
        return STATUS_RESOLVED;
      case ALL:
      default:
        return FILTER_ALL;
    }
  }
  
  private void setInactiveFilterStyle(Button button) {
    if (button == null)
      return;
    button.setStyle("-fx-background-color: white;" + "-fx-border-color: #dfe7f5;" + "-fx-border-radius: 18;" +
                    "-fx-background-radius: 18;" + "-fx-text-fill: #9faad2;" + "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;");
  }
  
  private void updateSummaryCardsAndReviewStats() {
    int inProgress = 0, toBeReviewed = 0, resolved = 0, sentBack = 0;
    
    for (TicketView ticket : tickets) {
      if (isStatus(ticket, TicketStatus.IN_PROGRESS.name())) {
        inProgress++;
      }
      if (isStatus(ticket, TicketStatus.COMPLETED.name())) {
        toBeReviewed++;
      }
      if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
        resolved++;
      }
    }
    
    awaitingReviewLabel.setText(String.valueOf(toBeReviewed));
    inProgressLabel.setText(String.valueOf(inProgress));
    approvedTodayLabel.setText(String.valueOf(resolved));
    sentBackLabel.setText(String.valueOf(sentBack));

  }
  private void openTicketDetailModal(TicketView ticket) {
    try {
      FXMLLoader loader =
              new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/EditorViewTicket.fxml"));
      Parent root = loader.load();

      TicketDetailModelController controller = loader.getController();
      controller.loadTicket(ticket);

      openModal(root, "Ticket Details");
      refreshDashboard();
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

  private void loadReviewQueue() {
    reviewQueueBox.getChildren().clear();

    String keyword = searchField != null ? searchField.getText() : "";
    
    for (TicketView ticket : getSortedTicketsByDeadline()) {
      if (ticket.isVolunteerTicket() && isUnassigned(ticket))
        continue;
      if (!matchesCurrentFilter(ticket))
        continue;
      if (!matchesTicketSearch(ticket, keyword))
        continue;
      
      ListRowItem row = ListRowItem.forEditorReview(ticket);
      row.setRowClick(event -> {
        if (isInteractiveTarget(event.getTarget())) {
          return;
        }
        openTicketDetailModal(ticket);
      });

      if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
        lockResolvedTicketRow(row);
        reviewQueueBox.getChildren().add(row);
        continue;
      }
      
      if (!isStatus(ticket, TicketStatus.COMPLETED.name())) {
        hideReviewActionButtons(row);
        reviewQueueBox.getChildren().add(row);
        continue;
      }
      
      row.setAction(event -> updateTicketStatus(ticket, TicketStatus.RESOLVED, "Ticket marked as resolved."));
      row.setThirdAction(
        event -> updateTicketStatus(ticket, TicketStatus.IN_PROGRESS, "Ticket returned to in progress."));
      reviewQueueBox.getChildren().add(row);
    }
    

  }
  
  private boolean matchesCurrentFilter(TicketView ticket) {
    if (ticket == null)
      return false;
    
    switch (currentFilter) {
      case ALL:
        return true;
      case OPEN:
        return isStatus(ticket, TicketStatus.OPEN.name());
      case IN_PROGRESS:
        return isStatus(ticket, TicketStatus.IN_PROGRESS.name());
      case TO_BE_REVIEWED:
        return isStatus(ticket, TicketStatus.COMPLETED.name());
      case RESOLVED:
        return isStatus(ticket, TicketStatus.RESOLVED.name());
      default:
        return true;
    }
  }
  
  private int getFilteredTicketCount() {
    int count = 0;
    String keyword = searchField != null ? searchField.getText() : "";

    for (TicketView ticket : tickets) {
      if (isVisibleInReviewQueue(ticket) && matchesCurrentFilter(ticket) && matchesTicketSearch(ticket, keyword))
        count++;
    }
    return count;
  }

  private boolean isVisibleInReviewQueue(TicketView ticket) {
    if (ticket == null)
      return false;
    return !(ticket.isVolunteerTicket() && isUnassigned(ticket));
  }
  
  private void hideReviewActionButtons(ListRowItem row) {
    if (row == null)
      return;
    setButtonHidden(row.getActionButton());
    setButtonHidden(row.getThirdActionButton());
  }
  
  private void lockResolvedTicketRow(ListRowItem row) {
    if (row == null)
      return;
    
    if (row.getAssignComboBox() != null) {
      row.getAssignComboBox().setDisable(true);
      row.getAssignComboBox().setPromptText("Closed");
    }
    
    if (row.getSecondaryActionButton() != null) {
      row.getSecondaryActionButton().setDisable(true);
      row.getSecondaryActionButton().setText("Closed");
    }
    
    setButtonHidden(row.getActionButton());
    setButtonHidden(row.getThirdActionButton());
  }
  
  private void setButtonHidden(Button button) {
    if (button == null)
      return;
    button.setDisable(true);
    button.setVisible(false);
    button.setManaged(false);
  }
  
  @FXML
  public void handleCreateTicket() {
    try {
      FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/CreateTicketModalEditorView.fxml"));
      Parent root = loader.load();
      CreateTicketModalEditorController controller = loader.getController();
      
      openModal(root, "Create New Ticket");
      
      if (controller != null && controller.isSubmitted()) {
        refreshDashboard();
      }
    } catch (IOException e) {
      showError("Unable to open Create Ticket modal.");
    }
  }

  @FXML
  public void createAnnouncement() {
    String title = titleField != null ? titleField.getText().trim() : "";
    String description = descriptionTextField != null ? descriptionTextField.getText().trim() : "";

    if (title.isEmpty()) {
      showError("Please enter an announcement title.");
      return;
    }

    if (description.isEmpty()) {
      showError("Please enter an announcement description.");
      return;
    }

    User currentUser = AppSession.currentUser;

    if (currentUser == null) {
      showError("No logged-in editor found.");
      return;
    }

    if (currentUser.getDepartment_id() <= 0) {
      showError("Your account is not assigned to a department.");
      return;
    }

    List<User> recipients = getAnnouncementRecipients(currentUser);

    if (recipients.isEmpty()) {
      showError("No department members found to receive this announcement.");
      return;
    }

    int sentCount = notificationDAO.createNotifications(recipients, title, description);

    if (sentCount <= 0) {
      showError("Unable to create announcement.");
      return;
    }

    titleField.clear();
    descriptionTextField.clear();
    showInfo("Announcement sent to " + sentCount + " user" + (sentCount == 1 ? "." : "s."));
  }


  private List<User> getAnnouncementRecipients(User currentUser) {
    List<User> recipients = new ArrayList<>();
    Set<Integer> seenUserIds = new HashSet<>();
    int currentUserId = currentUser.getUserId();
    int departmentId = currentUser.getDepartment_id();

    for (User user : userDAO.getUsersByDepartment(departmentId)) {
      if (user == null || user.getUserId() == currentUserId || !user.hasRole(Role.MEMBER)) {
        continue;
      }

      if (seenUserIds.add(user.getUserId())) {
        recipients.add(user);
      }
    }

    return recipients;
  }
}
