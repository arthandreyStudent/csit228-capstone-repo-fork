package com.csit228.capstone.controller;

import com.csit228.capstone.dao.CommentDAO;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.Comment;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.utils.AppSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketDetailModelController {

    @FXML
    public Button buttonClose;
    @FXML
    public HBox ticketHeader;
    @FXML
    public HBox statusBadgeContainer;
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
    public Label descriptionLabel;
    @FXML
    public Label assignedToLabel;
    @FXML
    public TextArea reviewCommentArea;

    private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
    private final CommentDAO commentDAO = CommentDAO.getCommentDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private TicketView currentTicket;

    @FXML
    public void initialize() {
        if (ticketHeader == null) {
            return;
        }

        LinearGradient headerGradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1f3e8f")),
                new Stop(1, Color.web("#3a7ef3"))
        );

        ticketHeader.setBackground(new Background(new BackgroundFill(headerGradient, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void loadTicket(TicketView ticket) {
        this.currentTicket = ticket;

        if (ticket != null) {
            setText(ticketCode, "#TIX-" + String.format("%03d", ticket.getId()));
            setText(ticketTitle, safe(ticket.getTitle(), "Untitled Ticket"));
            setText(descriptionLabel, safe(ticket.getDescription(), "No description provided."));
            setText(createdBy, safe(ticket.getCreatedBy(), "N/A"));
            setText(assignedToLabel, safe(ticket.getAssignedToName(), "Unassigned"));
            setText(departmentLabel, safe(ticket.getDepartmentName(), "N/A"));
            setDateTime(createdDateLabel, createdTimeLabel, ticket.getDateCreated());
            setDateTime(lastUpdatedDateLabel, lastUpdatedTimeLabel, ticket.getLastUpdated());
            setDateTime(deadlineDateLabel, deadlineTimeLabel, ticket.getDeadline());

            populateBadges(ticket);
            handleChangesRequestedNotice(ticket);
            loadCommentHistory(ticket);
        }
    }

    private void handleChangesRequestedNotice(TicketView ticket) {
        if (changesRequestedNoticeContainer == null) {
            return;
        }

        changesRequestedNoticeContainer.getChildren().clear();
        changesRequestedNoticeContainer.setVisible(false);
        changesRequestedNoticeContainer.setManaged(false);

        // Also hide the parent HBox that wraps the notice container to completely remove its footprint
        if (changesRequestedNoticeContainer.getParent() instanceof HBox) {
            changesRequestedNoticeContainer.getParent().setVisible(false);
            changesRequestedNoticeContainer.getParent().setManaged(false);
        }

        if (AppSession.currentUser == null || !AppSession.currentUser.getRole().name().equals("MEMBER")) {
            return;
        }

        if (ticket.getStatus() == null ||
                (!ticket.getStatus().equals("SENT_BACK") && !ticket.getStatus().equals("IN_PROGRESS"))) {
            return;
        }

        // Determine the user and time who rejected it. Since it's from the comments later, we'll placeholder it for now.
        String returnedByStr = "Editor/Executive";
        String returnedDate = "Recently";

        if (ticket.getLastUpdated() != null) {
            returnedDate = ticket.getLastUpdated().format(DateTimeFormatter.ofPattern("MMM d, yyyy • hh:mm a"));
        }

        // IF WE REACH HERE, WE MUST SHOW IT.
        changesRequestedNoticeContainer.setVisible(true);
        changesRequestedNoticeContainer.setManaged(true);
        if (changesRequestedNoticeContainer.getParent() instanceof HBox) {
            changesRequestedNoticeContainer.getParent().setVisible(true);
            changesRequestedNoticeContainer.getParent().setManaged(true);
        }

        VBox noticeBox = new VBox();
        noticeBox.setSpacing(12);
        noticeBox.setPadding(new Insets(20));
        noticeBox.setStyle(
                "-fx-background-color: #FFF6F1; " + "-fx-background-radius: 12; " + "-fx-border-color: #FFE4D6; " +
                        "-fx-border-radius: 12;");
        noticeBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(noticeBox, Priority.ALWAYS);

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);

        Label warningIcon = new Label("!");
        warningIcon.setStyle(
                "-fx-background-color: #F64E60; -fx-background-radius: 10; -fx-text-fill: white; " +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 2 7 2 7;");

        Label headerLabel = new Label("Changes Requested");
        headerLabel.setStyle("-fx-text-fill: #F64E60; -fx-font-family: 'Inter 18pt ExtraBold'; -fx-font-size: 16px;");
        headerBox.getChildren().addAll(warningIcon, headerLabel);

        Label bodyLabel = new Label(
                "Your submission was reviewed and requires changes before it can be approved.\nPlease review the feedback below" +
                        " and resubmit when ready.");
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle(
                "-fx-text-fill: #4B5563; -fx-font-family: 'Inter 18pt Regular'; -fx-font-size: 13px; -fx-line-spacing: 4px;");

        Label footerLabel = new Label("Returned by " + returnedByStr + " • " + returnedDate);
        footerLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-family: 'Inter 18pt Regular'; -fx-font-size: 12px;");

        noticeBox.getChildren().addAll(headerBox, bodyLabel, footerLabel);

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
        if (statusBadgeContainer == null || priorityBadgeContainer == null) {
            return;
        }

        statusBadgeContainer.getChildren().clear();
        statusBadgeContainer.setStyle("-fx-background-color: transparent;");
        statusBadgeContainer.setAlignment(Pos.CENTER_LEFT);

        priorityBadgeContainer.getChildren().clear();
        priorityBadgeContainer.setStyle("-fx-background-color: transparent;");
        priorityBadgeContainer.setAlignment(Pos.CENTER_LEFT);

        // Status Badge
        String status = ticket.getStatus() != null ? ticket.getStatus().toUpperCase() : "OPEN";
        String statusBg = "-dodger-blue-bg-accent";
        String statusText = "-dodger-blue-accent";
        String displayStatus = status.replace("_", " ");

        switch (status) {
            case "IN_PROGRESS":
            case "IN PROGRESS":
                statusBg = "-web-orange-bg-accent";
                statusText = "-web-orange-accent";
                break;
            case "COMPLETED":
                statusBg = "-emerald-green-bg-accent";
                statusText = "-emerald-green-accent";
                break;
            case "RESOLVED":
            case "APPROVED":
                statusBg = "-blue-violet-bg-accent";
                statusText = "-blue-violet-accent";
                break;
        }
        statusBadgeContainer.getChildren().add(createBadge(displayStatus, statusBg, statusText));

        // Priority Badge
        String priority = ticket.getPriority() != null ? ticket.getPriority().toUpperCase() : "MEDIUM";
        String prioBg = "-web-orange-bg-accent";
        String prioText = "-web-orange-accent";

        switch (priority) {
            case "LOW":
                prioBg = "-emerald-green-bg-accent";
                prioText = "-emerald-green-accent";
                break;
            case "HIGH":
            case "URGENT":
                prioBg = "-carnation-red-bg-accent";
                prioText = "-carnation-red-accent";
                break;
        }
        priorityBadgeContainer.getChildren().add(createBadge(priority, prioBg, prioText));
    }

    private void loadCommentHistory(TicketView ticket) {
        if (activityCommentContainer == null || ticket == null) {
            return;
        }

        activityCommentContainer.getChildren().clear();
        List<Comment> comments = commentDAO.findByTicketId(ticket.getId());
        int commentCount = comments != null ? comments.size() : 0;

        if (commentsBadgeContainer != null) {
            commentsBadgeContainer.getChildren().clear();
            Label badge = createBadge(commentCount + " notes", "#eef3ff", "#1c2b63");
            commentsBadgeContainer.getChildren().add(badge);
        }

        if (commentCount == 0) {
            Label emptyLabel = new Label("No comments yet.");
            emptyLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 12px;");
            activityCommentContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Comment comment : comments) {
            activityCommentContainer.getChildren().add(createCommentCard(comment));
        }
    }

    private VBox createCommentCard(Comment comment) {
        Label authorLabel = new Label(safe(comment.getCreatedBy(), "Unknown"));
        authorLabel.setStyle("-fx-text-fill: #1c2b63; -fx-font-size: 12px; -fx-font-weight: bold;");

        String timeText = comment.getDateCreated() != null
                ? comment.getDateCreated().format(dateFormatter) + " at " + comment.getDateCreated().format(timeFormatter)
                : "No timestamp";
        Label timeLabel = new Label(timeText);
        timeLabel.setStyle("-fx-text-fill: #9faad2; -fx-font-size: 10px;");

        Label contentLabel = new Label(safe(comment.getContent(), ""));
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(270.0);
        contentLabel.setStyle("-fx-text-fill: #4b587c; -fx-font-size: 12px; -fx-line-spacing: 3px;");

        VBox card = new VBox(4, authorLabel, timeLabel, contentLabel);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #f7f9fd; -fx-background-radius: 8;");
        return card;
    }

    @FXML
    public void onClickedReturnTicket(ActionEvent event) {
        updateCurrentTicket(TicketStatus.IN_PROGRESS, event);
    }

    @FXML
    public void onClickedResolveTicket(ActionEvent event) {
        updateCurrentTicket(TicketStatus.RESOLVED, event);
    }

    private void updateCurrentTicket(TicketStatus status, ActionEvent event) {
        if (currentTicket == null) {
            showError("No ticket selected.");
            return;
        }

        saveReviewComment();

        if (ticketDAO.updateStatus(currentTicket.getId(), status)) {
            closeModal(event);
        } else {
            showError("Unable to update ticket status.");
        }
    }

    private void saveReviewComment() {
        if (reviewCommentArea == null || currentTicket == null || AppSession.currentUser == null) {
            return;
        }

        String content = reviewCommentArea.getText();
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        commentDAO.createComment(AppSession.currentUser.getUserId(), currentTicket.getId(), content.trim());
    }

    @FXML
    public void onClickedButtonClose(ActionEvent event) {
        closeModal(event);
    }

    private void closeModal(ActionEvent event) {
        Object source = event != null ? event.getSource() : null;
        if (source instanceof Button button && button.getScene() != null && button.getScene().getWindow() instanceof Stage stage) {
            stage.close();
            return;
        }

        if (buttonClose != null && buttonClose.getScene() != null && buttonClose.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private void setText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    private void setDateTime(Label dateLabel, Label timeLabel, java.time.LocalDateTime dateTime) {
        setText(dateLabel, dateTime != null ? dateTime.format(dateFormatter) : "N/A");
        setText(timeLabel, dateTime != null ? dateTime.format(timeFormatter) : "");
    }

    private String safe(String value) {
        return safe(value, "N/A");
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("TIX.org");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
