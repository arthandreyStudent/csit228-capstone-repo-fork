package com.csit228.capstone.controller;

import com.csit228.capstone.model.Notification;
import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.ListRowItem;
import com.csit228.capstone.utils.UIStyler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DashboardMemberController extends StaffDashboardController {

  @FXML
  public VBox leftSideBarContainer;

  @FXML
  public HBox dashboardRow;

  @FXML
  public HBox myTasksRow;

  @FXML
  public HBox tasksBadgeContainer;

  @FXML
  public Button buttonLogout;

  @FXML
  public Label toDoLabel;

  @FXML
  public Label resolvedLabel;

  @FXML
  public Button allButton;

  @FXML
  public Button availableButton;

  @FXML
  public Button toDoButton;

  @FXML
  public Button inProgressButton;

  @FXML
  public Button overdueButton;
  
  @FXML
  public Button returnedButton;

  @FXML
  public Button completedButton;

  @FXML
  public Button resolvedButton;
  
  @FXML
  public AnchorPane rightPane;
  
  @FXML
  public HBox toDoOverdueContainer;
  
  @FXML
  public HBox inProgressOverdueContainer;
  
  @FXML
  public HBox completedOverdueContainer;
  
  @FXML
  public HBox resolvedOverdueContainer;
  
  @FXML
  private Label inProgressLabel;

  @FXML
  private Label completedLabel;

  @FXML
  private VBox myWorkTicketsBox;

  @FXML
  private VBox volunteerBoardBox;

  @FXML
  private VBox activityBox;

  @Override
  protected String getDefaultRoleName() {
    return "MEMBER";
  }

  @Override
  protected void refreshDashboard() {
    ticketDAO.getTicketViews();
    tickets = new ArrayList<>(ticketDAO.getViews());

    renderDashboard();
  }

  private String currentFilter = "ALL";

  @Override
  protected void renderDashboard() {
    updateSummaryCards();
    renderMyWorkTickets(currentFilter);
    loadVolunteerBoard();
    loadMemberActivity();
  }

  @Override
  protected void onSearchChanged() {
    renderMyWorkTickets(currentFilter);
    loadVolunteerBoard();
  }

  @Override
  protected void onDeadlineSortSelected() {
    renderMyWorkTickets(currentFilter);
    loadVolunteerBoard();
  }

  @FXML
  public void initialize() {
    setupProfile();
    setupSearch();
    setupSideBar();
    setupDeadlineSortComboBox();
    refreshDashboard();
    defaultToAllSelected();
    startWatching();

    UIStyler.applyLeftSideBarGradient(leftSideBarContainer);
  }
  
  private void setupSideBar() {
    dashboardRow.getStyleClass().add("clicked");
  }
  
  @FXML
  public void onDashboardRowClicked() {
    dashboardRow.getStyleClass().remove("clicked");
    myTasksRow.getStyleClass().remove("clicked");
    
    dashboardRow.getStyleClass().add("clicked");
    
    // TODO: Implement screen change from My Tasks back to the Dashboard here. For now we just make
    // the rightPane reappears again.
    rightPane.setVisible(true);
  }
  
  @FXML
  public void onMyTasksRowClicked() {
    dashboardRow.getStyleClass().remove("clicked");
    myTasksRow.getStyleClass().remove("clicked");

    myTasksRow.getStyleClass().add("clicked");
    
    // TODO: Implement My Tasks Pane here. For now we just hide the right pane when My Tasks is clicked
    //  since the main dashboard already surfaces the member's tasks.
    rightPane.setVisible(false);
  }

  private void defaultToAllSelected() {
    // default filter is ALL
    currentFilter = "ALL";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);

  }

  @FXML
  public void onAllFilterClicked(ActionEvent event) {
    currentFilter = "ALL";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);
  }

  @FXML
  public void onAvailableFilterClicked(ActionEvent event) {
    currentFilter = "AVAILABLE";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);
  }

  @FXML
  public void onToDoFilterClicked(ActionEvent event) {
    currentFilter = "TODO";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);
  }

  @FXML
  public void onInProgressFilterClicked(ActionEvent event) {
    currentFilter = "IN_PROGRESS";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);
  }

  @FXML
  public void onOverdueFilterClicked(ActionEvent event) {
    currentFilter = "OVERDUE";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);
  }
  
  @FXML
  public void onReturnedFilterClicked(ActionEvent event) {
    currentFilter = "RETURNED";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);
  }
  
  @FXML
  public void onCompletedFilterClicked(ActionEvent event) {
    currentFilter = "COMPLETED";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);
  }

  @FXML
  public void onResolvedFilterClicked(ActionEvent event) {
    currentFilter = "RESOLVED";
    applyFilterButtonStyle(currentFilter);
    renderMyWorkTickets(currentFilter);
  }

  private void updateSummaryCards() {
    int todoTasks = 0;
    int inProgress = 0;
    int completed = 0;
    int resolved = 0;

    for (TicketView ticket : tickets) {
      if (!isAssignedToCurrentUser(ticket)) {
        continue;
      }

      if (isToDoTicket(ticket))
        todoTasks++;
      if (isInProgress(ticket))
        inProgress++;
      if (isCompleted(ticket))
        completed++;
      if (isResolved(ticket))
        resolved++;

    }

    toDoLabel.setText(String.valueOf(todoTasks));
    inProgressLabel.setText(String.valueOf(inProgress));
    completedLabel.setText(String.valueOf(completed));
    resolvedLabel.setText(String.valueOf(resolved));
    
    updateOverdues();
  }
  
  private void updateOverdues() {
    int overdueCountToDo = 0;
    int overdueCountInProgress = 0;
    int overdueCountCompleted = 0;
    int overdueCountResolved = 0;

    for (TicketView ticket : tickets) {
      if (!isAssignedToCurrentUser(ticket)) {
        continue;
      }

      if (isToDoTicket(ticket) && isOverdueInDeadline(ticket)) {
        overdueCountToDo++;
      } else if (isInProgress(ticket) && isOverdueInDeadline(ticket)) {
        overdueCountInProgress++;
      } else if (isCompleted(ticket) && isOverdueInDeadline(ticket)) {
        overdueCountCompleted++;
      } else if (isResolved(ticket) && isOverdueInDeadline(ticket)) {
        overdueCountResolved++;
      }
    }
    
    if (overdueCountToDo > 0) {
      toDoOverdueContainer.getChildren().setAll(
        makeOverdueCircle(),
        makeOverdueLabel(overdueCountToDo)
      );
    } else {
      toDoOverdueContainer.getChildren().clear();
    }
    
    if (overdueCountInProgress > 0) {
      inProgressOverdueContainer.getChildren().setAll(
        makeOverdueCircle(),
        makeOverdueLabel(overdueCountInProgress)
      );
    } else {
      inProgressOverdueContainer.getChildren().clear();
    }
    
    if (overdueCountCompleted > 0) {
      completedOverdueContainer.getChildren().setAll(
        makeOverdueCircle(),
        makeOverdueLabel(overdueCountCompleted)
      );
    } else {
      completedOverdueContainer.getChildren().clear();
    }
    
    if (overdueCountResolved > 0) {
      resolvedOverdueContainer.getChildren().setAll(
        makeOverdueCircle(),
        makeOverdueLabel(overdueCountResolved)
      );
    } else {
      resolvedOverdueContainer.getChildren().clear();
    }
    
  }
  
  public boolean isOverdueInDeadline(TicketView ticket) {
    if (ticket == null || ticket.getDeadline() == null)
      return false;
    return LocalDate.now().isAfter(ticket.getDeadline().toLocalDate());
  }
  
  private FontIcon makeOverdueCircle() {
    FontIcon circle = new FontIcon("fas-circle");
    circle.setIconSize(7);
    circle.setIconColor(Paint.valueOf("#f55353"));
    
    return circle;
  }
  
  private Label makeOverdueLabel(int overdueCount) {
    Label label = new Label(String.valueOf(overdueCount) + " overdue");
    label.setStyle("-fx-text-fill: #f55353; " +
                   "-fx-font-size: 12px; " +
                   "-fx-font-family: 'Inter 18pt ExtraBold';"
                  );
    
    return label;
  }

  private void renderMyWorkTickets (String filter) {
    myWorkTicketsBox.getChildren().clear();

    String keyword = searchField != null ? searchField.getText() : "";

    for (TicketView ticket : getSortedTicketsByDeadline()) {
      // only tickets assigned to current user OR unassigned tickets but belongs to the same
      // department of the user are shown in the member dashboard ticket table
      if (!(isAssignedToCurrentUser(ticket) || isAvailableUnderDept(ticket)))
        continue;

      // filter by selected condition
      boolean include = switch (filter) {
        case "ALL" -> true;
        case "AVAILABLE" -> isAvailableUnderDept(ticket);
        case "TODO" -> isToDoTicket(ticket);
        case "IN_PROGRESS" -> isInProgress(ticket);
        case "OVERDUE" -> isOverdue(ticket);
        case "RETURNED" -> isReturned(ticket);
        case "COMPLETED" -> isStatus(ticket, TicketStatus.COMPLETED.name());
        case "RESOLVED" -> isResolved(ticket);
        default -> true;
      };

      if (!include)
        continue;

      if (!matchesTicketSearch(ticket, keyword))
        continue;

      // Compute all status booleans once and pass them into the factory.
      // This avoids duplicating the status-detection logic here and in
      // ListRowItem.getDynamicActionButtonInfo.
      boolean availDept       = isAvailableUnderDept(ticket);
      boolean assignedMe      = isAssignedToCurrentUser(ticket);
      boolean inProgress      = isInProgress(ticket);
      boolean completed       = isCompleted(ticket);
      boolean resolved        = isResolved(ticket);
      boolean overdue         = isOverdue(ticket);
      boolean returned        = isReturned(ticket);
      boolean overdueInProg   = isOverdueInProgress(ticket);
      boolean volunteer       = isVolunteerTicket(ticket);
         // IN_PROGRESS && overdue

      ListRowItem row = ListRowItem.forMemberMyWorkTicket(
          ticket, availDept, assignedMe, inProgress, completed, resolved, overdue, returned, overdueInProg, volunteer
      );

      // Derive the ButtonAction so the controller decides the semantics
      // (open modal vs state-transition) rather than hard-coding text strings.
      ListRowItem.ButtonAction action =
          ListRowItem.getDynamicActionButtonInfo(ticket, availDept, assignedMe,
              inProgress, completed, resolved, overdue, returned, overdueInProg, volunteer);

      // Attach the correct handler according to the resolved action type.
      row.setAction(event -> handleMemberAction(ticket, action));

      // Row click (not the button) always opens the detail modal.
      row.setRowClick(event -> {
        if (isInteractiveTarget(event.getTarget())) {
          return;
        }
        openTicketDetailModal(ticket);
      });

      myWorkTicketsBox.getChildren().add(row);
    }
  }

  /**
   * Routes a member's action button click to the correct business operation.
   * <ul>
   *   <li>{@link ButtonAction#TAKE}       — claim ownership via takeTicket</li>
   *   <li>{@link ButtonAction#START_TASK} — transition OPEN → IN_PROGRESS</li>
   *   <li>{@link ButtonAction#SUBMIT}     — transition IN_PROGRESS → COMPLETED</li>
   *   <li>{@link ButtonAction#RESUBMIT}   — re-submit an editor-returned ticket (non-null return_reason) → COMPLETED</li>
   *   <li>{@link ButtonAction#SUBMIT_LATE}— late submission (in-progress work, past deadline)</li>
   *   <li>{@link ButtonAction#VIEW_DETAILS}— open the ticket detail modal (read-only)</li>
   * </ul>
   */
  public void handleMemberAction(TicketView ticket, ListRowItem.ButtonAction action) {
    switch (action) {
      case TAKE:
        // TODO: Use the TicketDAO to assign this ticket's assigned_to to the user_id of the current user
        // that can be get from the AppSession.currentUser.
        // Also update the TicketView's assignedToName.
        
        break;
      case START_TASK:
        // TODO: Update the status of the ticket from OPEN to IN_PROGRESS through the TicketDAO.
        // Also update the TicketView's status
        
        break;
      case SUBMIT:
        // TODO: Update the status of the ticket from IN_PROGRESS to COMPLETED through the TicketDAO.
        // Update the ticket's last_updated to the datetime of when was the ticket submitted.
        // Also update the TicketView's status
        
        break;
      case RESUBMIT:
        // TODO: Update the status of the ticket from IN_PROGRESS to COMPLETED through the TicketDAO.
        // Update the ticket's last_updated to the datetime of when was the ticket submitted.
        // Also update the TicketView's status
        
        break;
      case SUBMIT_LATE:
        // TODO: Update the status of the ticket from IN_PROGRESS to COMPLETED through the TicketDAO.
        // Update the ticket's last_updated to the datetime of when was the ticket submitted.
        // Also update the TicketView's status
        
        break;
      
      case VIEW_DETAILS:
        // Completed / Resolved tickets are read-only.
        // Only open the ticket view modal
        
        break;
    }
  }

  private void applyFilterButtonStyle(String filter) {
    // clear existing style classes related to filters
    allButton.getStyleClass().removeIf(s -> s.equals("all-filter-style"));
    availableButton.getStyleClass().removeIf(s -> s.equals("available-filter-style"));
    toDoButton.getStyleClass().removeIf(s -> s.equals("todo-filter-style"));
    inProgressButton.getStyleClass().removeIf(s -> s.equals("inprogress-filter-style"));
    overdueButton.getStyleClass().removeIf(s -> s.equals("overdue-filter-style"));
    returnedButton.getStyleClass().removeIf(s -> s.equals("returned-filter-style"));
    completedButton.getStyleClass().removeIf(s -> s.equals("completed-filter-style"));
    resolvedButton.getStyleClass().removeIf(s -> s.equals("resolved-filter-style"));

    switch (filter) {
      case "ALL":
        allButton.getStyleClass().add("all-filter-style");
        break;
      case "AVAILABLE":
        availableButton.getStyleClass().add("available-filter-style");
        break;
      case "TODO":
        toDoButton.getStyleClass().add("todo-filter-style");
        break;
      case "IN_PROGRESS":
        inProgressButton.getStyleClass().add("inprogress-filter-style");
        break;
      case "OVERDUE":
        overdueButton.getStyleClass().add("overdue-filter-style");
        break;
      case "RETURNED":
        returnedButton.getStyleClass().add("returned-filter-style");
        break;
      case "COMPLETED":
        completedButton.getStyleClass().add("completed-filter-style");
        break;
      case "RESOLVED":
        resolvedButton.getStyleClass().add("resolved-filter-style");
        break;
    }
  }

  private void loadVolunteerBoard() {
    volunteerBoardBox.getChildren().clear();

    String keyword = searchField != null ? searchField.getText() : "";
    int count = 0;

    for (TicketView ticket : getSortedTicketsByDeadline()) {
      if (!isAvailableTicket(ticket) || !isVolunteerTicket(ticket))
        continue;
      if (!matchesTicketSearch(ticket, keyword))
        continue;

      ListRowItem row = ListRowItem.forMemberVolunteerTicket(ticket);
      row.setAction(event -> volunteerTicket(ticket));
      row.setRowClick(event -> volunteerTicket(ticket));
      volunteerBoardBox.getChildren().add(row);

      if (++count >= 8)
        break;
    }
  }

  private void loadMemberActivity() {
    activityBox.getChildren().clear();

    int count = 0;

    for (TicketView ticket : tickets) {
      if (!isAssignedToCurrentUser(ticket)) continue;

      Notification notification = new Notification(
        ticket.getId(),
        "You have \"" + ticket.getTitle() + "\" with status " + safe(ticket.getStatus()),
        false,                  // isRead
        LocalDateTime.now(),
        getCurrentUserId()
      );

      activityBox.getChildren().add(ListRowItem.forActivity(notification));

      if (++count >= 8)
        break;
    }
  }

  private void volunteerTicket (TicketView ticket) {
    User currentUser = AppSession.currentUser;

    if (currentUser == null) {
      showError("No logged-in user found.");
      return;
    }
    
    openTicketDetailModal(ticket);

//    // Only assign the ticket to the current user. Keep status as OPEN until member clicks "Start Work"
//    boolean assigned = ticketDAO.assignTicket(currentUser.getUserId(), ticket.getId());
//
//    if (assigned) {
//      showInfo("Ticket added to your tasks.");
//      // Explicitly refresh the DAO cache to ensure we get fresh data from the database
//      ticketDAO.getTicketViews();
//      refreshDashboard();
//    } else {
//      showError("Unable to take ticket.");
//    }
  }

  private String safe(String value) {
    return (value == null || value.trim().isEmpty()) ? "N/A" : value.trim();
  }
  
}

