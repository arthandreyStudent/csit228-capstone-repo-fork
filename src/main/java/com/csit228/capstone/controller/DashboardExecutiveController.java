package com.csit228.capstone.controller;

import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.model.*;
import com.csit228.capstone.utils.Formatter;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardExecutiveController {

    @FXML
    private Label profileInitialsLabel;

    @FXML
    private Label profileNameLabel;

    @FXML
    private Label profileRoleLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Label unassignedLabel;

    @FXML
    private Label inProgressLabel;

    @FXML
    private Label resolvedLabel;

    @FXML
    private Label overdueLabel;

    @FXML
    private HBox departmentTabsBox;

    @FXML
    private VBox pendingAssignmentQueueBox;

    @FXML
    private VBox recentActivityBox;

    @FXML
    private Button createTicketButton;

    @FXML
    private Label resolutionRateLabel;

    @FXML
    private Label resolvedRatePercentLabel;

    @FXML
    private Label inProgressRatePercentLabel;

    @FXML
    private Label overdueRatePercentLabel;

    @FXML
    private ProgressBar resolvedProgressBar;

    @FXML
    private ProgressBar inProgressProgressBar;

    @FXML
    private ProgressBar overdueProgressBar;

    private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
    private final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private final UserDAO userDAO = UserDAO.getUserDAO();

    private List<TicketView> tickets = new ArrayList<>();
    private List<Department> departments = new ArrayList<>();

    private String selectedDepartmentName = null;

    @FXML
    public void initialize() {
        setupProfile();
        setupSearch();

        loadDepartments();
        refreshDashboard();
    }

    private void setupProfile() {
        User user = AppSession.currentUser;

        if (user == null) {
            profileInitialsLabel.setText("NA");
            profileNameLabel.setText("Executive User");
            profileRoleLabel.setText("EXECUTIVE");
            return;
        }

        profileInitialsLabel.setText(Formatter.getInitials(user));
        profileNameLabel.setText(user.getFullName());
        profileRoleLabel.setText(user.getRole() != null ? user.getRole().toString() : "EXECUTIVE");
    }

    private void setupSearch() {
        if (searchField == null) {
            return;
        }

        searchField.setPromptText("Search tickets...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadPendingAssignmentQueue());
    }

    private void loadDepartments() {
        departments = new ArrayList<>(departmentDAO.getDepartments());
        renderDepartmentTabs();
    }

    private void renderDepartmentTabs() {
        departmentTabsBox.getChildren().clear();

        Button allButton = createDepartmentTabButton("All Depts", selectedDepartmentName == null);
        allButton.setOnAction(event -> {
            selectedDepartmentName = null;
            renderDepartmentTabs();
            loadPendingAssignmentQueue();
        });

        departmentTabsBox.getChildren().add(allButton);

        for (Department department : departments) {
            String departmentName = department.getName();

            Button departmentButton = createDepartmentTabButton(
                    departmentName,
                    departmentName.equalsIgnoreCase(selectedDepartmentName)
            );

            departmentButton.setOnAction(event -> {
                selectedDepartmentName = departmentName;
                renderDepartmentTabs();
                loadPendingAssignmentQueue();
            });

            departmentTabsBox.getChildren().add(departmentButton);
        }
    }

    private Button createDepartmentTabButton(String text, boolean selected) {
        Button button = new Button(text);
        button.setPrefHeight(32.0);
        button.setMinWidth(58.0);

        if (selected) {
            button.setStyle(
                    "-fx-background-color: #2f95ff;" +
                            "-fx-background-radius: 20;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 0 18 0 18;"
            );
        } else {
            button.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #dfe7f5;" +
                            "-fx-border-radius: 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-text-fill: #9faad2;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 0 18 0 18;"
            );
        }

        return button;
    }

    private void refreshDashboard() {
        tickets = new ArrayList<>(ticketDAO.getViews());

        updateSummaryCardsAndResolutionRate();
        loadPendingAssignmentQueue();
        loadRecentActivity();
    }

    private void updateSummaryCardsAndResolutionRate() {
        int total = tickets.size();
        int unassigned = 0;
        int inProgress = 0;
        int resolved = 0;
        int overdue = 0;

        for (TicketView ticket : tickets) {
            if (isUnassigned(ticket)) {
                unassigned++;
            }

            if (isStatus(ticket, "IN_PROGRESS")) {
                inProgress++;
            }

            if (isResolved(ticket)) {
                resolved++;
            }

            if (isOverdue(ticket)) {
                overdue++;
            }
        }

        unassignedLabel.setText(String.valueOf(unassigned));
        inProgressLabel.setText(String.valueOf(inProgress));
        resolvedLabel.setText(String.valueOf(resolved));
        overdueLabel.setText(String.valueOf(overdue));

        double resolvedRate = getRate(resolved, total);
        double inProgressRate = getRate(inProgress, total);
        double overdueRate = getRate(overdue, total);

        resolutionRateLabel.setText(Formatter.formatPercent(resolvedRate));
        resolvedRatePercentLabel.setText(Formatter.formatPercent(resolvedRate));
        inProgressRatePercentLabel.setText(Formatter.formatPercent(inProgressRate));
        overdueRatePercentLabel.setText(Formatter.formatPercent(overdueRate));

        resolvedProgressBar.setProgress(resolvedRate);
        inProgressProgressBar.setProgress(inProgressRate);
        overdueProgressBar.setProgress(overdueRate);
    }

    private boolean isAssignableTicket(TicketView ticket) {
        if (ticket == null) {
            return false;
        }

        return !isStatus(ticket, "COMPLETED")
                && !isStatus(ticket, "RESOLVED")
                && !isStatus(ticket, "APPROVED");
    }

    private void loadPendingAssignmentQueue() {
        pendingAssignmentQueueBox.getChildren().clear();

        String keyword = searchField != null ? searchField.getText() : "";

        for (TicketView ticket : tickets) {
            if (!isAssignableTicket(ticket)) {
                continue;
            }

            if (!matchesSelectedDepartment(ticket)) {
                continue;
            }

            if (!matchesTicketSearch(ticket, keyword)) {
                continue;
            }

            List<User> assignableUsers = getAssignableMembersForTicket(ticket);
            ListRowItem row = ListRowItem.forExecutiveAssignment(ticket, assignableUsers);

            row.setAction(event -> {
                User selectedUser = row.getSelectedAssignedUser();

                if (selectedUser == null) {
                    showInfo("Please select a member first.");
                    return;
                }

                boolean success = ticketDAO.assignTicket(selectedUser.getUserId(), ticket.getId());
                boolean updated = ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

                if (success && updated) {
                    showInfo("Ticket assigned to " + selectedUser.getFullName() + ".");
                    refreshDashboard();
                } else {
                    showError("Unable to assign ticket.");
                }
            });

            pendingAssignmentQueueBox.getChildren().add(row);
        }
    }

    private void loadRecentActivity() {
        recentActivityBox.getChildren().clear();

        int count = 0;

        for (TicketView ticket : tickets) {
            Notification notification = new Notification(
                    ticket.getId(),
                    buildActivityMessage(ticket),
                    Formatter.trimOrNA(ticket.getStatus()),
                    false,
                    LocalDateTime.now(),
                    getCurrentUserId()
            );

            recentActivityBox.getChildren().add(ListRowItem.forActivity(notification));

            count++;

            if (count >= 8) {
                break;
            }
        }
    }

    private List<User> getAssignableMembersForTicket(TicketView ticket) {
        List<User> result = new ArrayList<>();

        int departmentId = getDepartmentIdByName(ticket.getDepartmentName());

        if (departmentId > 0) {
            for (User user : userDAO.getUserByDepartment(departmentId)) {
                if (user != null && user.hasRole(Role.MEMBER)) {
                    result.add(user);
                }
            }
        }

        if (result.isEmpty()) {
            result.addAll(getAllMembers());
        }

        return result;
    }

    private List<User> getAllMembers() {
        Map<Integer, User> uniqueUsers = new LinkedHashMap<>();

        for (Department department : departments) {
            for (User user : userDAO.getUserByDepartment(department.getId())) {
                if (user != null && user.hasRole(Role.MEMBER)) {
                    uniqueUsers.put(user.getUserId(), user);
                }
            }
        }

        return new ArrayList<>(uniqueUsers.values());
    }

    private int getDepartmentIdByName(String departmentName) {
        if (departmentName == null) {
            return -1;
        }

        for (Department department : departments) {
            if (department.getName().equalsIgnoreCase(departmentName.trim())) {
                return department.getId();
            }
        }

        return -1;
    }

    private boolean matchesSelectedDepartment(TicketView ticket) {
        if (selectedDepartmentName == null || selectedDepartmentName.trim().isEmpty()) {
            return true;
        }

        return ticket.getDepartmentName() != null
                && ticket.getDepartmentName().equalsIgnoreCase(selectedDepartmentName);
    }

    private boolean matchesTicketSearch(TicketView ticket, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String search = keyword.trim().toLowerCase();

        return Formatter.trimOrNA(ticket.getTitle()).toLowerCase().contains(search)
                || Formatter.trimOrNA(ticket.getDescription()).toLowerCase().contains(search)
                || Formatter.trimOrNA(ticket.getDepartmentName()).toLowerCase().contains(search)
                || Formatter.trimOrNA(ticket.getPriority()).toLowerCase().contains(search)
                || Formatter.trimOrNA(ticket.getStatus()).toLowerCase().contains(search)
                || Formatter.trimOrNA(ticket.getCreatedBy()).toLowerCase().contains(search)
                || Formatter.trimOrNA(ticket.getAssignedToName()).toLowerCase().contains(search);
    }

    private boolean isUnassigned(TicketView ticket) {
        return ticket.getAssignedToName() == null || ticket.getAssignedToName().trim().isEmpty();
    }

    private boolean isStatus(TicketView ticket, String status) {
        return ticket.getStatus() != null && ticket.getStatus().equalsIgnoreCase(status);
    }

    private boolean isResolved(TicketView ticket) {
        return isStatus(ticket, "RESOLVED") || isStatus(ticket, "COMPLETED");
    }

    private boolean isOverdue(TicketView ticketView) {
        return ticketView != null
                && ticketView.getDeadline() != null
                && LocalDate.now().isAfter(ticketView.getDeadline().toLocalDate())
                && !isResolved(ticketView);
    }

    private String buildActivityMessage(TicketView ticket) {
        if (isUnassigned(ticket)) {
            return "\"" + ticket.getTitle() + "\" is waiting for assignment";
        }

        if (isResolved(ticket)) {
            return "\"" + ticket.getTitle() + "\" is resolved";
        }

        return "\"" + ticket.getTitle() + "\" is assigned to " + ticket.getAssignedToName();
    }


    // TODO: Refactor this to open a form within the dashboard instead of switching screens
    // STATUS: DONE!
    @FXML
    public void handleCreateTicket() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/CreateTicketModalExecView.fxml"));
            Parent root = loader.load();
            CreateTicketModalExecController controller = loader.getController();

            Window ownerWindow = getOwnerWindow();
            Stage modalStage = new Stage();
            modalStage.setTitle("Create New Ticket");
            if (ownerWindow != null) {
                modalStage.initOwner(ownerWindow);
                modalStage.initModality(Modality.WINDOW_MODAL);
            } else {
                modalStage.initModality(Modality.APPLICATION_MODAL);
            }
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

            if (controller != null && controller.isSubmitted()) {
                refreshDashboard();
            }
        } catch (IOException e) {
            showError("Unable to open Create Ticket modal.");
        }
    }

    private Window getOwnerWindow() {
        return createTicketButton != null && createTicketButton.getScene() != null
                ? createTicketButton.getScene().getWindow()
                : null;
    }

    private double getRate(int value, int total) {
        if (total <= 0) {
            return 0;
        }

        return (double) value / total;
    }

    private int getCurrentUserId() {
        return AppSession.currentUser != null ? AppSession.currentUser.getUserId() : 0;
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