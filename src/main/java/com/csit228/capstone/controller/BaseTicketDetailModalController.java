package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.Role;
import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.utils.ListRowItem;
import com.csit228.capstone.utils.UIStyler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
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
  
  private TicketView currentTicket;
  
  private User currentUser = AppSession.currentUser;
  
  public enum ButtonAction {
    BACK("Back", "fas-long-arrow-alt-left", "#f1eded", "#1d1d1f"),
    
    VOLUNTEER("Volunteer", "fas-hand-paper", "#50CD89", "#ffffff"),
    /**
     * Ticket is unassigned and available (OPEN, unassigned).
     * Action: claim ownership ("Take").
     */
    TAKE("Take", "fas-hand-holding", "#48c7cb", "#e6f2ff"),
    
    /**
     * Ticket is owned by current user, status OPEN — has not been started yet.
     * Action: transition to IN_PROGRESS ("Start Task").
     */
    START_TASK("Start Task", "fas-play", "#2f95ff", "#e6f2ff"),
    
    /**
     * Ticket is owned by current user, status IN_PROGRESS (first cycle) — work is active.
     * Action: mark as COMPLETED ("Submit").
     */
    SUBMIT("Submit", "fas-check", "#4bcc8a", "#dcffef"),
    
    /**
     * Ticket has a non-null return_reason and was returned by an editor for revisions.
     * Action: re-submit as COMPLETED ("Resubmit").
     */
    RESUBMIT("Resubmit", "fas-reply-all", "#f14d5a", "#ffe0e5"),
    
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
  public static List<ButtonAction> getDynamicActionButtonInfo(
    TicketView ticket,
    boolean isAvailableUnderDept,
    boolean isAssignedToCurrentUser,
    boolean isInProgress,
    boolean isCompleted,
    boolean isResolved,
    boolean isOverdue,
    boolean isReturned,
    boolean isOverdueInProgress,
    boolean isVolunteer) {
    
    if (isCompleted || isResolved) return List.of(ButtonAction.BACK);
    
    // SUBMIT_LATE takes priority over SUBMIT/RESUBMIT so that an overdue in-progress
    // ticket surfaces a "Submit Late" button (late-submission action) rather than the
    // plain "Submit" button.
    if (isOverdueInProgress) return List.of(ButtonAction.BACK, ButtonAction.SUBMIT_LATE);
    
    if (isInProgress) {
      // An editor-returned ticket carries a non-null return_reason; surface "Resubmit"
      // to distinguish it from a normal active work ticket.
      if (isReturned)
        return List.of(ButtonAction.BACK, ButtonAction.RESUBMIT);
      return List.of(ButtonAction.BACK, ButtonAction.SUBMIT);
    }
    
    if (isOverdue) return List.of(ButtonAction.BACK, ButtonAction.SUBMIT_LATE);
    
    if (isVolunteer) return List.of(ButtonAction.BACK, ButtonAction.VOLUNTEER);
    
    // Owned and still OPEN
    if (isAssignedToCurrentUser) return List.of(ButtonAction.BACK, ButtonAction.START_TASK);
    
    // Unassigned, OPEN, department-level availability
    if (isAvailableUnderDept) return List.of(ButtonAction.BACK, ButtonAction.TAKE);
    
    return List.of(ButtonAction.BACK, ButtonAction.START_TASK); // sensible fallback
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
      handleChangesRequestedNotice(ticket);
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
    boolean overdueInProg   = isOverdueInProgress(ticket);   // IN_PROGRESS && overdue
    boolean volunteer       = isVolunteerTicket(ticket);

    List<ButtonAction> actions = getDynamicActionButtonInfo(
      ticket, availDept, assignedMe,
      inProgress, completed, resolved, overdue, returned, overdueInProg, volunteer
    );

    // BACK button is actions[0], primary action button is actions[1]
    // The order guarantees from getDynamicActionButtonInfo.

    // --- Left container: BACK (cancels/returns, strongly anchored left)
    Button backButton = (actions.size() > 0)
        ? makeActionButton(actions.get(0)) : null;
    if (backButton != null) {
      leftButtonContainer.getChildren().add(backButton);
      HBox.setHgrow(backButton, Priority.ALWAYS);
    }

    // --- Right container: primary action (Submit / Resubmit / Start / Take / etc.)
    Button actionButton = (actions.size() > 1)
        ? makeActionButton(actions.get(1)) : null;
    if (actionButton != null) {
      rightButtonContainer.getChildren().add(actionButton);
      // Let the action button absorb every remaining pixel in its HBox.
      // makeActionButton already leaves maxWidth unbounded (Double.MAX_VALUE).
      HBox.setHgrow(actionButton, Priority.ALWAYS);
    } else {
      rightButtonContainer.setManaged(false);
      rightButtonContainer.setVisible(false);
    }
  }
  
  public void handleMemberAction(TicketView ticket, ButtonAction action) {
    switch (action) {
      case VOLUNTEER:
        // TODO: Use the TicketDAO to assign this ticket's assigned_to to the user_id of the current user
        // that can be get from the AppSession.currentUser.
        // Also update the TicketView's assignedToName.
        
        break;
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
      case BACK:
        // Completed / Resolved tickets are read-only; open the detail modal.
        // Just handle the BACK button where when clicked, it will close the opened modal.

        break;
    }
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

        // Implement more

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
  
  private void handleChangesRequestedNotice(TicketView ticket) {
    changesRequestedNoticeContainer.getChildren().clear();
    changesRequestedNoticeContainer.setVisible(false);
    changesRequestedNoticeContainer.setManaged(false);

    // Also hide the parent HBox that wraps the notice container to completely remove its footprint
    if (changesRequestedNoticeContainer.getParent() instanceof HBox) {
      changesRequestedNoticeContainer.getParent().setVisible(false);
      changesRequestedNoticeContainer.getParent().setManaged(false);
    }

    // Only show this notice to MEMBER role users
    if (AppSession.currentUser == null || !AppSession.currentUser.getRole().name().equals("MEMBER")) {
      return;
    }

    // A ticket has been returned by an editor when return_reason is non-null and the
    // ticket is in IN_PROGRESS status (the only state the editor can return it to).
    if (ticket.getStatus() == null ||
        !ticket.getStatus().equals("IN_PROGRESS") ||
        (ticket.getReturnReason() == null || ticket.getReturnReason().isBlank())) {
      return;
    }

    String returnReason = ticket.getReturnReason().trim();
    String returnedDate = ticket.getLastUpdated() != null
        ? ticket.getLastUpdated().format(DateTimeFormatter.ofPattern("MMM d, yyyy • hh:mm a"))
        : "Recently";

    // IF WE REACH HERE, WE MUST SHOW IT.
    changesRequestedNoticeContainer.setVisible(true);
    changesRequestedNoticeContainer.setManaged(true);
    if (changesRequestedNoticeContainer.getParent() instanceof HBox) {
      changesRequestedNoticeContainer.getParent().setVisible(true);
      changesRequestedNoticeContainer.getParent().setManaged(true);
    }

    VBox noticeBox = new VBox();
    noticeBox.setSpacing(15);
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
      "-fx-text-fill: #4B5563; -fx-font-family: 'Inter 18pt Regular'; -fx-font-size: 11px; -fx-line-spacing: 3px;");

    // The actual reason text stored in the return_reason column
    Label reasonLabel = new Label(returnReason);
    reasonLabel.setWrapText(true);
    reasonLabel.setStyle(
      "-fx-text-fill: #374151; -fx-font-family: 'Inter 18pt SemiBold'; -fx-font-size: 12px; -fx-line-spacing: 3px;");

    Label footerLabel = new Label("Returned • " + returnedDate);
    footerLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-family: 'Inter 18pt Regular'; -fx-font-size: 12px;");

    noticeBox.getChildren().addAll(headerBox, bodyLabel, reasonLabel, footerLabel);

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
    String displayStatus = status.replace("_", " ");
    
    statusBadgeContainer.getChildren().add(UIStyler.makeStatusBadge(displayStatus, 11));
    
    if (isOverdueInProgress(ticket)) {
      statusBadgeContainer.getChildren().add(UIStyler.makeOverdueBadge(11));
    }
    
    if (isReturned(ticket)) {
      statusBadgeContainer.getChildren().add(UIStyler.makeReturnedBadge(11));
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
}
