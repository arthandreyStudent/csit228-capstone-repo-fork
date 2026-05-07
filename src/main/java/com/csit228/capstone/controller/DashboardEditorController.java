package com.csit228.capstone.controller;

import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Notification;
import com.csit228.capstone.model.Role;
import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.Controls;
import com.csit228.capstone.utils.Formatter;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardEditorController {

    @FXML
    private Label reviewQueueCountLabel;

    @FXML
    private Label profileInitialsLabel;

    @FXML
    private Label profileNameLabel;

    @FXML
    private Label profileRoleLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Label awaitingReviewLabel;

    @FXML
    private Label inProgressLabel;

    @FXML
    private Label approvedTodayLabel;

    @FXML
    private Label sentBackLabel;

    @FXML
    private Button createTicketButton;

    @FXML
    private VBox reviewQueueBox;

    @FXML
    private Label reviewApprovalPercentLabel;

    @FXML
    private Label approvedStatLabel;

    @FXML
    private Label sentBackStatLabel;

    @FXML
    private Label editedStatLabel;

    @FXML
    private Label totalReviewsStatLabel;

    @FXML
    private VBox recentActivityBox;

    @FXML
    private Button allFilterButton;

    @FXML
    private Button openFilterButton;

    @FXML
    private Button inProgressFilterButton;

    @FXML
    private Button completedFilterButton;

    @FXML
    private Button resolvedFilterButton;

    @FXML
    private Button buttonLogout;

    private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
    private final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private final UserDAO userDAO = UserDAO.getUserDAO();

    private List<TicketView> tickets = new ArrayList<>();
    private List<Department> departments = new ArrayList<>();

    private ReviewQueueFilter currentFilter = ReviewQueueFilter.ALL;

    private enum ReviewQueueFilter {
        ALL,
        OPEN,
        IN_PROGRESS,
        TO_BE_REVIEWED,
        RESOLVED
    }

    @FXML
    public void initialize() {
        setupProfile();
        setupSearch();
        setupFilterButtons();
        loadDepartments();
        refreshDashboard();
    }

    private void setupProfile() {
        User user = AppSession.currentUser;

        if (user == null) {
            profileInitialsLabel.setText("NA");
            profileNameLabel.setText("Editor User");
            profileRoleLabel.setText("EDITOR");
            return;
        }

        profileInitialsLabel.setText(Formatter.getInitials(user));
        profileNameLabel.setText(user.getFullName());
        profileRoleLabel.setText(user.getRole() != null ? user.getRole().toString() : "EDITOR");
    }

    private void setupSearch() {
        if (searchField == null) {
            return;
        }

        searchField.setPromptText("Search tickets...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadReviewQueue());
    }

    private void setupFilterButtons() {
        setActiveFilterButton(allFilterButton);
    }

    @FXML
    public void showAllTickets() {
        currentFilter = ReviewQueueFilter.ALL;
        setActiveFilterButton(allFilterButton);
        loadReviewQueue();
    }

    @FXML
    public void showOpenTickets() {
        currentFilter = ReviewQueueFilter.OPEN;
        setActiveFilterButton(openFilterButton);
        loadReviewQueue();
    }

    @FXML
    public void showInProgressTickets() {
        currentFilter = ReviewQueueFilter.IN_PROGRESS;
        setActiveFilterButton(inProgressFilterButton);
        loadReviewQueue();
    }

    @FXML
    public void showCompletedTickets() {
        currentFilter = ReviewQueueFilter.TO_BE_REVIEWED;
        setActiveFilterButton(completedFilterButton);
        loadReviewQueue();
    }

    @FXML
    public void showResolvedTickets() {
        currentFilter = ReviewQueueFilter.RESOLVED;
        setActiveFilterButton(resolvedFilterButton);
        loadReviewQueue();
    }

    private void setActiveFilterButton(Button activeButton) {
        setInactiveFilterStyle(allFilterButton);
        setInactiveFilterStyle(openFilterButton);
        setInactiveFilterStyle(inProgressFilterButton);
        setInactiveFilterStyle(completedFilterButton);
        setInactiveFilterStyle(resolvedFilterButton);

        if (activeButton != null) {
            activeButton.setStyle(
                    "-fx-background-color: #ff9900;" +
                            "-fx-background-radius: 18;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;"
            );
        }
    }

    private void setInactiveFilterStyle(Button button) {
        if (button == null) {
            return;
        }

        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #dfe7f5;" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-radius: 18;" +
                        "-fx-text-fill: #9faad2;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );
    }

    private void loadDepartments() {
        departments = new ArrayList<>(departmentDAO.getDepartments());
    }

    private void refreshDashboard() {
        tickets = new ArrayList<>(ticketDAO.getViews());

        updateSummaryCardsAndReviewStats();
        loadReviewQueue();
        loadRecentActivity();
    }

    private void updateSummaryCardsAndReviewStats() {
        int open = 0;
        int inProgress = 0;
        int toBeReviewed = 0;
        int resolved = 0;
        int sentBack = 0;
        int edited = 0;
        int totalReviews = 0;

        for (TicketView ticket : tickets) {
            if (isUnassigned(ticket)) {
                open++;
            }

            if (isStatus(ticket, TicketStatus.IN_PROGRESS.name())) {
                inProgress++;
                edited++;
            }

            if (isStatus(ticket, TicketStatus.COMPLETED.name())) {
                toBeReviewed++;
                totalReviews++;
            }

            if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
                resolved++;
                totalReviews++;
            }
        }

        awaitingReviewLabel.setText(String.valueOf(toBeReviewed));
        inProgressLabel.setText(String.valueOf(inProgress));
        approvedTodayLabel.setText(String.valueOf(resolved));
        sentBackLabel.setText(String.valueOf(sentBack));

        approvedStatLabel.setText(String.valueOf(resolved));
        sentBackStatLabel.setText(String.valueOf(sentBack));
        editedStatLabel.setText(String.valueOf(edited));
        totalReviewsStatLabel.setText(String.valueOf(totalReviews));

        double approvalRate = totalReviews <= 0 ? 0 : (double) resolved / totalReviews;
        reviewApprovalPercentLabel.setText(Formatter.formatPercent(approvalRate));

        reviewQueueCountLabel.setText(String.valueOf(getFilteredTicketCount()));
    }

    private void loadReviewQueue() {
        reviewQueueBox.getChildren().clear();

        String keyword = searchField != null ? searchField.getText() : "";

        for (TicketView ticket : tickets) {
            if (!matchesCurrentFilter(ticket)) {
                continue;
            }

            if (!matchesTicketSearch(ticket, keyword)) {
                continue;
            }

            List<User> assignableUsers = getAssignableMembersForTicket(ticket);
            ListRowItem row = ListRowItem.forEditorReview(ticket, assignableUsers);

            if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
                lockResolvedTicketRow(row);
                reviewQueueBox.getChildren().add(row);
                continue;
            }

            // Save button, used for assigning or reassigning.
            row.setSecondaryAction(event -> {
                if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
                    showInfo("This ticket is already resolved and closed.");
                    return;
                }

                User selectedUser = row.getSelectedAssignedUser();

                if (selectedUser == null) {
                    showInfo("Please select a member first.");
                    return;
                }

                boolean assigned = ticketDAO.assignTicket(selectedUser.getUserId(), ticket.getId());
                boolean updated = ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

                if (assigned && updated) {
                    showInfo("Ticket assigned to " + selectedUser.getFullName() + ".");
                    refreshDashboard();
                } else {
                    showError("Unable to assign ticket.");
                }
            });

            /*
             * If the ticket is still OPEN or IN_PROGRESS,
             * hide the approve and return buttons.
             * The editor can only assign or reassign it.
             */
            if (!isStatus(ticket, TicketStatus.COMPLETED.name())) {
                hideReviewActionButtons(row);
                reviewQueueBox.getChildren().add(row);
                continue;
            }

            // Check button, only visible for COMPLETED tickets.
            row.setAction(event -> {
                updateTicketStatus(
                        ticket,
                        TicketStatus.RESOLVED,
                        "Ticket marked as resolved."
                );
            });

            // Return button, only visible for COMPLETED tickets.
            row.setThirdAction(event -> {
                updateTicketStatus(
                        ticket,
                        TicketStatus.IN_PROGRESS,
                        "Ticket returned to in progress."
                );
            });

            reviewQueueBox.getChildren().add(row);
        }

        reviewQueueCountLabel.setText(String.valueOf(reviewQueueBox.getChildren().size()));
    }

    private void hideReviewActionButtons(ListRowItem row) {
        if (row == null) {
            return;
        }

        if (row.getActionButton() != null) {
            row.getActionButton().setDisable(true);
            row.getActionButton().setVisible(false);
            row.getActionButton().setManaged(false);
        }

        if (row.getThirdActionButton() != null) {
            row.getThirdActionButton().setDisable(true);
            row.getThirdActionButton().setVisible(false);
            row.getThirdActionButton().setManaged(false);
        }
    }

    private void lockResolvedTicketRow(ListRowItem row) {
        if (row == null) {
            return;
        }

        if (row.getAssignComboBox() != null) {
            row.getAssignComboBox().setDisable(true);
            row.getAssignComboBox().setPromptText("Closed");
            row.getAssignComboBox().setStyle(
                    "-fx-background-color: #f5f7fb;" +
                            "-fx-border-color: #dfe7f5;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-font-size: 10px;" +
                            "-fx-text-fill: #9faad2;"
            );
        }

        if (row.getSecondaryActionButton() != null) {
            row.getSecondaryActionButton().setDisable(true);
            row.getSecondaryActionButton().setText("Closed");
            row.getSecondaryActionButton().setStyle(
                    "-fx-background-color: #eef2fb;" +
                            "-fx-background-radius: 7;" +
                            "-fx-text-fill: #9faad2;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;"
            );
        }

        if (row.getActionButton() != null) {
            row.getActionButton().setDisable(true);
            row.getActionButton().setVisible(false);
            row.getActionButton().setManaged(false);
        }

        if (row.getThirdActionButton() != null) {
            row.getThirdActionButton().setDisable(true);
            row.getThirdActionButton().setVisible(false);
            row.getThirdActionButton().setManaged(false);
        }
    }

    private boolean matchesCurrentFilter(TicketView ticket) {
        if (ticket == null) {
            return false;
        }

        switch (currentFilter) {
            case ALL:
                return true;

            case OPEN:
                return isUnassigned(ticket);

            case IN_PROGRESS:
                return isStatus(ticket, TicketStatus.IN_PROGRESS.name());

            case TO_BE_REVIEWED:
                return isStatus(ticket, TicketStatus.COMPLETED.name());

            case RESOLVED:
                return isStatus(ticket, TicketStatus.RESOLVED.name());

            default:
                return true;
        }
    }

    private int getFilteredTicketCount() {
        int count = 0;

        for (TicketView ticket : tickets) {
            if (matchesCurrentFilter(ticket)) {
                count++;
            }
        }

        return count;
    }

    private void updateTicketStatus(TicketView ticket, TicketStatus status, String successMessage) {
        if (ticket == null) {
            showError("No ticket selected.");
            return;
        }

        boolean updated = ticketDAO.updateStatus(ticket.getId(), status);

        if (updated) {
            showInfo(successMessage);
            refreshDashboard();
        } else {
            showError("Unable to update ticket status.");
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

    private boolean isUnassigned(TicketView ticket) {
        return ticket.getAssignedToName() == null || ticket.getAssignedToName().trim().isEmpty();
    }

    // TODO: Refactor this to open a form within the dashboard instead of switching screens
    // STATUS: DONE!
    @FXML
    public void handleCreateTicket() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/CreateTicketModalEditorView.fxml"));
            Parent root = loader.load();
            CreateTicketModalEditorController controller = loader.getController();

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
            modalStage.sizeToScene();
            modalStage.setOnShown(event -> modalStage.centerOnScreen());
            modalStage.showAndWait();

            if (controller != null && controller.isSubmitted()) {
                refreshDashboard();
            }
        } catch (IOException e) {
            showError("Unable to open Create Ticket modal.");
        }
    }

    @FXML
    public void onClickedLogout() throws IOException {
        AppSession.clearSession();
        Controls.switchScreen("LoginView.fxml");
    }

    private Window getOwnerWindow() {
        return createTicketButton != null && createTicketButton.getScene() != null
                ? createTicketButton.getScene().getWindow()
                : null;
    }

    private boolean isStatus(TicketView ticket, String status) {
        return ticket.getStatus() != null && ticket.getStatus().equalsIgnoreCase(status);
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

    private String buildActivityMessage(TicketView ticket) {
        if (isUnassigned(ticket)) {
            return "\"" + ticket.getTitle() + "\" is waiting for assignment";
        }

        if (isStatus(ticket, TicketStatus.COMPLETED.name())) {
            return "\"" + ticket.getTitle() + "\" is waiting for review";
        }

        if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
            return "\"" + ticket.getTitle() + "\" has been resolved";
        }

        if (isStatus(ticket, TicketStatus.IN_PROGRESS.name())) {
            return "\"" + ticket.getTitle() + "\" is still in progress";
        }

        return "\"" + ticket.getTitle() + "\" has status " + Formatter.trimOrNA(ticket.getStatus());
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