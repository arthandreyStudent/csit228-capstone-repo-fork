package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.ListRowItem;
import com.csit228.capstone.utils.UIStyler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.ArrayList;

public class TicketMemberController extends StaffTicketController {

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
  public Button openButton;
  
  @FXML
  public AnchorPane rightPane;
  
  @FXML
  public HBox inProgressOverdueContainer;
  
  @FXML
  public HBox completedOverdueContainer;
  
  @FXML
  public HBox resolvedOverdueContainer;
  
  @FXML
  public Label openLabel;
  
  @FXML
  private Label inProgressLabel;

  @FXML
  private Label completedLabel;

  @FXML
  private VBox availableTicketsBox;

  @FXML
  private VBox volunteerBoardBox;

  @FXML
  private VBox activityBox;

  private final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
  
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

  private String currentFilter = "OPEN";

  @Override
  protected void renderDashboard() {
    updateSummaryCards();
    renderTicketsTable(currentFilter);
    loadVolunteerBoard();
    loadRecentActivity(activityBox);
  }

  @Override
  protected void onSearchChanged() {
    renderTicketsTable(currentFilter);
    loadVolunteerBoard();
  }

  @Override
  protected void onDeadlineSortSelected() {
    renderTicketsTable(currentFilter);
    loadVolunteerBoard();
  }

  @FXML
  public void initialize() {
    setupProfile();
    setupSearch();
    setupSideBar();
    setupDeadlineSortComboBox();
    refreshDashboard();
    defaultToOpenSelected();
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

  private void defaultToOpenSelected() {
    // default filter is OPEN
    currentFilter = "OPEN";
    applyFilterButtonStyle(currentFilter);
    renderTicketsTable(currentFilter);
  }
  
  @FXML
  public void onOpenFilterClicked() {
    defaultToOpenSelected();
  }

  private void updateSummaryCards() {
    int openTasks = 0;
    int inProgress = 0;
    int completed = 0;
    int resolved = 0;

    for (TicketView ticket : tickets) {
      // only tickets assigned to current user OR unassigned tickets
      // under the same department OR volunteer tickets
      // are shown/countable in the member dashboard
      if (!(isAssignedToCurrentUser(ticket)
            || isAvailableUnderDept(ticket)
            || isVolunteerTicket(ticket)))
        continue;
      
      // Hide overdue OPEN tickets from Summary Cards
      if (isStatus(ticket, TicketStatus.OPEN.name()) && isOverdue(ticket))
        continue;
      
      
      if (isAvailableUnderDept(ticket) || isVolunteerTicket(ticket))
        openTasks++;
      if (isInProgress(ticket))
        inProgress++;
      if (isCompleted(ticket))
        completed++;
      if (isResolved(ticket))
        resolved++;

    }

    openLabel.setText(String.valueOf(openTasks));
    inProgressLabel.setText(String.valueOf(inProgress));
    completedLabel.setText(String.valueOf(completed));
    resolvedLabel.setText(String.valueOf(resolved));
    
    updateOverdue();
  }
  
  private void updateOverdue() {
    int overdueCountInProgress = 0;
    int overdueCountCompleted = 0;
    int overdueCountResolved = 0;

    for (TicketView ticket : tickets) {
      // only tickets assigned to current user OR unassigned tickets but belongs to the same
      // department of the user are shown in the member dashboard ticket table and counted in the summary cards
      if (!(isAssignedToCurrentUser(ticket) || isAvailableUnderDept(ticket)))
        continue;

      if (isInProgress(ticket) && isOverdueInDeadline(ticket)) {
        overdueCountInProgress++;
      } else if (isCompleted(ticket) && isOverdueInDeadline(ticket)) {
        overdueCountCompleted++;
      } else if (isResolved(ticket) && isOverdueInDeadline(ticket)) {
        overdueCountResolved++;
      }
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

  private void renderTicketsTable(String filter) {
    availableTicketsBox.getChildren().clear();

    String keyword = searchField != null ? searchField.getText() : "";

    for (TicketView ticket : getSortedTicketsByDeadline()) {
      // only tickets assigned to current user OR unassigned tickets but belongs to the same
      // department of the user are shown in the member dashboard ticket table
      if (!(isAssignedToCurrentUser(ticket) || isAvailableUnderDept(ticket)))
        continue;
      
      // Hide overdue OPEN tickets from Available Tickets
      if (isStatus(ticket, TicketStatus.OPEN.name()) && isOverdue(ticket))
        continue;

      // filter by selected condition
      boolean include = isAvailableUnderDept(ticket);

      if (!include)
        continue;

      if (!matchesTicketSearch(ticket, keyword))
        continue;

      // Compute all status booleans once and pass them into the factory.
      // This avoids duplicating the status-detection logic here and in
      // ListRowItem.getDynamicActionButtonInfo.
      boolean availDept       = isAvailableUnderDept(ticket);
      boolean inProgress      = isInProgress(ticket);
      boolean completed       = isCompleted(ticket);
      boolean resolved        = isResolved(ticket);
      boolean overdueInProg   = isOverdueInProgress(ticket);
      boolean volunteer       = isVolunteerTicket(ticket);

      ListRowItem row = ListRowItem.forMemberAvailableTicket(
          ticket, availDept, inProgress, completed, resolved, overdueInProg, volunteer
      );

      // Derive the ButtonAction so the controller decides the semantics
      // (open modal vs state-transition) rather than hard-coding text strings.
      ListRowItem.ButtonAction action =
          ListRowItem.getDynamicActionButtonInfo(ticket, availDept,
              inProgress, completed, resolved, overdueInProg, volunteer);

      // Attach the correct handler according to the resolved action type.
      row.setAction(event -> handleDetailModalAction(ticket, action));

      // Row click (not the button) always opens the detail modal.
      row.setRowClick(event -> {
        if (isInteractiveTarget(event.getTarget())) {
          return;
        }
        openTicketDetailModal(ticket);
      });

      availableTicketsBox.getChildren().add(row);
    }
  }

  /**
   * Handles the primary (right-bar) action button placed by
   * {@link ListRowItem#getDynamicActionButtonInfo(ListRowItem.ButtonAction)}
   * inside each member "My Work" table row.
   *
   * <ul>
   *   <li>{@link ButtonAction#TAKE}        — claim ownership via takeTicket</li>
   *   <li>{@link ButtonAction#START_TASK}  — transition OPEN → IN_PROGRESS</li>
   *   <li>{@link ButtonAction#SUBMIT}      — transition IN_PROGRESS → COMPLETED</li>
   *   <li>{@link ButtonAction#RESUBMIT}    — IN_PROGRESS (returned) → COMPLETED</li>
   *   <li>{@link ButtonAction#SUBMIT_LATE} — late submission → COMPLETED</li>
   *   <li>{@link ButtonAction#VIEW_DETAILS}— open the detail modal (read-only)</li>
   *   <li>{@link ButtonAction#BACK}        — open the detail modal</li>
   * </ul>
   *
   * The DAO logic here mirrors {@code BaseTicketDetailModalController.handleDetailModalAction}
   * so that row-level and modal-level actions always behave identically.
   */
  public void handleDetailModalAction(TicketView ticket, ListRowItem.ButtonAction action) {
    switch (action) {
      case TAKE -> {
        boolean ok = ticketDAO.assignTicket(getCurrentUserId(), ticket.getId());
        if (ok) {
          showInfo(getSuccessMessage(action));
          refreshDashboard();
        } else {
          showError("Unable to take ticket.");
        }
      }
      case SUBMIT, SUBMIT_LATE -> {
        boolean ok1 = ticketDAO.updateStatus(ticket.getId(),
            com.csit228.capstone.enums.TicketStatus.COMPLETED);
        boolean ok2 = ticketDAO.setLastUpdated(ticket.getId(), java.time.LocalDateTime.now());
        if (ok1 && ok2) {
          showInfo(getSuccessMessage(action));
          refreshDashboard();
        } else {
          showError("Unable to submit ticket.");
        }
      }
      case VIEW_DETAILS -> {
        // Completed / Resolved tickets are read-only; back/detail clicks
        // simply re-open the detail modal so the member can read notes.
        openTicketDetailModal(ticket);
      }
    }
  }
  
  private String getSuccessMessage(ListRowItem.ButtonAction action) {
    return switch (action) {
      case VOLUNTEER -> "You volunteered for the ticket.";
      case TAKE -> "Ticket successfully assigned to you.";
      case SUBMIT -> "Ticket submitted successfully.";
      case SUBMIT_LATE -> "Late ticket submitted successfully.";
      default -> null;
    };
  }

  private void applyFilterButtonStyle(String filter) {
    // clear existing style classes related to filters
    openButton.getStyleClass().removeIf(s -> s.equals("open-filter-style"));
    
    if (filter.equals("OPEN")) {
      openButton.getStyleClass().add("open-filter-style");
    }
  }

  private void loadVolunteerBoard() {
    volunteerBoardBox.getChildren().clear();

    String keyword = searchField != null ? searchField.getText() : "";
    int count = 0;

    for (TicketView ticket : getSortedTicketsByDeadline()) {
      if (!isAvailableTicket(ticket) || !ticket.isVolunteerTicket())
        continue;
      if (!matchesTicketSearch(ticket, keyword))
        continue;
      // Hide overdue OPEN tickets from Available Tickets
      if (isStatus(ticket, TicketStatus.OPEN.name()) && isOverdue(ticket))
        continue;

      ListRowItem row = ListRowItem.forMemberVolunteerTicket(ticket);
      row.setAction(event -> volunteerTicket(ticket));
      row.setRowClick(event -> volunteerTicket(ticket));
      volunteerBoardBox.getChildren().add(row);

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
  }

  private boolean isFromUserDepartment(TicketView ticket, int userDeptId) {
    if (userDeptId <= 0) return true;
    String userDeptName = departmentDAO.getDepartmentByID(userDeptId) != null
            ? departmentDAO.getDepartmentByID(userDeptId).getName()
            : null;
    if (userDeptName == null) return true;
    return userDeptName.equalsIgnoreCase(ticket.getDepartmentName());
  }
  
  private String safe(String value) {
    return (value == null || value.trim().isEmpty()) ? "N/A" : value.trim();
  }
  
 
}

