package com.csit228.capstone.utils;

import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Notification;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListRowItem extends VBox {

    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
    private static final DateTimeFormatter ACTIVITY_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");

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

    public static ListRowItem forMemberAvailableTicket(TicketView ticketView) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = ticketView;

        HBox row = item.createBaseTableRow(TABLE_ROW_WIDTH, 66, "white");

        VBox taskDetailsBox = item.createTicketDetailsBox(
                ticketView,
                MEMBER_DETAILS_WIDTH,
                "By " + safeText(ticketView.getCreatedBy(), "Unknown") + " • #TIX-" + formatTicketId(ticketView.getId())
        );

        HBox priorityBox = createFixedBox(
                MEMBER_PRIORITY_WIDTH,
                createBadge(safeText(ticketView.getPriority(), "MEDIUM"), getPriorityStyle(ticketView.getPriority()))
        );

        Label deadlineLabel = createDeadlineLabel(ticketView);
        deadlineLabel.setPrefWidth(MEMBER_DEADLINE_WIDTH);
        deadlineLabel.setMinWidth(MEMBER_DEADLINE_WIDTH);
        deadlineLabel.setMaxWidth(MEMBER_DEADLINE_WIDTH);

        HBox statusBox = createFixedBox(
                MEMBER_STATUS_WIDTH,
                createBadge(displayEnum(safeText(ticketView.getStatus(), "OPEN")), getStatusStyle(ticketView.getStatus()))
        );

        item.actionButton = createActionButton("Start", 82, "#2f95ff", "white");
        HBox actionBox = createFixedBox(MEMBER_ACTION_WIDTH, item.actionButton);

        row.getChildren().addAll(taskDetailsBox, priorityBox, deadlineLabel, statusBox, actionBox);
        item.addHoverEffect(row);
        item.getChildren().add(row);

        return item;
    }

    public static ListRowItem forMemberVolunteerTicket(TicketView ticketView) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = ticketView;

        VBox card = new VBox();
        card.setPrefWidth(SMALL_CARD_WIDTH);
        card.setMaxWidth(SMALL_CARD_WIDTH);
        card.setMinHeight(118);
        card.setSpacing(8);
        card.setPadding(new Insets(12, 12, 12, 12));
        card.setCursor(Cursor.HAND);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e7ecf8;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label priorityBadge = createBadge(safeText(ticketView.getPriority(), "MEDIUM"), getPriorityStyle(ticketView.getPriority()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label departmentLabel = new Label("Volunteer");
        departmentLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 10px;");

        topRow.getChildren().addAll(priorityBadge, spacer, departmentLabel);

        Label titleLabel = createTitleLabel(safeText(ticketView.getTitle(), "Untitled Ticket"));
        titleLabel.setMaxWidth(SMALL_CARD_WIDTH - 24);

        Label descriptionLabel = createSubtitleLabel(safeText(ticketView.getDescription(), "No ticket description available."));
        descriptionLabel.setMaxWidth(SMALL_CARD_WIDTH - 24);

        Label deadlineLabel = createDeadlineLabel(ticketView);

        item.actionButton = createActionButton("Volunteer", SMALL_CARD_WIDTH - 24, "#4bcc8a", "white");

        card.getChildren().addAll(topRow, titleLabel, descriptionLabel, deadlineLabel, item.actionButton);
        item.addCardHoverEffect(card);
        item.getChildren().add(card);

        return item;
    }

    public static ListRowItem forExecutiveAssignment(TicketView ticketView, List<? extends User> assignableUsers) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = ticketView;

        HBox row = item.createBaseTableRow(TABLE_ROW_WIDTH, 66, "white");

        VBox ticketDetailsBox = item.createTicketDetailsBox(
                ticketView,
                EXEC_DETAILS_WIDTH,
                "By " + safeText(ticketView.getCreatedBy(), "Unknown") + " • #TIX-" + formatTicketId(ticketView.getId())
        );

        String departmentText = safeText(ticketView.getDepartmentName(), "Volunteer");
        Label departmentLabel = new Label(departmentText);
        departmentLabel.setPrefWidth(EXEC_DEPT_WIDTH);
        departmentLabel.setMinWidth(EXEC_DEPT_WIDTH);
        departmentLabel.setMaxWidth(EXEC_DEPT_WIDTH);
        departmentLabel.setWrapText(true);
        departmentLabel.setTooltip(new Tooltip(departmentText));
        departmentLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox priorityBox = createFixedBox(
                EXEC_PRIORITY_WIDTH,
                createBadge(safeText(ticketView.getPriority(), "MEDIUM"), getPriorityStyle(ticketView.getPriority()))
        );

        Label deadlineLabel = createDeadlineLabel(ticketView);
        deadlineLabel.setPrefWidth(EXEC_DEADLINE_WIDTH);
        deadlineLabel.setMinWidth(EXEC_DEADLINE_WIDTH);
        deadlineLabel.setMaxWidth(EXEC_DEADLINE_WIDTH);

        item.assignComboBox = new ComboBox<>();
        item.assignComboBox.setItems(FXCollections.observableArrayList(assignableUsers));
        item.assignComboBox.setPromptText("Assign");
        item.assignComboBox.setPrefWidth(140);
        item.assignComboBox.setPrefHeight(28);
        item.assignComboBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #dfe7f5;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 10px;" +
                        "-fx-text-fill: #1c2b63;"
        );


        // CHANGE THIS TO EDITOR ASSIGNED TO THAT DEPARTMENT
        String assignedName = safeText(ticketView.getAssignedToName(), "");
        if (assignableUsers != null && !assignedName.isBlank()) {
            for (User user : assignableUsers) {
                if (user != null && user.getFullName().equalsIgnoreCase(assignedName)) {
                    item.assignComboBox.setValue(user);
                    break;
                }
            }
        }

        HBox assignBox = createFixedBox(EXEC_ASSIGN_WIDTH, item.assignComboBox);
        item.actionButton = createActionButton("Save", 52, "#2f95ff", "white");
        HBox actionBox = createFixedBox(EXEC_ACTION_WIDTH, item.actionButton);

        row.getChildren().addAll(ticketDetailsBox, departmentLabel, priorityBox, deadlineLabel, assignBox, actionBox);
        item.addHoverEffect(row);
        item.getChildren().add(row);

        return item;
    }

    public static ListRowItem forEditorReview(TicketView ticketView) {
        return forEditorReview(ticketView, null);
    }

    public static ListRowItem forEditorReview(TicketView ticketView, List<? extends User> assignableUsers) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = ticketView;

        HBox row = item.createBaseTableRow(TABLE_ROW_WIDTH, 66, "white");

        VBox ticketDetailsBox = item.createTicketDetailsBox(
                ticketView,
                EDITOR_DETAILS_WIDTH,
                "#TIX-" + formatTicketId(ticketView.getId()) + " • " + safeText(ticketView.getDepartmentName(), "Volunteer")
        );

        HBox assignToBox = new HBox();
        assignToBox.setPrefWidth(EDITOR_ASSIGN_WIDTH);
        assignToBox.setMinWidth(EDITOR_ASSIGN_WIDTH);
        assignToBox.setMaxWidth(EDITOR_ASSIGN_WIDTH);
        assignToBox.setAlignment(Pos.CENTER_LEFT);

        String assignedName = safeText(ticketView.getAssignedToName(), "");

        item.assignComboBox = new ComboBox<>();
        item.assignComboBox.setPromptText("Assign");
        item.assignComboBox.setPrefWidth(120);
        item.assignComboBox.setPrefHeight(28);
        item.assignComboBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #dfe7f5;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 10px;" +
                        "-fx-text-fill: #1c2b63;"
        );

        if (assignableUsers != null) {
            item.assignComboBox.setItems(FXCollections.observableArrayList(assignableUsers));
            if (!assignedName.isBlank()) {
                for (User user : assignableUsers) {
                    if (user != null && user.getFullName().equalsIgnoreCase(assignedName)) {
                        item.assignComboBox.setValue(user);
                        break;
                    }
                }
            }
        }

        assignToBox.getChildren().add(item.assignComboBox);

        HBox statusBox = createFixedBox(
                EDITOR_STATUS_WIDTH,
                createBadge(displayEnum(safeText(ticketView.getStatus(), "OPEN")), getStatusStyle(ticketView.getStatus()))
        );

        HBox priorityBox = createFixedBox(
                EDITOR_PRIORITY_WIDTH,
                createBadge(safeText(ticketView.getPriority(), "MEDIUM"), getPriorityStyle(ticketView.getPriority()))
        );

        Label deadlineLabel = createDeadlineLabel(ticketView);
        deadlineLabel.setPrefWidth(EDITOR_DEADLINE_WIDTH);
        deadlineLabel.setMinWidth(EDITOR_DEADLINE_WIDTH);
        deadlineLabel.setMaxWidth(EDITOR_DEADLINE_WIDTH);

        HBox actionsBox = new HBox();
        actionsBox.setPrefWidth(EDITOR_ACTIONS_WIDTH);
        actionsBox.setMinWidth(EDITOR_ACTIONS_WIDTH);
        actionsBox.setMaxWidth(EDITOR_ACTIONS_WIDTH);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setSpacing(5);

        item.secondaryActionButton = createActionButton("Save", 38, "#eef3ff", "#1c2b63");
        item.actionButton = createActionButton("✓", 28, "#4bcc8a", "white");
        item.thirdActionButton = createIconButton("↶", 28, "#ffe0e5", "#f14d5a");

        actionsBox.getChildren().addAll(item.secondaryActionButton, item.actionButton, item.thirdActionButton);

        row.getChildren().addAll(ticketDetailsBox, assignToBox, statusBox, priorityBox, deadlineLabel, actionsBox);
        item.addHoverEffect(row);
        item.getChildren().add(row);

        return item;
    }

    public static ListRowItem forActivity(Notification notification) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = notification;

        HBox row = new HBox();
        row.setPrefWidth(SMALL_CARD_WIDTH);
        row.setMaxWidth(SMALL_CARD_WIDTH);
        row.setMinHeight(52);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(8);
        row.setCursor(Cursor.HAND);
        row.setStyle("-fx-background-color: transparent;");

        String initials = getNotificationInitials(notification);

        StackPane avatar = createAvatar(initials, getNotificationCircleColor(notification), getNotificationTextColor(notification));

        VBox textBox = new VBox();
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setSpacing(3);
        textBox.setPrefWidth(SMALL_CARD_TEXT_WIDTH);
        textBox.setMaxWidth(SMALL_CARD_TEXT_WIDTH);

        Label messageLabel = new Label(safeText(notification.getMessage(), "No notification message."));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(SMALL_CARD_TEXT_WIDTH);
        messageLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label timeLabel = new Label(formatNotificationTime(notification));
        timeLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 9px;");

        textBox.getChildren().addAll(messageLabel, timeLabel);
        row.getChildren().addAll(avatar, textBox);

        item.addActivityHoverEffect(row);
        item.getChildren().add(row);

        return item;
    }

    public static ListRowItem forDepartment(Department department, int count) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = department;

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(850);
        row.setMinHeight(60);
        row.setSpacing(10);
        row.setPadding(new Insets(10, 15, 10, 15));
        row.setCursor(Cursor.HAND);

        row.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: transparent transparent #dfe7f5 transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Label nameLabel = new Label(safeText(department.getName(), "Unnamed Department"));
        nameLabel.setPrefWidth(300);
        nameLabel.setStyle("-fx-font-family: 'Georgia'; -fx-text-fill: #0f1012; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label descLabel = new Label(safeText(department.getDescription(), "No description provided."));
        descLabel.setPrefWidth(400);
        descLabel.setWrapText(false);
        descLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");

        Label numOfJobs = new Label("" + department.getJobs().size());
        numOfJobs.setPrefWidth(160);
        numOfJobs.setStyle("-fx-font-family: 'Georgia'; -fx-alignment: 'center'; -fx-text-fill: #888888; -fx-font-size: 15px;");

        Label numOfMembers = new Label("" + count);
        numOfMembers.setPrefWidth(160);
        numOfMembers.setStyle("-fx-font-family: 'Georgia'; -fx-alignment: 'center'; -fx-text-fill: #888888; -fx-font-size: 15px;");



        row.getChildren().addAll(nameLabel, descLabel, numOfJobs, numOfMembers);

        item.addCardHoverEffect(row);
        item.getChildren().add(row);

        return item;
    }

    public static ListRowItem forUser(User user) {
        ListRowItem item = new ListRowItem();
        item.sourceObject = user;

        HBox row = new HBox();
        row.setPrefWidth(SMALL_CARD_WIDTH);
        row.setMaxWidth(SMALL_CARD_WIDTH);
        row.setMinHeight(58);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(8);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setCursor(Cursor.HAND);
        row.setStyle("-fx-background-color: white; -fx-border-color: #eef2fb; -fx-border-width: 0 0 1 0;");

        String initials = getUserInitials(user);
        StackPane avatar = createAvatar(initials, getAvatarBackground(initials), getAvatarTextColor(initials));

        VBox textBox = new VBox();
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setSpacing(3);

        Label nameLabel = createTitleLabel(user != null ? user.getFullName() : "Unknown User");
        Label roleLabel = createSubtitleLabel(getUserRoleText(user));

        textBox.getChildren().addAll(nameLabel, roleLabel);
        row.getChildren().addAll(avatar, textBox);

        item.addHoverEffect(row);
        item.getChildren().add(row);

        return item;
    }

    private HBox createBaseTableRow(double width, double height, String rowColor) {
        HBox row = new HBox();
        row.setPrefWidth(width);
        row.setMinWidth(width);
        row.setMaxWidth(width);
        row.setMinHeight(height);
        row.setPrefHeight(height);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        row.setStyle(
                "-fx-background-color: " + safeText(rowColor, "white") + ";" +
                        "-fx-border-color: #eef2fb;" +
                        "-fx-border-width: 1 0 0 0;" +
                        "-fx-padding: 0 0 0 0;"
        );
        return row;
    }

    private VBox createTicketDetailsBox(TicketView ticketView, double width, String subtitle) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        box.setSpacing(2);

        Label titleLabel = createTitleLabel(safeText(ticketView.getTitle(), "Untitled Ticket"));
        titleLabel.setMaxWidth(width - 8);

        Label subtitleLabel = createSubtitleLabel(subtitle);
        subtitleLabel.setMaxWidth(width - 8);

        box.getChildren().addAll(titleLabel, subtitleLabel);
        return box;
    }

    private static Label createTitleLabel(String title) {
        Label titleLabel = new Label(safeText(title, "Untitled"));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 12px; -fx-font-weight: bold;");
        return titleLabel;
    }

    private static Label createSubtitleLabel(String subtitle) {
        Label subtitleLabel = new Label(safeText(subtitle, ""));
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 10px;");
        return subtitleLabel;
    }

    private static Label createDeadlineLabel(TicketView ticketView) {
        String deadline = formatDeadline(ticketView);
        Label deadlineLabel = new Label(deadline);
        deadlineLabel.setWrapText(true);
        deadlineLabel.setAlignment(Pos.CENTER_LEFT);
        deadlineLabel.setStyle("-fx-text-fill: " + getDeadlineColor(ticketView) + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        return deadlineLabel;
    }

    private static Label createBadge(String text, String style) {
        Label badge = new Label(makeBadgeText(text));
        badge.setAlignment(Pos.CENTER);
        badge.setMinWidth(44);
        badge.setPrefHeight(22);
        badge.setPadding(new Insets(0, 7, 0, 7));
        badge.setStyle(style + "-fx-background-radius: 6; -fx-font-size: 9px; -fx-font-weight: bold;");
        return badge;
    }

    private static Button createActionButton(String text, double width, String backgroundColor, String textColor) {
        Button button = new Button(text);
        button.setPrefWidth(width);
        button.setMinWidth(width);
        button.setMaxWidth(width);
        button.setPrefHeight(28);
        button.setCursor(Cursor.HAND);
        button.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-background-radius: 7;" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );
        return button;
    }

    private static Button createIconButton(String text, double width, String backgroundColor, String textColor) {
        return createActionButton(text, width, backgroundColor, textColor);
    }

    private static StackPane createAvatar(String initials, String circleColor, String textColor) {
        StackPane avatar = new StackPane();
        avatar.setPrefWidth(32);
        avatar.setMinWidth(32);
        avatar.setMaxWidth(32);
        avatar.setPrefHeight(32);

        Circle circle = new Circle(14);
        circle.setStyle("-fx-fill: " + circleColor + ";");

        Label initialsLabel = new Label(safeText(initials, "NA"));
        initialsLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 9px; -fx-font-weight: bold;");

        avatar.getChildren().addAll(circle, initialsLabel);
        return avatar;
    }

    private static HBox createFixedBox(double width, Node node) {
        HBox box = new HBox();
        box.setPrefWidth(width);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(node);
        return box;
    }

    private void addHoverEffect(Node node) {
        String normalStyle = node.getStyle();
        node.setOnMouseEntered(event -> node.setStyle(normalStyle.replace("-fx-background-color: white;", "-fx-background-color: #f8faff;")));
        node.setOnMouseExited(event -> node.setStyle(normalStyle));
    }

    private void addCardHoverEffect(Node node) {
        String normalStyle = node.getStyle();
        node.setOnMouseEntered(event -> node.setStyle("-fx-background-color: #f8faff; -fx-border-color: #d7e4fb; -fx-border-radius: 12; -fx-background-radius: 12;"));
        node.setOnMouseExited(event -> node.setStyle(normalStyle));
    }

    private void addActivityHoverEffect(Node node) {
        String normalStyle = node.getStyle();
        node.setOnMouseEntered(event -> node.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 10; -fx-padding: 4 6 4 6;"));
        node.setOnMouseExited(event -> node.setStyle(normalStyle));
    }

    private static String getPriorityStyle(String priority) {
        if (priority == null) {
            return "-fx-background-color: #dceeff; -fx-text-fill: #2f95ff;";
        }

        switch (priority.toUpperCase()) {
            case "URGENT":
                return "-fx-background-color: #ffe0e5; -fx-text-fill: #f14d5a;";
            case "HIGH":
                return "-fx-background-color: #ffe7b5; -fx-text-fill: #ff9900;";
            case "MEDIUM":
                return "-fx-background-color: #dceeff; -fx-text-fill: #2f95ff;";
            case "LOW":
                return "-fx-background-color: #d9ffed; -fx-text-fill: #4bcc8a;";
            default:
                return "-fx-background-color: #dceeff; -fx-text-fill: #2f95ff;";
        }
    }

    private static String getStatusStyle(String status) {
        if (status == null) {
            return "-fx-background-color: #dceeff; -fx-text-fill: #2f95ff;";
        }

        switch (status.toUpperCase()) {
            case "COMPLETED":
                return "-fx-background-color: #dceeff; -fx-text-fill: #00a2ff;";
            case "IN PROGRESS":
            case "IN_PROGRESS":
                return "-fx-background-color: #ffedcc; -fx-text-fill: #ff9900;";
            case "OPEN":
                return "-fx-background-color: #dceeff; -fx-text-fill: #2f95ff;";
            case "RESOLVED":
            case "APPROVED":
                return "-fx-background-color: #dcffef; -fx-text-fill: #4bcc8a;";
            case "OVERDUE":
            case "SENT_BACK":
                return "-fx-background-color: #ffe0e5; -fx-text-fill: #f14d5a;";
            default:
                return "-fx-background-color: #dceeff; -fx-text-fill: #2f95ff;";
        }
    }

    private static String getDeadlineColor(TicketView ticketView) {
        if (isOverdue(ticketView)) {
            return "#f14d5a";
        }
        return "#1c2b63";
    }

    private static String getAvatarBackground(String initials) {
        if (initials == null) {
            return "#dceeff";
        }

        switch (initials.toUpperCase()) {
            case "SJ":
            case "ER":
            case "JT":
                return "#d9ffed";
            case "MC":
            case "MB":
                return "#ffedcc";
            default:
                return "#dceeff";
        }
    }

    private static String getAvatarTextColor(String initials) {
        if (initials == null) {
            return "#2f95ff";
        }

        switch (initials.toUpperCase()) {
            case "SJ":
            case "ER":
            case "JT":
                return "#4bcc8a";
            case "MC":
            case "MB":
                return "#ff9900";
            default:
                return "#2f95ff";
        }
    }

    private static String getNotificationInitials(Notification notification) {
        if (notification == null) {
            return "NA";
        }

        String type = notification.getType();

        if (type != null && !type.trim().isEmpty()) {
            String cleanType = type.trim().replace("_", " ");
            String[] words = cleanType.split("\\s+");

            if (words.length >= 2) {
                return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
            }

            return cleanType.substring(0, Math.min(2, cleanType.length())).toUpperCase();
        }

        int userId = notification.getUserId();
        return userId > 0 ? "U" + userId : "NA";
    }

    private static String getNotificationCircleColor(Notification notification) {
        if (notification == null || notification.getType() == null) {
            return "#dceeff";
        }

        String type = notification.getType().toUpperCase();

        switch (type) {
            case "APPROVED":
            case "RESOLVED":
            case "COMPLETED":
            case "SUCCESS":
                return "#d9ffed";
            case "SENT_BACK":
            case "REVISION":
            case "WARNING":
            case "EDITED":
                return "#ffedcc";
            case "URGENT":
            case "ERROR":
            case "OVERDUE":
                return "#ffe0e5";
            default:
                return "#dceeff";
        }
    }

    private static String getNotificationTextColor(Notification notification) {
        if (notification == null || notification.getType() == null) {
            return "#2f95ff";
        }

        String type = notification.getType().toUpperCase();

        switch (type) {
            case "APPROVED":
            case "RESOLVED":
            case "COMPLETED":
            case "SUCCESS":
                return "#4bcc8a";
            case "SENT_BACK":
            case "REVISION":
            case "WARNING":
            case "EDITED":
                return "#ff9900";
            case "URGENT":
            case "ERROR":
            case "OVERDUE":
                return "#f14d5a";
            default:
                return "#2f95ff";
        }
    }

    private static String getUserInitials(User user) {
        if (user == null) {
            return "NA";
        }

        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        String firstInitial = firstName != null && !firstName.trim().isEmpty()
                ? firstName.trim().substring(0, 1).toUpperCase()
                : "";
        String lastInitial = lastName != null && !lastName.trim().isEmpty()
                ? lastName.trim().substring(0, 1).toUpperCase()
                : "";

        String initials = firstInitial + lastInitial;
        return initials.isBlank() ? "NA" : initials;
    }

    private static String getUserRoleText(User user) {
        if (user == null || user.getRole() == null) {
            return "USER";
        }
        return user.getRole().toString();
    }

    private static String formatTicketId(int ticketId) {
        if (ticketId <= 0) {
            return "000";
        }
        return String.format("%03d", ticketId);
    }

    private static String formatDeadline(TicketView ticketView) {
        if (ticketView == null || ticketView.getDeadline() == null) {
            return "No deadline";
        }

        LocalDateTime deadlineDateTime = ticketView.getDeadline();

        String formattedDeadline = deadlineDateTime.format(DEADLINE_FORMATTER);

        if (isOverdue(ticketView)) {
            return formattedDeadline + " !";
        }
        return formattedDeadline;
    }

    private static boolean isOverdue(TicketView ticketView) {
        if (ticketView == null || ticketView.getDeadline() == null) {
            return false;
        }

        String status = safeText(ticketView.getStatus(), "");

        if (status.equalsIgnoreCase("COMPLETED")
                || status.equalsIgnoreCase("RESOLVED")
                || status.equalsIgnoreCase("APPROVED")) {
            return false;
        }

        return ticketView.getDeadline().isBefore(LocalDateTime.now());
    }

    private static String formatNotificationTime(Notification notification) {
        if (notification == null || notification.getCreatedAt() == null) {
            return "No date";
        }
        return notification.getCreatedAt().format(ACTIVITY_TIME_FORMATTER);
    }

    private static String displayEnum(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("_", " ");
    }

    private static String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("N/A")) {
            return fallback;
        }
        return value.trim();
    }

    private static String shortenText(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength) + "...";
    }

    private static String makeBadgeText(String text) {
        String clean = safeText(text, "N/A").toUpperCase();

        if (clean.equals("IN_PROGRESS") || clean.equals("IN PROGRESS")) {
            return "IN PROG";
        }

        if (clean.length() > 8) {
            return clean.substring(0, 8);
        }

        return clean;
    }

    public TicketView getTicketView() {
        if (sourceObject instanceof TicketView) {
            return (TicketView) sourceObject;
        }
        return null;
    }

    public Notification getNotification() {
        if (sourceObject instanceof Notification) {
            return (Notification) sourceObject;
        }
        return null;
    }

    public User getUser() {
        if (sourceObject instanceof User) {
            return (User) sourceObject;
        }
        return null;
    }

    public Object getSourceObject() {
        return sourceObject;
    }

    public Button getActionButton() {
        return actionButton;
    }

    public Button getSecondaryActionButton() {
        return secondaryActionButton;
    }

    public Button getThirdActionButton() {
        return thirdActionButton;
    }

    public ComboBox<User> getAssignComboBox() {
        return assignComboBox;
    }

    public User getSelectedAssignedUser() {
        if (assignComboBox == null) {
            return null;
        }
        return assignComboBox.getValue();
    }

    public void setAction(EventHandler<ActionEvent> eventHandler) {
        if (actionButton != null) {
            actionButton.setOnAction(eventHandler);
        }
    }

    public void setSecondaryAction(EventHandler<ActionEvent> eventHandler) {
        if (secondaryActionButton != null) {
            secondaryActionButton.setOnAction(eventHandler);
        }
    }

    public void setThirdAction(EventHandler<ActionEvent> eventHandler) {
        if (thirdActionButton != null) {
            thirdActionButton.setOnAction(eventHandler);
        }
    }

    public void setRowClick(EventHandler<MouseEvent> eventHandler) {
        setOnMouseClicked(eventHandler);
    }
}
