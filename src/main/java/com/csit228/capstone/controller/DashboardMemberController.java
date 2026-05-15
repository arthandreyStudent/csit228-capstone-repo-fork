package com.csit228.capstone.controller;

import com.csit228.capstone.model.Notification;
import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardMemberController extends BaseDashboardController {

    @FXML private Label openTasksLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label completedLabel;
    @FXML private Label overdueLabel;
    @FXML private VBox availableTicketsBox;
    @FXML private VBox volunteerBoardBox;
    @FXML private VBox activityBox;

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

    @Override
    protected void renderDashboard() {
        updateSummaryCards();
        loadAvailableTickets();
        loadVolunteerBoard();
        loadMemberActivity();
    }

    @Override
    protected void onSearchChanged() {
        loadAvailableTickets();
        loadVolunteerBoard();
    }

    @Override
    protected void onDeadlineSortSelected() {
        loadAvailableTickets();
        loadVolunteerBoard();
    }

    @FXML
    public void initialize() {
        setupProfile();
        setupSearch();
        setupDeadlineSortComboBox();
        refreshDashboard();
        startWatching();
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

            if (isStatus(ticket, TicketStatus.OPEN.name()))        openTasks++;
            if (isStatus(ticket, TicketStatus.IN_PROGRESS.name())) inProgress++;
            if (isResolved(ticket))                                 completed++;
            if (isOverdue(ticket))                                  overdue++;
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
            if (!isAvailableTicket(ticket) || isVolunteerTicket(ticket)) continue;
            if (!matchesTicketSearch(ticket, keyword)) continue;

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
            if (!isAvailableTicket(ticket) || !isVolunteerTicket(ticket)) continue;
            if (!matchesTicketSearch(ticket, keyword)) continue;

            ListRowItem row = ListRowItem.forMemberVolunteerTicket(ticket);
            row.setAction(event -> takeTicket(ticket));
            volunteerBoardBox.getChildren().add(row);

            if (++count >= 8) break;
        }
    }

    private void loadMemberActivity() {
        activityBox.getChildren().clear();

        int count = 0;

        for (TicketView ticket : tickets) {
            if (!isAssignedToCurrentUser(ticket)) continue;

            Notification notification = new Notification(
                    ticket.getId(),
                    "You have \"" + ticket.getTitle() + "\" with status " + safe(ticket.getStatus()),
                    false,                  // isRead
                    LocalDateTime.now(),
                    getCurrentUserId()
            );

            activityBox.getChildren().add(ListRowItem.forActivity(notification));

            if (++count >= 8) break;
        }
    }

    private void takeTicket(TicketView ticket) {
        User currentUser = AppSession.currentUser;

        if (currentUser == null) {
            showError("No logged-in user found.");
            return;
        }

        boolean assigned = ticketDAO.assignTicket(currentUser.getUserId(), ticket.getId());
        boolean updated  = ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

        if (assigned && updated) {
            showInfo("Ticket added to your tasks.");
            refreshDashboard();
        } else {
            showError("Unable to take ticket.");
        }
    }
    private boolean isAvailableTicket(TicketView ticket) {
        if (ticket == null || !isUnassigned(ticket)) return false;
        return isStatus(ticket, TicketStatus.OPEN.name())
                || isStatus(ticket, TicketStatus.IN_PROGRESS.name());
    }

    private boolean isAssignedToCurrentUser(TicketView ticket) {
        User currentUser = AppSession.currentUser;
        if (currentUser == null || ticket.getAssignedToName() == null) return false;
        return ticket.getAssignedToName().equalsIgnoreCase(currentUser.getFullName());
    }

    private String safe(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value.trim();
    }
}
