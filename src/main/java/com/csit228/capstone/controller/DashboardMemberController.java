package com.csit228.capstone.controller;

import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.model.Notification;
import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.Controls;
import com.csit228.capstone.utils.Formatter;
import com.csit228.capstone.utils.ListRowItem;
import com.csit228.capstone.utils.TicketDeadlineComparator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardMemberController {

    @FXML private TextField searchField;
    @FXML private Label profileInitialsLabel;
    @FXML private Label profileNameLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Label openTasksLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label completedLabel;
    @FXML private Label overdueLabel;
    @FXML private VBox availableTicketsBox;
    @FXML private VBox volunteerBoardBox;
    @FXML private VBox activityBox;
    @FXML private Button buttonLogout;
    @FXML private ComboBox<String> deadlineSortComboBox;

    private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();

    private List<TicketView> tickets = new ArrayList<>();




    @FXML
    public void initialize() {
        setupProfile();
        setupSearch();
        setupDeadlineSortComboBox();
        refreshDashboard();
    }

    @FXML
    public void onClickedLogout() throws IOException {
        AppSession.clearSession();
        Controls.switchScreen("LoginView.fxml");
    }

    private void setupProfile() {
        User user = AppSession.currentUser;

        if (user == null) {
            profileInitialsLabel.setText("NA");
            profileNameLabel.setText("Member User");
            profileRoleLabel.setText("MEMBER");
            return;
        }

        profileInitialsLabel.setText(Formatter.getInitials(user));
        profileNameLabel.setText(user.getFullName());
        profileRoleLabel.setText(user.getRole() != null ? user.getRole().toString() : "MEMBER");
    }

    private void setupSearch() {
        if (searchField == null) {
            return;
        }

        searchField.setPromptText("Search tickets...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadAvailableTickets();
            loadVolunteerBoard();
        });
    }

    private void setupDeadlineSortComboBox() {
        if (deadlineSortComboBox == null) {
            return;
        }

        deadlineSortComboBox.getItems().setAll("Nearest Deadline", "Farthest Deadline");
        deadlineSortComboBox.setValue("Nearest Deadline");
    }

    @FXML
    public void onDeadlineSortChanged() {
        loadAvailableTickets();
        loadVolunteerBoard();
    }

    private void refreshDashboard() {
        ticketDAO.getTicketViews();
        tickets = new ArrayList<>(ticketDAO.getViews());

        updateSummaryCards();
        loadAvailableTickets();
        loadVolunteerBoard();
        loadActivity();
    }

    private void updateSummaryCards() {
        int openTasks = 0;
        int inProgress = 0;
        int completed = 0;
        int overdue = 0;

        for (TicketView ticket : tickets) {
            if (!isAssignedToCurrentUser(ticket)) {
                continue;
            }

            if (isStatus(ticket, TicketStatus.OPEN.name())) {
                openTasks++;
            }

            if (isStatus(ticket, TicketStatus.IN_PROGRESS.name())) {
                inProgress++;
            }

            if (isStatus(ticket, TicketStatus.COMPLETED.name()) || isStatus(ticket, TicketStatus.RESOLVED.name())) {
                completed++;
            }

            if (isOverdue(ticket)) {
                overdue++;
            }
        }

        openTasksLabel.setText(String.valueOf(openTasks));
        inProgressLabel.setText(String.valueOf(inProgress));
        completedLabel.setText(String.valueOf(completed));
        overdueLabel.setText(String.valueOf(overdue));
    }

    private void loadAvailableTickets() {
        availableTicketsBox.getChildren().clear();

        String keyword = searchField != null ? searchField.getText() : "";

        for (TicketView ticket : getSortedTicketsByDeadline()) {
            if (!isAvailableTicket(ticket)) {
                continue;
            }

            if (isVolunteerTicket(ticket)) {
                continue;
            }

            if (!matchesTicketSearch(ticket, keyword)) {
                continue;
            }

            ListRowItem row = ListRowItem.forMemberAvailableTicket(ticket);
            row.setAction(event -> takeTicket(ticket));

            availableTicketsBox.getChildren().add(row);
        }
    }

    private void loadVolunteerBoard() {
        volunteerBoardBox.getChildren().clear();

        String keyword = searchField != null ? searchField.getText() : "";
        int count = 0;

        for (TicketView ticket : getSortedTicketsByDeadline()) {
            if (!isAvailableTicket(ticket)) {
                continue;
            }

            if (!isVolunteerTicket(ticket)) {
                continue;
            }

            if (!matchesTicketSearch(ticket, keyword)) {
                continue;
            }

            ListRowItem row = ListRowItem.forMemberVolunteerTicket(ticket);
            row.setAction(event -> takeTicket(ticket));

            volunteerBoardBox.getChildren().add(row);
            count++;

            if (count >= 8) {
                break;
            }
        }
    }

    private List<TicketView> getSortedTicketsByDeadline() {
        List<TicketView> sortedTickets = new ArrayList<>(tickets);

        TicketDeadlineComparator.SortMode sortMode =
                TicketDeadlineComparator.getSortModeFromText(
                        deadlineSortComboBox != null ? deadlineSortComboBox.getValue() : null
                );

        sortedTickets.sort(new TicketDeadlineComparator(sortMode));

        return sortedTickets;
    }

    private void loadActivity() {
        activityBox.getChildren().clear();

        int count = 0;

        for (TicketView ticket : tickets) {
            if (!isAssignedToCurrentUser(ticket)) {
                continue;
            }

            Notification notification = new Notification(
                    ticket.getId(),
                    "You have \"" + ticket.getTitle() + "\" with status " + safe(ticket.getStatus()),
                    safe(ticket.getStatus()),
                    false,
                    LocalDateTime.now(),
                    getCurrentUserId()
            );

            activityBox.getChildren().add(ListRowItem.forActivity(notification));
            count++;

            if (count >= 8) {
                break;
            }
        }
    }

    private void takeTicket(TicketView ticket) {
        User currentUser = AppSession.currentUser;

        if (currentUser == null) {
            showError("No logged-in user found.");
            return;
        }

        boolean assigned = ticketDAO.assignTicket(currentUser.getUserId(), ticket.getId());
        boolean updated = ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

        if (assigned && updated) {
            showInfo("Ticket added to your tasks.");
            refreshDashboard();
        } else {
            showError("Unable to take ticket.");
        }
    }

    private boolean isAvailableTicket(TicketView ticket) {
        if (ticket == null) {
            return false;
        }

        if (!isUnassigned(ticket)) {
            return false;
        }

        return isStatus(ticket, TicketStatus.OPEN.name()) || isStatus(ticket, TicketStatus.IN_PROGRESS.name());
    }

    private boolean isVolunteerTicket(TicketView ticket) {
        if (ticket == null) {
            return false;
        }

        String departmentName = ticket.getDepartmentName();

        return departmentName == null
                || departmentName.trim().isEmpty()
                || departmentName.equalsIgnoreCase("N/A")
                || departmentName.equalsIgnoreCase("Volunteer");
    }

    private boolean isAssignedToCurrentUser(TicketView ticket) {
        User currentUser = AppSession.currentUser;

        if (currentUser == null || ticket.getAssignedToName() == null) {
            return false;
        }

        return ticket.getAssignedToName().equalsIgnoreCase(currentUser.getFullName());
    }

    private boolean isUnassigned(TicketView ticket) {
        return ticket.getAssignedToName() == null || ticket.getAssignedToName().trim().isEmpty();
    }

    private boolean isOverdue(TicketView ticket) {
        if (ticket == null || ticket.getDeadline() == null) {
            return false;
        }

        if (isStatus(ticket, TicketStatus.COMPLETED.name()) || isStatus(ticket, TicketStatus.RESOLVED.name())) {
            return false;
        }

        return ticket.getDeadline().isBefore(LocalDateTime.now());
    }

    private boolean isStatus(TicketView ticket, String status) {
        return ticket != null && ticket.getStatus() != null && ticket.getStatus().equalsIgnoreCase(status);
    }

    private boolean matchesTicketSearch(TicketView ticket, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String search = keyword.trim().toLowerCase();

        return safe(ticket.getTitle()).toLowerCase().contains(search)
                || safe(ticket.getDescription()).toLowerCase().contains(search)
                || safe(ticket.getDepartmentName()).toLowerCase().contains(search)
                || safe(ticket.getPriority()).toLowerCase().contains(search)
                || safe(ticket.getStatus()).toLowerCase().contains(search)
                || safe(ticket.getCreatedBy()).toLowerCase().contains(search)
                || safe(ticket.getAssignedToName()).toLowerCase().contains(search);
    }

    private int getCurrentUserId() {
        return AppSession.currentUser != null ? AppSession.currentUser.getUserId() : 0;
    }

    private String safe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "N/A";
        }

        return value.trim();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TIX.org");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("TIX.org");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
