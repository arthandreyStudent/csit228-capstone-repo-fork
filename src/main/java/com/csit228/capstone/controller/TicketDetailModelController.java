package com.csit228.capstone.controller;

import com.csit228.capstone.dao.CommentDAO;
import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.enums.Role;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.Comment;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.observer.CommentObserver;
import com.csit228.capstone.observer.CommentWatcher;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.NotificationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TicketDetailModelController implements CommentObserver {

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
    public ComboBox<User> assignedToComboBox;
    @FXML
    public Button saveAssignmentButton;
    @FXML
    public Button returnTicketButton;
    @FXML
    public Button resolveTicketButton;
    @FXML
    public TextArea reviewCommentArea;

    private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
    private final CommentDAO commentDAO = CommentDAO.getCommentDAO();
    private final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private final UserDAO userDAO = UserDAO.getUserDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    public TextField reviewTitleField;

    private TicketView currentTicket;
    private boolean memberMode = false;
    private Runnable refreshCallback;

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

        if (reviewCommentArea != null) {
            reviewCommentArea.textProperty().addListener((observable, oldValue, newValue) -> updateSaveAssignmentButtonState());
        }
    }

    @Override
    public int getTicketId() {
        return currentTicket != null ? currentTicket.getId() : -1;
    }

    @Override
    public void onCommentsChanged(List<Comment> updatedComments) {
        if (currentTicket != null) {
            TicketView fresh = ticketDAO.getTicketViewById(currentTicket.getId());
            if (fresh != null && !fresh.getStatus().equalsIgnoreCase(currentTicket.getStatus())) {
                currentTicket = fresh;
                configureActionButtons(currentTicket);
                populateBadges(currentTicket);
            }
        }

        loadCommentHistory(updatedComments);
    }


    public void loadTicket(TicketView ticket) {
        this.currentTicket = ticket;

        if (ticket == null) {
            return;
        }

        String assignedToName = ticket.getAssignedToName();

        String assignedToDisplay = assignedToName == null || assignedToName.trim().isEmpty()
                ? "Ticket Still Unassigned"
                : assignedToName.trim();

        setText(ticketCode, "#TIX-" + String.format("%03d", ticket.getId()));
        setText(ticketTitle, safe(ticket.getTitle(), "Untitled Ticket"));
        setText(descriptionLabel, safe(ticket.getDescription(), "No description provided."));
        setText(createdBy, safe(ticket.getCreatedBy(), "N/A"));
        setText(assignedToLabel, assignedToDisplay);
        setText(departmentLabel, safe(ticket.getDepartmentName(), "N/A"));

        configureAssignmentDropdown(ticket);
        configureActionButtons(ticket);

        setDateTime(createdDateLabel, createdTimeLabel, ticket.getDateCreated());
        setDateTime(lastUpdatedDateLabel, lastUpdatedTimeLabel, ticket.getLastUpdated());
        setDateTime(deadlineDateLabel, deadlineTimeLabel, ticket.getDeadline());

        populateBadges(ticket);
        startWatchingComments(ticket);
    }

    private void startWatchingComments(TicketView ticket) {
        CommentWatcher watcher = CommentWatcher.getInstance();

        List<Comment> initial = commentDAO.findByTicketId(ticket.getId());
        watcher.setInitialCount(ticket.getId(), initial != null ? initial.size() : 0);

        loadCommentHistory(initial != null ? initial : new ArrayList<>());

        watcher.addObserver(this);
        watcher.start(3);
    }

    private void stopWatchingComments() {
        if (currentTicket == null) return;
        CommentWatcher watcher = CommentWatcher.getInstance();
        watcher.removeObserver(this);
        watcher.clearTicket(currentTicket.getId());
    }


    public void loadTicketForMember(TicketView ticket, Runnable refreshCallback) {
        this.memberMode = true;
        this.refreshCallback = refreshCallback;

        loadTicket(ticket);

        boolean canSubmit = isStatus(ticket, TicketStatus.IN_PROGRESS);

        setVisible(returnTicketButton, false);
        setVisible(resolveTicketButton, false);
        setVisible(assignedToComboBox, false);
        setVisible(assignedToLabel, true);

        if (saveAssignmentButton != null) {
            saveAssignmentButton.setText("Submit");
            setVisible(saveAssignmentButton, canSubmit);
            saveAssignmentButton.setDisable(!canSubmit);
        }

        if (reviewCommentArea != null) {
            reviewCommentArea.clear();
            reviewCommentArea.setEditable(canSubmit);

            if (canSubmit) {
                reviewCommentArea.setPromptText("Add a note before submitting your task...");
            } else {
                reviewCommentArea.setPromptText("This ticket is no longer editable.");
            }
        }
    }

    private void configureAssignmentDropdown(TicketView ticket) {
        if (assignedToComboBox == null) {
            return;
        }

        assignedToComboBox.setOnAction(null);
        assignedToComboBox.getItems().clear();
        assignedToComboBox.setValue(null);
        assignedToComboBox.setDisable(false);
        assignedToComboBox.setButtonCell(userNameCell());
        assignedToComboBox.setCellFactory(listView -> userNameCell());
        if (saveAssignmentButton != null) {
            saveAssignmentButton.setDisable(true);
        }

        boolean showDropdown = isOpenUnassigned(ticket);
        setVisible(assignedToLabel, !showDropdown);
        setVisible(assignedToComboBox, showDropdown);

        if (!showDropdown) {
            return;
        }

        List<User> departmentUsers = getDepartmentUsers(ticket);
        assignedToComboBox.getItems().setAll(departmentUsers);

        if (departmentUsers.isEmpty()) {
            assignedToComboBox.setPromptText("No names found");
            assignedToComboBox.setDisable(true);
            return;
        }

        assignedToComboBox.setPromptText("Assign to...");
        assignedToComboBox.setOnAction(event -> updateSaveAssignmentButtonState());
    }

    @FXML
    public void onClickedSaveAssignment(ActionEvent event) {
        if (memberMode) {
            submitMemberTicket(event);
            return;
        }

        if (isOpenUnassigned(currentTicket)) {
            assignSelectedUser();
            return;
        }

        if (isStatus(currentTicket, TicketStatus.IN_PROGRESS)) {
            saveStandaloneComment();
        }
    }

    private void updateSaveAssignmentButtonState() {
        if (saveAssignmentButton == null) {
            return;
        }

        if (memberMode) {
            saveAssignmentButton.setDisable(!isStatus(currentTicket, TicketStatus.IN_PROGRESS));
            return;
        }

        if (assignedToComboBox == null) {
            return;
        }

        if (isOpenUnassigned(currentTicket)) {
            saveAssignmentButton.setDisable(assignedToComboBox.getSelectionModel().getSelectedItem() == null);
            return;
        }

        if (isStatus(currentTicket, TicketStatus.IN_PROGRESS)) {
            saveAssignmentButton.setDisable(!hasReviewComment());
            return;
        }

        saveAssignmentButton.setDisable(true);
    }

    private List<User> getDepartmentUsers(TicketView ticket) {
        List<User> departmentUsers = new ArrayList<>();
        if (ticket == null || ticket.isVolunteerTicket()) {
            return departmentUsers;
        }

        Integer departmentId = departmentDAO.getDepartmentByName(ticket.getDepartmentName());
        if (departmentId == null) {
            return departmentUsers;
        }

        int currentUserId = AppSession.currentUser != null ? AppSession.currentUser.getUserId() : -1;

        for (User user : userDAO.getUsersByDepartment(departmentId)) {
            if (user != null && user.getUserId() != currentUserId) {
                departmentUsers.add(user);
            }
        }
        departmentUsers.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));
        return departmentUsers;
    }

    private ListCell<User> userNameCell() {
        return new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFullName());
            }
        };
    }

    private void assignSelectedUser() {
        if (currentTicket == null || assignedToComboBox == null) {
            return;
        }

        User selectedUser = assignedToComboBox.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            return;
        }

        boolean assigned = ticketDAO.assignTicket(selectedUser.getUserId(), currentTicket.getId());
        boolean updated = ticketDAO.updateStatus(currentTicket.getId(), TicketStatus.IN_PROGRESS);

        if (!assigned || !updated) {
            showError("Unable to assign ticket.");
            assignedToComboBox.getSelectionModel().clearSelection();
            updateSaveAssignmentButtonState();
            return;
        }

        saveReviewComment();
        NotificationManager.notifyAssignee(selectedUser, currentTicket.getTitle(), getCurrentUserName());
        currentTicket.setStatus(TicketStatus.IN_PROGRESS.name());
        setText(assignedToLabel, selectedUser.getFullName());
        setVisible(assignedToLabel, true);
        setVisible(assignedToComboBox, false);
        configureActionButtons(currentTicket);
        populateBadges(currentTicket);
    }

    private void configureActionButtons(TicketView ticket) {
        if (memberMode) {
            boolean canSubmit = isStatus(ticket, TicketStatus.IN_PROGRESS);

            setVisible(saveAssignmentButton, canSubmit);
            if (saveAssignmentButton != null) {
                saveAssignmentButton.setText("Submit");
                saveAssignmentButton.setDisable(!canSubmit);
            }

            setVisible(returnTicketButton, false);
            setVisible(resolveTicketButton, false);
            return;
        }

        boolean showReviewActions = hasAssignee(ticket) && isStatus(ticket, TicketStatus.COMPLETED);
        boolean showSaveAction = isOpenUnassigned(ticket) || isStatus(ticket, TicketStatus.IN_PROGRESS);

        setVisible(saveAssignmentButton, showSaveAction);
        if (saveAssignmentButton != null) {
            saveAssignmentButton.setText(isStatus(ticket, TicketStatus.IN_PROGRESS) ? "Comment" : "Save");
        }

        updateSaveAssignmentButtonState();

        setVisible(returnTicketButton, showReviewActions);
        setVisible(resolveTicketButton, showReviewActions);
    }

    private void saveStandaloneComment() {
        if (!hasReviewComment()) {
            showError("Please enter a comment first.");
            return;
        }

        saveReviewComment();

        List<Comment> updated = commentDAO.findByTicketId(currentTicket.getId());
        CommentWatcher.getInstance().setInitialCount(currentTicket.getId(), updated.size());
        loadCommentHistory(updated);

        updateSaveAssignmentButtonState();
    }

    private boolean isOpenUnassigned(TicketView ticket) {
        return ticket != null && isStatus(ticket, TicketStatus.OPEN) && !hasAssignee(ticket);
    }

    private boolean hasAssignee(TicketView ticket) {
        return ticket != null && !isBlank(ticket.getAssignedToName());
    }

    private boolean isStatus(TicketView ticket, TicketStatus status) {
        return ticket != null && ticket.getStatus() != null && ticket.getStatus().equalsIgnoreCase(status.name());
    }

    private String getCurrentUserName() {
        if (AppSession.currentUser != null) {
            return AppSession.currentUser.getFullName();
        }
        return safe(currentTicket != null ? currentTicket.getCreatedBy() : null, "TIX.org");
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

    private void loadCommentHistory(List<Comment> comments) {
        if (activityCommentContainer == null) return;

        activityCommentContainer.getChildren().clear();
        int count = comments.size();

        if (commentsBadgeContainer != null) {
            commentsBadgeContainer.getChildren().clear();
            commentsBadgeContainer.getChildren().add(createBadge(count + " notes", "#eef3ff", "#1c2b63"));
        }

        if (count == 0) {
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

            if (status == TicketStatus.IN_PROGRESS) {
                NotificationManager.notifyReturnTicket(currentTicket, getCurrentUserName());
            }

            if (status == TicketStatus.RESOLVED) {
                NotificationManager.notifyApprove(currentTicket, getCurrentUserName());
            }

            currentTicket.setStatus(status.name());

            if (refreshCallback != null) {
                refreshCallback.run();
            }

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
        reviewCommentArea.clear();
    }

    private void submitMemberTicket(ActionEvent event) {
        if (currentTicket == null) {
            showError("No ticket selected.");
            return;
        }

        if (!isStatus(currentTicket, TicketStatus.IN_PROGRESS)) {
            showError("Only in-progress tickets can be submitted.");
            return;
        }

        saveReviewComment();

        boolean updated = ticketDAO.updateStatus(currentTicket.getId(), TicketStatus.COMPLETED);

        if (updated) {
            currentTicket.setStatus(TicketStatus.COMPLETED.name());
            NotificationManager.notifySubmitted(currentTicket, getCurrentUserName());
            if (refreshCallback != null) {
                refreshCallback.run();
            }

            closeModal(event);
        } else {
            showError("Unable to submit task.");
        }
    }
    @FXML
    public void onClickedButtonClose(ActionEvent event) {
        closeModal(event);
    }

    private void closeModal(ActionEvent event) {
        stopWatchingComments();
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

    private void setVisible(Node node, boolean visible) {
        if (node != null) {
            node.setManaged(visible);
            node.setVisible(visible);
        }
    }

    private void setDateTime(Label dateLabel, Label timeLabel, java.time.LocalDateTime dateTime) {
        setText(dateLabel, dateTime != null ? dateTime.format(dateFormatter) : "N/A");
        setText(timeLabel, dateTime != null ? dateTime.format(timeFormatter) : "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean hasReviewComment() {
        return reviewCommentArea != null && !isBlank(reviewCommentArea.getText());
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
