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
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class ListRowItem extends VBox {
  
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");

  private static final String STATUS_OPEN = "#3B82F6";
  private static final String STATUS_OPEN_BG = "#DBEAFE";
  private static final String STATUS_IN_PROGRESS = "#F59E0B";
  private static final String STATUS_IN_PROGRESS_BG = "#FEF3C7";
  private static final String STATUS_COMPLETED = "#22C55E";
  private static final String STATUS_COMPLETED_BG = "#DCFCE7";
  private static final String STATUS_RESOLVED = "#8B5CF6";
  private static final String STATUS_RESOLVED_BG = "#EDE9FE";

  private static final double TABLE_ROW_WIDTH = 850.0;

  private static final double MEMBER_DETAILS_WIDTH = 300.0;
  private static final double MEMBER_PRIORITY_WIDTH = 110.0;
  private static final double MEMBER_DEADLINE_WIDTH = 170.0;
  private static final double MEMBER_STATUS_WIDTH = 140.0;
  private static final double MEMBER_ACTION_WIDTH = 110.0;

  private static final double EXEC_DETAILS_WIDTH = 230.0;
  private static final double EXEC_DEPT_WIDTH = 122.0;
  private static final double EXEC_PRIORITY_WIDTH = 90.0;
  private static final double EXEC_DEADLINE_WIDTH = 130.0;
  private static final double EXEC_ASSIGN_WIDTH = 160.0;
  private static final double EXEC_ACTION_WIDTH = 65.0;

  private static final double EDITOR_DETAILS_WIDTH = 370.0;
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
  
  public static ListRowItem forMemberAvailableTicket(TicketView ticket) {
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

    String createdBy = ticket.getCreatedBy() != null ? ticket.getCreatedBy() : "Unknown";
    String ticketNum = String.format("%03d", ticket.getId());

    VBox detailsBox = makeTicketDetailsBox(ticket.getTitle(), "By " + createdBy + " • #TIX-" + ticketNum, MEMBER_DETAILS_WIDTH);

    Label priorityBadge = makePriorityBadge(ticket.getPriority());
    HBox priorityBox = makeFixedWidthBox(MEMBER_PRIORITY_WIDTH, priorityBadge);

    Label deadlineLabel = makeDeadlineLabel(ticket);
    deadlineLabel.setPrefWidth(MEMBER_DEADLINE_WIDTH);
    deadlineLabel.setMinWidth(MEMBER_DEADLINE_WIDTH);
    deadlineLabel.setMaxWidth(MEMBER_DEADLINE_WIDTH);

    Label statusBadge = makeStatusBadge(ticket.getStatus());
    HBox statusBox = makeFixedWidthBox(MEMBER_STATUS_WIDTH, statusBadge);

    Button startButton = makeButton("Start", 82, "#2f95ff", "white");
    item.actionButton = startButton;
    HBox actionBox = makeFixedWidthBox(MEMBER_ACTION_WIDTH, startButton);

    row.getChildren().addAll(detailsBox, priorityBox, deadlineLabel, statusBox, actionBox);

    String normalStyle = row.getStyle();
    row.setOnMouseEntered(e -> row.setStyle(normalStyle.replace("-fx-background-color: white;", "-fx-background-color: #f8faff;")));
    row.setOnMouseExited(e  -> row.setStyle(normalStyle));

    item.getChildren().add(row);
    return item;
  }


  public static ListRowItem forMemberVolunteerTicket(TicketView ticket) {
    ListRowItem item = new ListRowItem();
    item.sourceObject = ticket;

    String title       = ticket.getTitle()       != null ? ticket.getTitle()       : "Untitled Ticket";
    String description = ticket.getDescription() != null ? ticket.getDescription() : "No ticket description available.";

    Label priorityBadge = makePriorityBadge(ticket.getPriority());

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

        Button volunteerButton = makeButton("Volunteer", SMALL_CARD_WIDTH - 24, "#4bcc8a", "white");
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

        Label priorityBadge = makePriorityBadge(ticket.getPriority());
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

        Label statusBadge = makeStatusBadge(ticket.getStatus());
        HBox statusBox = makeFixedWidthBox(EDITOR_STATUS_WIDTH, statusBadge);

        Label priorityBadge = makePriorityBadge(ticket.getPriority());
        HBox priorityBox = makeFixedWidthBox(EDITOR_PRIORITY_WIDTH, priorityBadge);

        Label deadlineLabel = makeDeadlineLabel(ticket);
        deadlineLabel.setPrefWidth(EDITOR_DEADLINE_WIDTH);
        deadlineLabel.setMinWidth(EDITOR_DEADLINE_WIDTH);
        deadlineLabel.setMaxWidth(EDITOR_DEADLINE_WIDTH);

        Button approveButton  = makeButton("✓",   28, "#4bcc8a", "white");
        Button sendBackButton = makeButton("↶",   28, "#ffe0e5", "#f14d5a");
        item.actionButton          = approveButton;
        item.thirdActionButton     = sendBackButton;

        HBox actionsBox = new HBox(5, approveButton, sendBackButton);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setPrefWidth(EDITOR_ACTIONS_WIDTH);
        actionsBox.setMinWidth(EDITOR_ACTIONS_WIDTH);
        actionsBox.setMaxWidth(EDITOR_ACTIONS_WIDTH);

        row.getChildren().addAll(detailsBox, statusBox, priorityBox, deadlineLabel, actionsBox);

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
        messageLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 11px; -fx-font-weight: bold;");

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
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 10; -fx-padding: 4 6 4 6;"));
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
        titleLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(safeSubtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(width - 8);
        subtitleLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 10px;");

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
            text = "No deadline";
        } else if (overdue) {
            text = ticket.getDeadline().format(DATE_FORMATTER) + " !";
        } else {
            text = ticket.getDeadline().format(DATE_FORMATTER);
        }

        String color = overdue ? "#f14d5a" : "#1c2b63";

        Label label = new Label(text);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        return label;
    }

    private static Label makePriorityBadge(String priority) {
        String text = priority != null ? priority.toUpperCase() : "MEDIUM";

        String bgColor;
        String textColor;
        switch (text) {
            case "URGENT": bgColor = "#ffe0e5"; textColor = "#f14d5a"; break;
            case "HIGH":   bgColor = "#ffe7b5"; textColor = "#ff9900"; break;
            case "LOW":    bgColor = "#d9ffed"; textColor = "#4bcc8a"; break;
            default:       bgColor = "#dceeff"; textColor = "#2f95ff"; break;
        }

        return makeBadge(text, bgColor, textColor);
    }

    private static Label makeStatusBadge(String status) {
        String text = status != null ? status.toUpperCase().replace("_", " ") : "OPEN";

        String bgColor;
        String textColor;
        switch (text) {
            case "IN PROGRESS": bgColor = STATUS_IN_PROGRESS_BG; textColor = STATUS_IN_PROGRESS; break;
            case "RESOLVED":
            case "APPROVED":    bgColor = STATUS_RESOLVED_BG; textColor = STATUS_RESOLVED; break;
            case "OVERDUE":
            case "SENT BACK":   bgColor = "#ffe0e5"; textColor = "#f14d5a"; break;
            case "COMPLETED":   bgColor = STATUS_COMPLETED_BG; textColor = STATUS_COMPLETED; break;
            default:            bgColor = STATUS_OPEN_BG; textColor = STATUS_OPEN; break;
        }

        return makeBadge(text, bgColor, textColor);
    }

    private static Label makeBadge(String text, String bgColor, String textColor) {
        String display = text.length() > 8 ? text.substring(0, 8) : text;
        if (display.equals("IN PROGR")) display = "IN PROG";

        Label badge = new Label(display);
        badge.setAlignment(Pos.CENTER);
        badge.setMinWidth(44);
        badge.setPrefHeight(22);
        badge.setPadding(new Insets(0, 7, 0, 7));
        badge.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 9px;" +
                        "-fx-font-weight: bold;"
        );
        return badge;
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
                || status.equalsIgnoreCase("RESOLVED")
                || status.equalsIgnoreCase("APPROVED");

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
        if (notification == null || notification.getMessage() == null) return STATUS_OPEN_BG;
        String msg = notification.getMessage().toUpperCase();
        if (msg.contains("RESOLVED"))                              return STATUS_RESOLVED_BG;
        if (msg.contains("COMPLETED"))                             return STATUS_COMPLETED_BG;
        if (msg.contains("IN PROGRESS"))                           return STATUS_IN_PROGRESS_BG;
        if (msg.contains("OVERDUE"))                               return "#ffe0e5";
        return STATUS_OPEN_BG;
    }

    private static String getNotificationTextColor(Notification notification) {
        if (notification == null || notification.getMessage() == null) return STATUS_OPEN;
        String msg = notification.getMessage().toUpperCase();
        if (msg.contains("RESOLVED"))                              return STATUS_RESOLVED;
        if (msg.contains("COMPLETED"))                             return STATUS_COMPLETED;
        if (msg.contains("IN PROGRESS"))                           return STATUS_IN_PROGRESS;
        if (msg.contains("OVERDUE"))                               return "#f14d5a";
        return STATUS_OPEN;
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
