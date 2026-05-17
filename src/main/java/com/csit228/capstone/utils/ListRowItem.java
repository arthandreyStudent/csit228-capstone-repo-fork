package com.csit228.capstone.utils;

import com.csit228.capstone.model.Notification;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

public class ListRowItem extends VBox {

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
  public static ButtonAction getDynamicActionButtonInfo(
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
        
    
    if (isCompleted || isResolved) return ButtonAction.VIEW_DETAILS;
    
    // SUBMIT_LATE takes priority over SUBMIT/RESUBMIT so that an overdue in-progress
    // ticket surfaces a "Submit Late" button (late-submission action) rather than the
    // plain "Submit" button.
    if (isOverdueInProgress) return ButtonAction.SUBMIT_LATE;
    
    if (isInProgress) {
      // An editor-returned ticket carries a non-null return_reason; surface "Resubmit"
      // to distinguish it from a normal active work ticket.
      if (isReturned)
        return ButtonAction.RESUBMIT;
      return ButtonAction.SUBMIT;
    }
    
    if (isOverdue) return ButtonAction.SUBMIT_LATE;
    
    // Owned and still OPEN
    if (isAssignedToCurrentUser) return ButtonAction.START_TASK;
    
    // Unassigned, OPEN, department-level availability
    if (isAvailableUnderDept) return ButtonAction.TAKE;
    
    if (isVolunteer) return ButtonAction.VOLUNTEER;
    
    return ButtonAction.START_TASK; // sensible fallback
  
  }

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");

  private static final double TABLE_ROW_WIDTH = 850.0;

  private static final double MEMBER_DETAILS_WIDTH = 230.0;
  private static final double MEMBER_PRIORITY_WIDTH = 100.0;
  private static final double MEMBER_DEADLINE_WIDTH = 140.0;
  private static final double MEMBER_STATUS_WIDTH = 150.0;
  private static final double MEMBER_ACTION_WIDTH = 210.0;

  private static final double EXEC_DETAILS_WIDTH = 230.0;
  private static final double EXEC_DEPT_WIDTH = 122.0;
  private static final double EXEC_PRIORITY_WIDTH = 90.0;
  private static final double EXEC_DEADLINE_WIDTH = 130.0;
  private static final double EXEC_ASSIGN_WIDTH = 160.0;
  private static final double EXEC_ACTION_WIDTH = 65.0;

  private static final double EDITOR_DETAILS_WIDTH = 210.0;
  private static final double EDITOR_ASSIGN_WIDTH = 160.0;
  private static final double EDITOR_STATUS_WIDTH = 95.0;
  private static final double EDITOR_PRIORITY_WIDTH = 85.0;
  private static final double EDITOR_DEADLINE_WIDTH = 170.0;
  private static final double EDITOR_ACTIONS_WIDTH = 110.0;

  private static final double SMALL_CARD_WIDTH = 299.0;
  private static final double SMALL_CARD_TEXT_WIDTH = 244.0;

  private Object sourceObject;
  private Button actionButton;
  private Button secondaryActionButton;
  private Button thirdActionButton;
  private ComboBox<User> assignComboBox;

  private ListRowItem() {
    setFillWidth(true);
    setStyle("-fx-background-color: transparent;");
  }
  
  public static ListRowItem forMemberMyWorkTicket(TicketView ticket) {
    return forMemberMyWorkTicket(ticket,
        false,   // isAvailableUnderDept — default, overridden in controller
        false,   // isAssignedToCurrentUser
        false,   // isInProgress
        false,   // isCompleted
        false,   // isResolved
        false,   // isOverdue
        false,   // returned
        false,   // isOverdueInProgress
        false);  // isVolunteer
  }


  /**
   * Convenience overload that accepts computed status flags so the caller (the
   * {@code DashboardMemberController}) drives the button state rather than this
   * utility class reaching into a session singleton.
   */
  public static ListRowItem forMemberMyWorkTicket(
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

    ListRowItem item = new ListRowItem();
    item.sourceObject = ticket;

    HBox row = new HBox();
    row.setPrefWidth(TABLE_ROW_WIDTH);
    row.setMinWidth(TABLE_ROW_WIDTH);
    row.setMaxWidth(TABLE_ROW_WIDTH);
    // No fixed height — row grows naturally when the status column stacks two badges
    row.setMinHeight(USE_COMPUTED_SIZE);
    row.setPrefHeight(USE_COMPUTED_SIZE);
    row.setMaxHeight(USE_COMPUTED_SIZE);
    row.setPadding(new Insets(12, 0, 12, 0));
    row.setAlignment(Pos.CENTER_LEFT);
    row.setCursor(Cursor.HAND);
    row.setStyle("-fx-background-color: white; " +
                 "-fx-border-color: #eef2fb; " +
                 "-fx-border-width: 1 0 0 0;"
                );

    String createdBy = ticket.getCreatedBy() != null ? ticket.getCreatedBy() : "Unknown";
    String ticketNum = String.format("%03d", ticket.getId());

    VBox detailsBox = makeTicketDetailsBox(ticket.getTitle(), "By " + createdBy + " • #TIX-" + ticketNum, MEMBER_DETAILS_WIDTH);

    Label priorityBadge = UIStyler.makePriorityBadge(ticket.getPriority());
    HBox priorityBox = makeFixedWidthBox(MEMBER_PRIORITY_WIDTH, priorityBadge);

    Label deadlineLabel = makeDeadlineLabel(ticket);
    deadlineLabel.setPrefWidth(MEMBER_DEADLINE_WIDTH);
    deadlineLabel.setMinWidth(MEMBER_DEADLINE_WIDTH);
    deadlineLabel.setMaxWidth(MEMBER_DEADLINE_WIDTH);

    Label statusBadge = UIStyler.makeStatusBadge(ticket.getStatus());
    // VBox so an "Overdue" badge can be stacked beneath the status badge
    VBox statusVBox = new VBox(statusBadge);
    statusVBox.setAlignment(Pos.CENTER_LEFT);
    statusVBox.setPrefWidth(MEMBER_STATUS_WIDTH);
    statusVBox.setMinWidth(MEMBER_STATUS_WIDTH);
    statusVBox.setMaxWidth(MEMBER_STATUS_WIDTH);
    statusVBox.setSpacing(5);
    if (isOverdueInProgress) {
      Label overdueSubBadge = UIStyler.makeOverdueBadge();
      statusVBox.getChildren().add(overdueSubBadge);
    } else if (isReturned) {
      Label returnedSubBadge = UIStyler.makeReturnedBadge();
      statusVBox.getChildren().add(returnedSubBadge);
    }
    
    HBox statusBox = makeFixedWidthBox(MEMBER_STATUS_WIDTH, statusVBox);

    // -- Dynamic action button --
    ButtonAction action = getDynamicActionButtonInfo(
        ticket, isAvailableUnderDept, isAssignedToCurrentUser,
        isInProgress, isCompleted, isResolved, isOverdue, isReturned, isOverdueInProgress, isVolunteer
    );
    item.actionButton = makeActionButton(action);
    HBox actionBox = makeFixedWidthBox(MEMBER_ACTION_WIDTH, item.actionButton);

    row.getChildren().addAll(detailsBox, priorityBox, deadlineLabel, statusBox, actionBox);

    String normalStyle = row.getStyle();
    row.setOnMouseEntered(e -> row.setStyle(normalStyle.replace("-fx-background-color: white;", "-fx-background-color: #f8faff;")));
    row.setOnMouseExited(e  -> row.setStyle(normalStyle));

    item.getChildren().add(row);
    return item;
  }
  
  /**
   * Creates a themed action button from a {@link ButtonAction} descriptor.
   * A {@link FontIcon} is placed on the left side of the button text.
   */
  private static Button makeActionButton(ButtonAction action) {
    // Width adapts to label length; minimum 90px to fit "View Details" comfortably
    double btnWidth = Math.max(action.text.length() * 8.5 + 30, 90.0);

    Button button = new Button(action.text);
    button.setPrefWidth(btnWidth);
    button.setMinWidth(btnWidth);
    button.setMaxWidth(btnWidth);
    button.setPrefHeight(28);
    button.setCursor(Cursor.HAND);
    button.setStyle(
        "-fx-background-color: " + action.bgColor + ";" +
        "-fx-background-radius: 7;" +
        "-fx-text-fill: " + action.textColor + ";" +
        "-fx-font-size: 11px;" +
        "-fx-font-family: 'Inter 18pt ExtraBold'"
    );

    // Icon on the left
    FontIcon icon = new FontIcon(action.iconLiteral);
    icon.setIconSize(12);
    icon.setIconColor(javafx.scene.paint.Color.web(action.textColor));
    button.setGraphic(icon);
    button.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
    button.setGraphicTextGap(5);

    return button;
  }

  public static ListRowItem forMemberVolunteerTicket(TicketView ticket) {
    ListRowItem item = new ListRowItem();
    item.sourceObject = ticket;

    String title       = ticket.getTitle()       != null ? ticket.getTitle()       : "Untitled Ticket";
    String description = ticket.getDescription() != null ? ticket.getDescription() : "No ticket description available.";

        Label priorityBadge = UIStyler.makePriorityBadge(ticket.getPriority());

        Label volunteerTag = new Label("Volunteer");
        volunteerTag.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(priorityBadge, spacer, volunteerTag);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(SMALL_CARD_WIDTH - 24);
        titleLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(SMALL_CARD_WIDTH - 24);
        descriptionLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 10px;");

        Label deadlineLabel = makeDeadlineLabel(ticket);

        // Volunteer board tickets are OPEN, unassigned, and are not tied to any department → "Volunteer" action
        ButtonAction volunteerAction = ButtonAction.VOLUNTEER;
        FontIcon volunteerIcon = new FontIcon(volunteerAction.iconLiteral);
        volunteerIcon.setIconSize(12);
        volunteerIcon.setIconColor(javafx.scene.paint.Color.web(volunteerAction.textColor));

        Button volunteerButton = new Button(volunteerAction.text);
        volunteerButton.setPrefWidth(SMALL_CARD_WIDTH - 24);
        volunteerButton.setMaxWidth(SMALL_CARD_WIDTH - 24);
        volunteerButton.setPrefHeight(28);
        volunteerButton.setCursor(Cursor.HAND);
        volunteerButton.setStyle(
                "-fx-background-color: " + volunteerAction.bgColor + ";" +
                        "-fx-background-radius: 7;" +
                        "-fx-text-fill: " + volunteerAction.textColor + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );
        volunteerButton.setGraphic(volunteerIcon);
        volunteerButton.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        volunteerButton.setGraphicTextGap(5);

        item.actionButton = volunteerButton;

        VBox card = new VBox(8, topRow, titleLabel, descriptionLabel, deadlineLabel, volunteerButton);
        card.setPrefWidth(SMALL_CARD_WIDTH);
        card.setMaxWidth(SMALL_CARD_WIDTH);
        card.setMinHeight(118);
        card.setPadding(new Insets(12));
        card.setCursor(Cursor.HAND);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e7ecf8; -fx-border-radius: 12; -fx-background-radius: 12;");

        String normalStyle = card.getStyle();
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8faff; -fx-border-color: #d7e4fb; -fx-border-radius: 12; -fx-background-radius: 12;"));
        card.setOnMouseExited(e  -> card.setStyle(normalStyle));

        item.getChildren().add(card);
        return item;
    }


    public static ListRowItem forExecutiveAssignment(TicketView ticket, List<? extends User> users) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = ticket;

        HBox row = new HBox();
        row.setPrefWidth(TABLE_ROW_WIDTH);
        row.setMinWidth(TABLE_ROW_WIDTH);
        row.setMaxWidth(TABLE_ROW_WIDTH);
        row.setMinHeight(66);
        row.setPrefHeight(66);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        row.setStyle("-fx-background-color: white; -fx-border-color: #eef2fb; -fx-border-width: 1 0 0 0;");

        String createdBy  = ticket.getCreatedBy()      != null ? ticket.getCreatedBy()      : "Unknown";
        String ticketNum  = String.format("%03d", ticket.getId());
        String department = ticket.getDepartmentName() != null ? ticket.getDepartmentName() : "Volunteer";

        VBox detailsBox = makeTicketDetailsBox(ticket.getTitle(), "By " + createdBy + " • #TIX-" + ticketNum, EXEC_DETAILS_WIDTH);

        Label deptLabel = new Label(department);
        deptLabel.setPrefWidth(EXEC_DEPT_WIDTH);
        deptLabel.setMinWidth(EXEC_DEPT_WIDTH);
        deptLabel.setMaxWidth(EXEC_DEPT_WIDTH);
        deptLabel.setWrapText(true);
        deptLabel.setTooltip(new Tooltip(department));
        deptLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label priorityBadge = UIStyler.makePriorityBadge(ticket.getPriority());
        HBox priorityBox = makeFixedWidthBox(EXEC_PRIORITY_WIDTH, priorityBadge);

        Label deadlineLabel = makeDeadlineLabel(ticket);
        deadlineLabel.setPrefWidth(EXEC_DEADLINE_WIDTH);
        deadlineLabel.setMinWidth(EXEC_DEADLINE_WIDTH);
        deadlineLabel.setMaxWidth(EXEC_DEADLINE_WIDTH);

        ComboBox<User> combo = makeAssignComboBox(users, ticket.getAssignedToName());
        item.assignComboBox = combo;
        HBox assignBox = makeFixedWidthBox(EXEC_ASSIGN_WIDTH, combo);

        Button saveButton = makeButton("Save", 52, "#2f95ff", "white");
        item.actionButton = saveButton;
        HBox actionBox = makeFixedWidthBox(EXEC_ACTION_WIDTH, saveButton);

        row.getChildren().addAll(detailsBox, deptLabel, priorityBox, deadlineLabel, assignBox, actionBox);

        String normalStyle = row.getStyle();
        row.setOnMouseEntered(e -> row.setStyle(normalStyle.replace("-fx-background-color: white;", "-fx-background-color: #f8faff;")));
        row.setOnMouseExited(e  -> row.setStyle(normalStyle));

        item.getChildren().add(row);
        return item;
    }


    public static ListRowItem forEditorReview(TicketView ticket) {
        return forEditorReview(ticket, null);
    }

    public static ListRowItem forEditorReview(TicketView ticket, List<? extends User> users) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = ticket;

        HBox row = new HBox();
        row.setPrefWidth(TABLE_ROW_WIDTH);
        row.setMinWidth(TABLE_ROW_WIDTH);
        row.setMaxWidth(TABLE_ROW_WIDTH);
        row.setMinHeight(66);
        row.setPrefHeight(66);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        row.setStyle("-fx-background-color: white; -fx-border-color: #eef2fb; -fx-border-width: 1 0 0 0;");

        String ticketNum  = String.format("%03d", ticket.getId());
        String department = ticket.getDepartmentName() != null ? ticket.getDepartmentName() : "Volunteer";

        VBox detailsBox = makeTicketDetailsBox(ticket.getTitle(), "#TIX-" + ticketNum + " • " + department, EDITOR_DETAILS_WIDTH);

        ComboBox<User> combo = makeAssignComboBox(users, ticket.getAssignedToName());
        item.assignComboBox = combo;
        HBox assignBox = new HBox(combo);
        assignBox.setAlignment(Pos.CENTER_LEFT);
        assignBox.setPrefWidth(EDITOR_ASSIGN_WIDTH);
        assignBox.setMinWidth(EDITOR_ASSIGN_WIDTH);
        assignBox.setMaxWidth(EDITOR_ASSIGN_WIDTH);

        Label statusBadge = UIStyler.makeStatusBadge(ticket.getStatus());
        HBox statusBox = makeFixedWidthBox(EDITOR_STATUS_WIDTH, statusBadge);

        Label priorityBadge = UIStyler.makePriorityBadge(ticket.getPriority());
        HBox priorityBox = makeFixedWidthBox(EDITOR_PRIORITY_WIDTH, priorityBadge);

        Label deadlineLabel = makeDeadlineLabel(ticket);
        deadlineLabel.setPrefWidth(EDITOR_DEADLINE_WIDTH);
        deadlineLabel.setMinWidth(EDITOR_DEADLINE_WIDTH);
        deadlineLabel.setMaxWidth(EDITOR_DEADLINE_WIDTH);

        Button saveButton     = makeButton("Save", 38, "#eef3ff", "#1c2b63");
        Button approveButton  = makeButton("✓",   28, "#4bcc8a", "white");
        Button sendBackButton = makeButton("↶",   28, "#ffe0e5", "#f14d5a");
        item.secondaryActionButton = saveButton;
        item.actionButton          = approveButton;
        item.thirdActionButton     = sendBackButton;

        HBox actionsBox = new HBox(5, saveButton, approveButton, sendBackButton);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setPrefWidth(EDITOR_ACTIONS_WIDTH);
        actionsBox.setMinWidth(EDITOR_ACTIONS_WIDTH);
        actionsBox.setMaxWidth(EDITOR_ACTIONS_WIDTH);

        row.getChildren().addAll(detailsBox, assignBox, statusBox, priorityBox, deadlineLabel, actionsBox);

        String normalStyle = row.getStyle();
        row.setOnMouseEntered(e -> row.setStyle(normalStyle.replace("-fx-background-color: white;", "-fx-background-color: #f8faff;")));
        row.setOnMouseExited(e  -> row.setStyle(normalStyle));

        item.getChildren().add(row);
        return item;
    }


    public static ListRowItem forActivity(Notification notification) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = notification;

        String initials   = getNotificationInitials(notification);
        String circleBg   = getNotificationCircleColor(notification);
        String circleText = getNotificationTextColor(notification);

        StackPane avatarIcon = makeAvatar(initials, circleBg, circleText);

        String messageText = notification.getMessage() != null ? notification.getMessage() : "No notification message.";
        Label messageLabel = new Label(messageText);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(SMALL_CARD_TEXT_WIDTH);
        messageLabel.setStyle("-fx-text-fill: #1c2b63; " +
                              "-fx-font-size: 11px; " +
                              "-fx-font-family: 'Inter 18pt Medium';");

        String timeText = notification.getCreatedAt() != null ? notification.getCreatedAt().format(DATE_FORMATTER) : "No date";
        Label timeLabel = new Label(timeText);
        timeLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 9px;");

        VBox textBox = new VBox(3, messageLabel, timeLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setPrefWidth(SMALL_CARD_TEXT_WIDTH);
        textBox.setMaxWidth(SMALL_CARD_TEXT_WIDTH);

        HBox row = new HBox(8, avatarIcon, textBox);
        row.setPrefWidth(SMALL_CARD_WIDTH);
        row.setMaxWidth(SMALL_CARD_WIDTH);
        row.setMinHeight(52);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        row.setStyle("-fx-background-color: transparent;");

        String normalStyle = row.getStyle();
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8faff; " +
                                                "-fx-background-radius: 10; " +
                                                "-fx-font-size: 11px; " +
                                                "-fx-font-family: 'Inter 18pt SemiBold';" +
                                                "-fx-padding: 3 4 3 4;"));
        row.setOnMouseExited(e  -> row.setStyle(normalStyle));

        item.getChildren().add(row);
        return item;
    }


    public static ListRowItem forUser(User user) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = user;

        String initials   = getUserInitials(user);
        String circleBg   = getAvatarBackground(initials);
        String circleText = getAvatarTextColor(initials);

        StackPane avatarIcon = makeAvatar(initials, circleBg, circleText);

        String fullName = user != null ? user.getFullName() : "Unknown User";
        String role     = (user != null && user.getRole() != null) ? user.getRole().toString() : "USER";

        Label nameLabel = new Label(fullName);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label roleLabel = new Label(role);
        roleLabel.setWrapText(true);
        roleLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 10px;");

        VBox textBox = new VBox(3, nameLabel, roleLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(8, avatarIcon, textBox);
        row.setPrefWidth(SMALL_CARD_WIDTH);
        row.setMaxWidth(SMALL_CARD_WIDTH);
        row.setMinHeight(58);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setCursor(Cursor.HAND);
        row.setStyle("-fx-background-color: white; -fx-border-color: #eef2fb; -fx-border-width: 0 0 1 0;");

        String normalStyle = row.getStyle();
        row.setOnMouseEntered(e -> row.setStyle(normalStyle.replace("-fx-background-color: white;", "-fx-background-color: #f8faff;")));
        row.setOnMouseExited(e  -> row.setStyle(normalStyle));

        item.getChildren().add(row);
        return item;
    }


    private static VBox makeTicketDetailsBox(String title, String subtitle, double width) {
        String safeTitle    = title    != null ? title    : "Untitled Ticket";
        String safeSubtitle = subtitle != null ? subtitle : "";

        Label titleLabel = new Label(safeTitle);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(width - 8);
        titleLabel.setStyle("-fx-text-fill: #203477; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-family: 'Inter 18pt ExtraBold'");

        Label subtitleLabel = new Label(safeSubtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(width - 8);
        subtitleLabel.setStyle("-fx-text-fill: #7482b2;" +
                               "-fx-font-size: 10px;" +
                               "-fx-font-family: 'Inter 18pt SemiBold'"
                              );

        VBox box = new VBox(2, titleLabel, subtitleLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        return box;
    }

    private static HBox makeFixedWidthBox(double width, javafx.scene.Node node) {
        HBox box = new HBox(node);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        return box;
    }

    private static Label makeDeadlineLabel(TicketView ticket) {
        boolean overdue = isOverdue(ticket);

        String text;
        if (ticket == null || ticket.getDeadline() == null) {
            text = "—";
        } else {
            text = ticket.getDeadline().format(DATE_FORMATTER);
        }

        String color = overdue ? "#f14d5a" : "#1c2b63";

        Label label = new Label(text);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setStyle("-fx-text-fill: " + color + "; " +
                       "-fx-font-size: 10px; " +
                       "-fx-font-family: 'Inter 18pt SemiBold'");

        if (overdue) {
            // Use a FontIcon instead of the Unicode ⚠U+FE0F sequence so the deadline
            // cell renders identically on every OS / JDK / font combination.
            FontIcon warningIcon = new FontIcon("fas-exclamation-triangle");
            warningIcon.setIconSize(10);
            warningIcon.setIconColor(javafx.scene.paint.Color.web("#f14d5a"));
            label.setGraphic(warningIcon);
            label.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
            label.setGraphicTextGap(3);
        }

        return label;
    }
    
    private static Label makeLastUpdatedLabel(TicketView ticket) {
      
      String text;
      if (ticket == null || ticket.getLastUpdated() == null) {
        text = "—";
      } else {
        text = ticket.getLastUpdated().format(DATE_FORMATTER);
      }
      
      String color = "#1c2b63";
      
      Label label = new Label(text);
      label.setWrapText(true);
      label.setAlignment(Pos.CENTER_LEFT);
      label.setStyle("-fx-text-fill: " + color + "; " +
                     "-fx-font-size: 10px; " +
                     "-fx-font-family: 'Inter 18pt SemiBold'");
      
      return label;
    }

    private static Button makeButton(String text, double width, String bgColor, String textColor) {
        Button button = new Button(text);
        button.setPrefWidth(width);
        button.setMinWidth(width);
        button.setMaxWidth(width);
        button.setPrefHeight(28);
        button.setCursor(Cursor.HAND);
        button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 7;" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );
        return button;
    }

    private static ComboBox<User> makeAssignComboBox(List<? extends User> users, String assignedName) {
        ComboBox<User> combo = new ComboBox<>();
        combo.setPromptText("Assign");
        combo.setPrefWidth(140);
        combo.setPrefHeight(28);
        combo.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #dfe7f5;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 10px;" +
                        "-fx-text-fill: #1c2b63;"
        );

        if (users != null) {
            combo.setItems(FXCollections.observableArrayList(users));

            if (assignedName != null && !assignedName.isBlank()) {
                for (User user : users) {
                    if (user != null && user.getFullName().equalsIgnoreCase(assignedName)) {
                        combo.setValue(user);
                        break;
                    }
                }
            }
        }

        return combo;
    }

    private static StackPane makeAvatar(String initials, String circleBg, String circleText) {
        Circle circle = new Circle(14);
        circle.setStyle("-fx-fill: " + circleBg + ";");

        Label initialsLabel = new Label(initials != null ? initials : "NA");
        initialsLabel.setStyle("-fx-text-fill: " + circleText + "; -fx-font-size: 9px; -fx-font-weight: bold;");

        StackPane avatar = new StackPane(circle, initialsLabel);
        avatar.setPrefWidth(32);
        avatar.setMinWidth(32);
        avatar.setMaxWidth(32);
        avatar.setPrefHeight(32);
        return avatar;
    }

    private static boolean isOverdue(TicketView ticket) {
        if (ticket == null || ticket.getDeadline() == null) return false;

        String status = ticket.getStatus() != null ? ticket.getStatus() : "";
        boolean alreadyDone = status.equalsIgnoreCase("COMPLETED")
                || status.equalsIgnoreCase("RESOLVED");

        return !alreadyDone && ticket.getDeadline().isBefore(LocalDateTime.now());
    }

    private static String getNotificationInitials(Notification notification) {
        if (notification == null || notification.getMessage() == null) return "NA";
        String msg = notification.getMessage().toUpperCase();
        if (msg.contains("RESOLVED"))    return "RS";
        if (msg.contains("IN PROGRESS")) return "IP";
        if (msg.contains("COMPLETED"))   return "CP";
        if (msg.contains("WAITING"))     return "WT";
        return "TK";
    }

    private static String getNotificationCircleColor(Notification notification) {
        if (notification == null || notification.getMessage() == null) return "#dceeff";
        String msg = notification.getMessage().toUpperCase();
        if (msg.contains("RESOLVED") || msg.contains("COMPLETED")) return "#d9ffed";
        if (msg.contains("IN PROGRESS"))                           return "#ffedcc";
        if (msg.contains("OVERDUE"))                               return "#ffe0e5";
        return "#dceeff";
    }

    private static String getNotificationTextColor(Notification notification) {
        if (notification == null || notification.getMessage() == null) return "#2f95ff";
        String msg = notification.getMessage().toUpperCase();
        if (msg.contains("RESOLVED") || msg.contains("COMPLETED")) return "#4bcc8a";
        if (msg.contains("IN PROGRESS"))                           return "#ff9900";
        if (msg.contains("OVERDUE"))                               return "#f14d5a";
        return "#2f95ff";
    }

    private static String getUserInitials(User user) {
        if (user == null) return "NA";
        String first = user.getFirstName();
        String last  = user.getLastName();
        String a = (first != null && !first.trim().isEmpty()) ? first.trim().substring(0, 1).toUpperCase() : "";
        String b = (last  != null && !last.trim().isEmpty())  ? last.trim().substring(0, 1).toUpperCase()  : "";
        String initials = a + b;
        return initials.isBlank() ? "NA" : initials;
    }

    private static String getAvatarBackground(String initials) {
        if (initials == null) return "#dceeff";
        switch (initials.toUpperCase()) {
            case "SJ": case "ER": case "JT": return "#d9ffed";
            case "MC": case "MB":            return "#ffedcc";
            default:                         return "#dceeff";
        }
    }

    private static String getAvatarTextColor(String initials) {
        if (initials == null) return "#2f95ff";
        switch (initials.toUpperCase()) {
            case "SJ": case "ER": case "JT": return "#4bcc8a";
            case "MC": case "MB":            return "#ff9900";
            default:                         return "#2f95ff";
        }
    }


    public TicketView   getTicketView()   { return sourceObject instanceof TicketView   ? (TicketView)   sourceObject : null; }
    public Notification getNotification() { return sourceObject instanceof Notification ? (Notification) sourceObject : null; }
    public User         getUser()         { return sourceObject instanceof User         ? (User)         sourceObject : null; }
    public Object       getSourceObject() { return sourceObject; }

    public Button         getActionButton()          { return actionButton; }
    public Button         getSecondaryActionButton() { return secondaryActionButton; }
    public Button         getThirdActionButton()     { return thirdActionButton; }
    public ComboBox<User> getAssignComboBox()        { return assignComboBox; }

    public User getSelectedAssignedUser() {
        return assignComboBox != null ? assignComboBox.getValue() : null;
    }

    public void setAction(EventHandler<ActionEvent> handler) {
        if (actionButton != null) actionButton.setOnAction(handler);
    }
    
    public void setSecondaryAction(EventHandler<ActionEvent> eventHandler) {
      if (secondaryActionButton != null) {
        secondaryActionButton.setOnAction(eventHandler);
      }
    }

    public void setThirdAction(EventHandler<ActionEvent> handler) {
        if (thirdActionButton != null) thirdActionButton.setOnAction(handler);
    }

    public void setRowClick(EventHandler<MouseEvent> handler) {
        setOnMouseClicked(handler);
    }
}
