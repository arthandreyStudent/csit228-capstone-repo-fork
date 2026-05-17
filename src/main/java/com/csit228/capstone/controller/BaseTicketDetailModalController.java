package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.enums.Role;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.Ticket;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.ListRowItem;
import com.csit228.capstone.utils.UIStyler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BaseTicketDetailModalController {
  
  @FXML
  public Button buttonClose;
  @FXML
  public HBox ticketHeader;
  @FXML
  public VBox statusBadgeContainer;
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
  @FXML
  public HBox activityHeaderContainer;
  @FXML
  public HBox bottomButtonsContainer;
  @FXML
  public Label ticketDesc;
  @FXML
  public HBox activityContainer;
  @FXML
  public ScrollPane activityContentScrollPane;
  @FXML
  public HBox volunteerMsgBoxContainer;
  
  private Runnable onTicketMutated;
  
  private TicketView currentTicket;
  
  private User currentUser = AppSession.currentUser;
  private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
  
  public void setOnTicketMutated(Runnable onTicketMutated) {
    this.onTicketMutated = onTicketMutated;
  }
  
  public enum ButtonAction {
    BACK("Back", "fas-long-arrow-alt-left", "#f1eded", "#1d1d1f"),
    
    VOLUNTEER("Volunteer", "fas-hand-paper", "#50CD89", "#ffffff"),
    /**
     * Ticket is unassigned and available (OPEN, unassigned).
     * Action: claim ownership ("Take").
     */
    TAKE("Take", "fas-hand-holding", "#48c7cb", "#e6f2ff"),
    
    /**
     * Ticket is owned by current user, status IN_PROGRESS (first cycle) — work is active.
     * Action: mark as COMPLETED ("Submit").
     */
    SUBMIT("Submit", "fas-check", "#4bcc8a", "#dcffef"),
    
    /**
     * Ticket deadline has passed and is overdue.
     * Action: same as Submit but labelled to signal late submission.
     */
    SUBMIT_LATE("Submit Late", "fas-clock", "#ff9900", "#ffedcc"),
    
    /**
     * Ticket is COMPLETED or RESOLVED — read-only display.
     * Action: open detail modal (no state change).
     */
    VIEW_DETAILS("View Details", "fas-eye", "#7f77dd", "#ecebf9");
    
    public final String text;
    public final String iconLiteral;
    public final String bgColor;
    public final String textColor;
    
    ButtonAction(String text, String iconLiteral, String bgColor, String textColor) {
      this.text = text;
      this.iconLiteral = iconLiteral;
      this.bgColor = bgColor;
      this.textColor = textColor;
    }
  }
  
  /**
   * Determines the dynamic button action for a ticket in the member "My Work" table.
   *
   * <p>Priority order (highest to lowest):
   * <ol>
   *   <li>Completed / Resolved → VIEW_DETAILS</li>
   *   <li>Overdue In Progress (IN_PROGRESS AND overdue) → SUBMIT_LATE</li>
   *   <li>Editor-returned (IN_PROGRESS with non-null return_reason) → RESUBMIT</li>
   *   <li>In Progress (regular active work) → SUBMIT</li>
   *   <li>Overdue (OPEN + overdue) → SUBMIT_LATE</li>
   *   <li>Owned + Open (To Do) → START_TASK</li>
   *   <li>Available (unassigned dept ticket) → TAKE</li>
   *   <li>Otherwise → default action (fallback)</li>
   * </ol>
   *
   * Hanging-comment parameters allow callers to inject the related status helpers
   * without imposing an internal dependency (keeps {@code ListRowItem} dep-free).
   */
  public static List<ButtonAction> getDynamicTicketModalButton(
    TicketView ticket,
    boolean isAvailableUnderDept,
    boolean isInProgress,
    boolean isCompleted,
    boolean isResolved,
    boolean isOverdue,
    boolean isOverdueInProgress,
    boolean isVolunteer) {
    
    if (isCompleted || isResolved) return List.of(ButtonAction.BACK);
    
    // SUBMIT_LATE takes priority over SUBMIT/RESUBMIT so that an overdue in-progress
    // ticket surfaces a "Submit Late" button (late-submission action) rather than the
    // plain "Submit" button.
    if (isOverdueInProgress) return List.of(ButtonAction.BACK, ButtonAction.SUBMIT_LATE);
    
    if (isInProgress) {
      return List.of(ButtonAction.BACK, ButtonAction.SUBMIT);
    }
    
    if (isVolunteer) return List.of(ButtonAction.BACK, ButtonAction.VOLUNTEER);
    
    // Unassigned, OPEN, department-level availability
    if (isAvailableUnderDept) return List.of(ButtonAction.BACK, ButtonAction.TAKE);
    
    return List.of(ButtonAction.BACK, ButtonAction.SUBMIT); // sensible fallback
  }
  
  /**
   * Creates a themed action button from a {@link ListRowItem.ButtonAction} descriptor.
   * A {@link FontIcon} is placed on the left side of the button text.
   */
  private static Button makeActionButton(ButtonAction action) {
    // minimum width so the pill retains visual weight when stretched
    double btnMinWidth = action.text.length() * 8.5 + 30;

    Button button = new Button(action.text);
    button.setMinWidth(btnMinWidth);           // never shrink below natural text width
    button.setPrefWidth(Region.USE_COMPUTED_SIZE); // default to natural size
    button.setMaxWidth(Double.MAX_VALUE);       // unrestrained — HBox can freely grow this
    button.setPrefHeight(32.0);
    button.setCursor(Cursor.HAND);
    button.setStyle(
      "-fx-background-color: " + action.bgColor + ";" +
      "-fx-background-radius: 7;" +
      "-fx-text-fill: " + action.textColor + ";" +
      "-fx-font-size: 12px;" +
      "-fx-font-family: 'Inter 18pt ExtraBold'"
    );

    // Icon on the left
    FontIcon icon = new FontIcon(action.iconLiteral);
    icon.setIconSize(14);
    icon.setIconColor(javafx.scene.paint.Color.web(action.textColor));
    button.setGraphic(icon);
    button.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
    button.setGraphicTextGap(5);

    return button;
  }
  
  @FXML
  public void initialize() {
    // Sets the gradient of the header to match the branding colors of the app.
    // This is done programmatically to allow for easier adjustments in the future without needing to modify
    // the FXML or add new resources.
    UIStyler.applyNavyBlueHeaderGradient(ticketHeader);
  }
  
  public void loadTicket(TicketView ticket) {
    this.currentTicket = ticket;
    
    if (ticket != null) {
      ticketCode.setText("#TIX-" + String.format("%03d", ticket.getId()));
      ticketTitle.setText(ticket.getTitle() != null ? ticket.getTitle() : "Untitled Ticket");

      populateBadges(ticket);
      ticketDesc.setText(ticket.getDescription());
      handleAssignedAndCreatedSection(ticket);
      handleVolunteerMsgBox(ticket);
      handleTimeLine(ticket);
      handleActivitySection(ticket);
      departmentLabel.setText(ticket.getDepartmentName());
      handleButtons(ticket);
    }
  }
  
  private void handleVolunteerMsgBox(TicketView ticket) {
    
    if (isVolunteerTicket(ticket)) {
      volunteerMsgBoxContainer.setStyle("-fx-background-color: #d1ffdb;" +
                                        "-fx-background-radius: 10px;");
      
      FontIcon icon = new FontIcon("fas-info-circle");
      icon.setIconSize(14);
      icon.setIconColor(Color.web("#50CD89"));
      
      Label label = new Label("Unassigned · Open for volunteering");
      label.setStyle("-fx-text-fill: #50CD89;" +
                      "-fx-font-family: 'Inter 18pt ExtraBold';" +
                      "-fx-font-size: 12px;");
      
      volunteerMsgBoxContainer.getChildren().addAll(icon, label);
      
    } else {
      volunteerMsgBoxContainer.setManaged(false);
      volunteerMsgBoxContainer.setVisible(false);
    }
  }
  
  private void handleButtons(TicketView ticket) {
    switch (currentUser.getRole()) {
      case MEMBER:
        handleButtonsForMember(ticket);
        break;
      case EDITOR:
        handleButtonsForEditor(ticket);
        break;
      case EXECUTIVE:
        handleButtonsForExecutive(ticket);
        break;
      default:
        break;
    }
  }
  
  private void handleButtonsForMember(TicketView ticket) {
    // Compute all status booleans once and pass them into the factory.
    // This avoids duplicating the status-detection logic here and in
    // ListRowItem.getDynamicActionButtonInfo.
    boolean availDept       = isAvailableUnderDept(ticket);
    boolean inProgress      = isInProgress(ticket);
    boolean completed       = isCompleted(ticket);
    boolean resolved        = isResolved(ticket);
    boolean overdue         = isOverdue(ticket);
    boolean overdueInProg   = isOverdueInProgress(ticket);   // IN_PROGRESS && overdue
    boolean volunteer       = isVolunteerTicket(ticket);
    
    List<ButtonAction> actions = getDynamicTicketModalButton(
      ticket, availDept, inProgress, completed, resolved, overdue, overdueInProg, volunteer
    );
    
    // BACK button is actions[0], primary action button is actions[1]
    // The order guarantees from getDynamicActionButtonInfo.
    
    // --- Left container: BACK (closes modal, no data mutation)
    Button backButton = (actions.size() > 0)
                        ? makeActionButton(actions.get(0)) : null;
    if (backButton != null) {
      leftButtonContainer.getChildren().setAll(backButton);
      HBox.setHgrow(backButton, Priority.ALWAYS);
      backButton.setOnAction(e -> closeModal(backButton));
    }
    
    // --- Right container: primary action (Submit / Resubmit / Start / Take / etc.)
    Button actionButton = (actions.size() > 1)
                          ? makeActionButton(actions.get(1)) : null;
    if (actionButton != null) {
      rightButtonContainer.getChildren().setAll(actionButton);
      HBox.setHgrow(actionButton, Priority.ALWAYS);
      actionButton.setOnAction(e -> handleDetailModalAction(ticket, actions.get(1), actionButton));
    } else {
      rightButtonContainer.setManaged(false);
      rightButtonContainer.setVisible(false);
    }
  }
  
  private void handleButtonsForEditor(TicketView ticket) {
    // Implement mga buttons ari sa ticket modal view paras editor
  }
  
  private void handleButtonsForExecutive(TicketView ticket) {
    // Implement mga buttons ari sa ticket modal view paras executive
  }
  
  /**
   * Handles the primary (right-bar) action button inside the ticket detail modal.
   * Each {@link ButtonAction} maps to one or more {@link TicketDAO} mutations.
   * BACK is handled inline as {@link #closeModal(Button)}; all other transitions
   * land here.
   * <p>
   * After every successful mutation the modal is reloaded via
   * {@link #loadTicket(TicketView)} so that badges, notices, and the button bar
   * all reflect the new state without the caller having to do it manually.
   */
  private void handleDetailModalAction(TicketView ticket,
                                       ButtonAction action,
                                       Button sourceButton) {
    User currentUser = AppSession.currentUser;
    if (currentUser == null) {
      showError("No logged-in user found.");
      return;
    }

    boolean mutated = switch (action) {
      case VOLUNTEER -> {
        // Assign the volunteer ticket to the current member; keep status OPEN
        // until they explicitly click "Start Work".
        boolean ok =
          ticketDAO.assignTicket(currentUser.getUserId(), ticket.getId()) &&
          ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
        yield ok;
      }
      case TAKE -> {
        // Same as VOLUNTEER but surfaced for department-scoped unassigned tickets.
        boolean ok = ticketDAO.assignTicket(currentUser.getUserId(), ticket.getId()) &&
                     ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
        yield ok;
      }
      case SUBMIT, SUBMIT_LATE -> {
        // IN_PROGRESS → COMPLETED: member is submitting the finished ticket.
        // last_updated is set to "now" to mark the submission time.
        boolean ok1 = ticketDAO.updateStatus(ticket.getId(), TicketStatus.COMPLETED);
        boolean ok2 = ticketDAO.setLastUpdated(ticket.getId(), LocalDateTime.now());
        yield ok1 && ok2;
      }
      default -> {
        // VIEW_DETAILS, BACK and any unrecognised action carry no mutation.
        yield true;
      }
    };

    if (mutated) {
      // Pull fresh data from the DAO so badges / notices re-render correctly.
      ticketDAO.getTicketViews();
      
      if (onTicketMutated != null) {
        onTicketMutated.run();
      }
      
      String successMessage = getSuccessMessage(action);
      
      if (successMessage != null) {
        showInfo(successMessage);
      }
      
      closeModal(sourceButton);
    } else {
      showError("Action failed — please try again.");
    }
  }
  
  private String getSuccessMessage(ButtonAction action) {
    return switch (action) {
      case VOLUNTEER -> "You volunteered for the ticket.";
      case TAKE -> "Ticket successfully assigned to you.";
      case SUBMIT -> "Ticket submitted successfully.";
      case SUBMIT_LATE -> "Late ticket submitted successfully.";
      default -> null;
    };
  }

  private void showInfo(String message) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
        javafx.scene.control.Alert.AlertType.INFORMATION);
    alert.setTitle("TIX.org");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showError(String message) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
        javafx.scene.control.Alert.AlertType.ERROR);
    alert.setTitle("TIX.org");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
  
  private void handleTimeLine(TicketView ticket) {
    handleCreatedDateTime(ticket);
    handleLastUpdatedDateTime(ticket);
    handleDeadlineDateTime(ticket);
  }
  
  private void handleCreatedDateTime(TicketView ticket) {
    if (ticket.getDateCreated() != null) {
      createdDateLabel.setText(ticket.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
      createdTimeLabel.setText(ticket.getDateCreated().format(DateTimeFormatter.ofPattern("hh:mm a")));
    } else {
      createdDateLabel.setText("N/A");
      createdTimeLabel.setText("");
    }
  }
  
  private void handleLastUpdatedDateTime(TicketView ticket) {
    if (ticket.getLastUpdated() != null) {
      lastUpdatedDateLabel.setText(ticket.getLastUpdated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
      lastUpdatedTimeLabel.setText(ticket.getLastUpdated().format(DateTimeFormatter.ofPattern("hh:mm a")));
    } else {
      lastUpdatedDateLabel.setText("N/A");
      lastUpdatedTimeLabel.setText("");
    }
  }
  
  private void handleDeadlineDateTime(TicketView ticket) {
    if (ticket.getDeadline() != null) {
      deadlineDateLabel.setText(ticket.getDeadline().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
      deadlineTimeLabel.setText(ticket.getDeadline().format(DateTimeFormatter.ofPattern("hh:mm a")));
    } else {
      deadlineDateLabel.setText("N/A");
      deadlineTimeLabel.setText("");
    }
  }
  
  private void handleActivitySection(TicketView ticket) {
    Role currentRole = currentUser.getRole();

    switch (currentRole) {
      case MEMBER:
        activityHeaderContainer.setVisible(false);
        activityHeaderContainer.setManaged(false);
        activityContainer.setVisible(false);
        activityContainer.setManaged(false);
        // Explicitly collapse the ScrollPane so the outer VBox has no residual
        // preferred-height contribution once this hide-visibility toggle fires.
        if (activityContentScrollPane != null) {
          activityContentScrollPane.setPrefHeight(0);
        }
        break;
      case EDITOR:
      case EXECUTIVE:
        activityHeaderContainer.setVisible(true);
        activityHeaderContainer.setManaged(true);
        activityContainer.setVisible(true);
        activityContainer.setManaged(true);
        // Restore natural height so the ScrollPane sizes to its content.
        if (activityContentScrollPane != null) {
          activityContentScrollPane.setPrefHeight(ScrollPane.USE_COMPUTED_SIZE);
        }

        // I-implement pa na makita ang comment threads here

        break;
    }

  }
  
  private void handleAssignedAndCreatedSection(TicketView ticket) {
    Role currentRole = currentUser.getRole();
    
    switch (currentRole) {
      case MEMBER:
        memberAssignedAndCreateSection(ticket);
        break;
      case EDITOR:
        editorAssignedAndCreateSection(ticket);
        break;
      case EXECUTIVE:
        executiveAssignedAndCreateSection(ticket);
        break;
      default:
        break;
    }
  }
  
  private void memberAssignedAndCreateSection(TicketView ticket) {
    assignedToSubHeader.setText("CURRENTLY ASSIGNED TO");
    
    if (isUnassigned(ticket)) {
      Label noAssigned = new Label("N/A");
      noAssigned.setStyle("-fx-text-fill: #F64E60;" +
                          "-fx-font-family: 'Inter 18pt Medium';" +
                          "-fx-font-size: 12px;"
                          );
      
      assignedContainerOne.getChildren().add(noAssigned);
    } else {
      Label assigned = new Label("You");
      assigned.setStyle("-fx-text-fill: #252525;" +
                        "-fx-font-family: 'Inter 18pt Medium';" +
                        "-fx-font-size: 12px;"
      );
      assignedContainerOne.getChildren().add(assigned);
    }
    
    createdBy.setText(ticket.getCreatedBy());
  }
  
  private void editorAssignedAndCreateSection(TicketView ticket) {
    // Implement
  }
  
  private void executiveAssignedAndCreateSection(TicketView ticket) {
    // Implement
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
    String displayStatus = status.replace("_", " ");
    
    statusBadgeContainer.getChildren().add(UIStyler.makeStatusBadge(displayStatus, 11));
    
    if (isOverdueInProgress(ticket)) {
      statusBadgeContainer.getChildren().add(UIStyler.makeOverdueBadge(11));
    }
    
    // Priority Badge
    String priority = ticket.getPriority() != null ? ticket.getPriority().toUpperCase() : "MEDIUM";

    priorityBadgeContainer.getChildren().add(UIStyler.makePriorityBadge(priority, 11));
  }
  
  @FXML
  public void onClickedButtonClose() {
    closeModal(buttonClose);
  }
  
  protected void closeModal(Button sourceButton) {
    if (sourceButton != null && sourceButton.getScene() != null &&
        sourceButton.getScene().getWindow() instanceof Stage stage) {
      stage.close();
    }
  }
  
  protected boolean isVolunteerTicket(TicketView ticket) {
    if (ticket == null)
      return false;
    String dept = ticket.getDepartmentName();
    return dept.equalsIgnoreCase("Volunteer") && isUnassigned(ticket) && isStatus(ticket, TicketStatus.OPEN.name());
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
}
