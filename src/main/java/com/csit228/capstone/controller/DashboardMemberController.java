package com.csit228.capstone.controller;

import com.csit228.capstone.application.TixApp;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.model.Notification;
import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardMemberController {

    @FXML
    private TextField searchField;

    @FXML
    private Label openTasksLabel;

    @FXML
    private Label inProgressLabel;

    @FXML
    private Label completedLabel;

    @FXML
    private Label overdueLabel;

    @FXML
    private VBox availableTicketsBox;

    @FXML
    private VBox volunteerBoardBox;

    @FXML
    private VBox activityBox;

    private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();

    private List<TicketView> tickets = new ArrayList<>();

    @FXML
    public void initialize() {
        setupSearch();
        refreshDashboard();
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

            if (isStatus(ticket, "OPEN")) {
                openTasks++;
            }

            if (isStatus(ticket, "IN_PROGRESS")) {
                inProgress++;
            }

            if (isStatus(ticket, "COMPLETED") || isStatus(ticket, "RESOLVED")) {
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

        for (TicketView ticket : tickets) {
            if (!isAvailableTicket(ticket)) {
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

        for (TicketView ticket : tickets) {
            if (!isAvailableTicket(ticket)) {
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
        User currentUser = TixApp.currentUser;

        if (currentUser == null) {
            showError("No logged-in user found.");
            return;
        }

        boolean success = ticketDAO.assignTicket(currentUser.getUserId(), ticket.getId());
        boolean updated = ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

        if (success && updated) {
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

        return isStatus(ticket, "OPEN") || isStatus(ticket, "IN_PROGRESS");
    }

    private boolean isAssignedToCurrentUser(TicketView ticket) {
        User currentUser = TixApp.currentUser;

        if (currentUser == null || ticket.getAssignedToName() == null) {
            return false;
        }

        return ticket.getAssignedToName().equalsIgnoreCase(currentUser.getFullName());
    }

    private boolean isUnassigned(TicketView ticket) {
        return ticket.getAssignedToName() == null || ticket.getAssignedToName().trim().isEmpty();
    }

    private boolean isOverdue(TicketView ticket) {
        if (ticket.getDeadline() == null) {
            return false;
        }

        if (isStatus(ticket, "COMPLETED") || isStatus(ticket, "RESOLVED")) {
            return false;
        }

        return ticket.getDeadline().isBefore(LocalDateTime.now());
    }

    private boolean isStatus(TicketView ticket, String status) {
        return ticket.getStatus() != null && ticket.getStatus().equalsIgnoreCase(status);
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
        return TixApp.currentUser != null ? TixApp.currentUser.getUserId() : 0;
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