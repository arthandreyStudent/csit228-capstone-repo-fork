package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.Ticket;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.ListRowItem;
import com.csit228.capstone.utils.NotificationManager;
import com.csit228.capstone.utils.TicketDeadlineComparator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;



import java.util.ArrayList;
import java.util.List;

public class TicketMemberController extends BaseTicketController {

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

    @FXML
    private HBox dashboardMenuItem;

    @FXML
    private HBox myTasksMenuItem;

    @FXML
    private Label myTasksSidebarCountLabel;

    @FXML
    private AnchorPane myTasksPage;

    @FXML
    private VBox myTasksBox;

    @FXML
    private Label myTasksCountLabel;

    @FXML
    private TextField myTasksSearchField;

    @FXML
    private ComboBox<String> myTasksDeadlineSortComboBox;

    @FXML
    private Button myTasksInProgressButton;

    @FXML
    private Button myTasksCompletedButton;

    @FXML
    private Button myTasksResolvedButton;

    private final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();

    private boolean showingMyTasks = false;
    private String currentMyTasksFilter = TicketStatus.IN_PROGRESS.name();

    private final String activeMenuStyle = "-fx-background-color: #5b1617; -fx-background-radius: 10;";
    private final String inactiveMenuStyle = "-fx-background-color: transparent;";

    private final String activeTabStyle = "-fx-background-color: #5d0808;" +
                                          " -fx-background-radius: 18; -fx-text-fill: white; " +
                                          "-fx-font-family: 'Georgia'; " +
                                          "-fx-font-size: 12px; " +
                                          "-fx-font-weight: bold;";
    
    private final String inactiveTabStyle = "-fx-background-color: white;" +
                                            " -fx-border-color: #dfe7f5; -fx-border-radius: 18; " +
                                            "-fx-background-radius: 18; -fx-text-fill: #75706b;" +
                                            "-fx-font-family: 'Georgia'; " +
                                            "-fx-font-size: 12px; -fx-font-weight: bold;";

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

        if (showingMyTasks) {
            loadMyTasks();
        } else {
            loadAvailableTickets();
            loadVolunteerBoard();
            loadRecentActivity(activityBox);
        }
    }

    @Override
    protected void onSearchChanged() {
        if (showingMyTasks) {
            loadMyTasks();
        } else {
            loadAvailableTickets();
            loadVolunteerBoard();
        }
    }

    @Override
    protected void onDeadlineSortSelected() {
        if (showingMyTasks) {
            loadMyTasks();
        } else {
            loadAvailableTickets();
            loadVolunteerBoard();
        }
    }

    @FXML
    public void initialize() {
        setupProfile();
        setupSearch();
        setupDeadlineSortComboBox();
        setupMyTasksControls();
        refreshDashboard();
        startWatching();
    }

    private void setupMyTasksControls() {
        hideMyTasksPage();

        if (myTasksSearchField != null) {
            myTasksSearchField.textProperty().addListener((obs, oldValue, newValue) -> loadMyTasks());
        }

        if (myTasksDeadlineSortComboBox != null) {
            myTasksDeadlineSortComboBox.getItems().setAll("Nearest Deadline", "Farthest Deadline");
            myTasksDeadlineSortComboBox.setValue("Nearest Deadline");
        }

        updateMyTasksTabButtons();
    }

    private void updateSummaryCards() {
        int openTasks = 0;
        int inProgress = 0;
        int completed = 0;
        int overdue = 0;
        int myTasksCount = 0;

        int userDeptId = AppSession.currentUser != null ? AppSession.currentUser.getDepartment_id() : -1;

        for (TicketView ticket : tickets) {
            if (isAvailableTicket(ticket) && isFromUserDepartment(ticket, userDeptId)) {
                openTasks++;
            }

            if (isAssignedToCurrentUser(ticket)) {
                myTasksCount++;

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
        }

        openTasksLabel.setText(String.valueOf(openTasks));
        inProgressLabel.setText(String.valueOf(inProgress));
        completedLabel.setText(String.valueOf(completed));
        overdueLabel.setText(String.valueOf(overdue));

        if (myTasksSidebarCountLabel != null) {
            myTasksSidebarCountLabel.setText(String.valueOf(myTasksCount));
        }
    }

    private void loadAvailableTickets() {
        if (availableTicketsBox == null) {
            return;
        }

        availableTicketsBox.getChildren().clear();

        String keyword = searchField != null ? searchField.getText() : "";
        int userDeptId = AppSession.currentUser != null ? AppSession.currentUser.getDepartment_id() : -1;

        for (TicketView ticket : getSortedTicketsByDeadline()) {
            if (!isAvailableTicket(ticket) || ticket.isVolunteerTicket()) {
                continue;
            }

            if (!isFromUserDepartment(ticket, userDeptId)) {
                continue;
            }

            if (!matchesTicketSearch(ticket, keyword)) {
                continue;
            }

            ListRowItem row = ListRowItem.forMemberAvailableTicket(ticket);

            row.setRowClick(event -> openMasterTicketDetail(ticket));

            row.setAction(event -> {
                event.consume();
                takeTicket(ticket);
            });

            availableTicketsBox.getChildren().add(row);
        }
    }

    private void loadVolunteerBoard() {
        if (volunteerBoardBox == null) {
            return;
        }

        volunteerBoardBox.getChildren().clear();

        String keyword = searchField != null ? searchField.getText() : "";
        int count = 0;

        for (TicketView ticket : getSortedTicketsByDeadline()) {
            if (!isAvailableTicket(ticket) || !ticket.isVolunteerTicket()) {
                continue;
            }

            if (!matchesTicketSearch(ticket, keyword)) {
                continue;
            }

            ListRowItem row = ListRowItem.forMemberVolunteerTicket(ticket);

            row.setRowClick(event -> openMasterTicketDetail(ticket));

            row.setAction(event -> {
                event.consume();
                takeTicket(ticket);
            });

            volunteerBoardBox.getChildren().add(row);

            if (++count >= 8) {
                break;
            }
        }
    }

    private void loadMyTasks() {
        if (myTasksBox == null) {
            return;
        }

        myTasksBox.getChildren().clear();

        String keyword = myTasksSearchField != null ? myTasksSearchField.getText() : "";
        int count = 0;

        for (TicketView ticket : getSortedMyTasksByDeadline()) {
            if (!isAssignedToCurrentUser(ticket)) {
                continue;
            }

            if (!isStatus(ticket, currentMyTasksFilter)) {
                continue;
            }

            if (!matchesTicketSearch(ticket, keyword)) {
                continue;
            }

            ListRowItem row = ListRowItem.forMemberTaskTicket(ticket);

            row.setRowClick(event -> openMemberTicketView(ticket));

            myTasksBox.getChildren().add(row);
            count++;
        }

        if (myTasksCountLabel != null) {
            myTasksCountLabel.setText(String.valueOf(count));
        }

        if (count == 0) {
            Label emptyLabel = new Label("No tickets found for this tab.");
            emptyLabel.setStyle("" +
                                "-fx-text-fill: #918683; " +
                                "-fx-font-family: 'Georgia'; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 13px; " +
                                "-fx-padding: 20 0 0 0;");
            myTasksBox.getChildren().add(emptyLabel);
        }
    }

    private List<TicketView> getSortedMyTasksByDeadline() {
        List<TicketView> sorted = new ArrayList<>(tickets);

        TicketDeadlineComparator.SortMode mode = TicketDeadlineComparator.getSortModeFromText(
                myTasksDeadlineSortComboBox != null ? myTasksDeadlineSortComboBox.getValue() : null
        );

        sorted.sort(new TicketDeadlineComparator(mode));
        return sorted;
    }
    
    protected void takeTicket(TicketView ticket) {
        User currentUser = AppSession.currentUser;

        if (currentUser == null) {
            showError("No logged-in user found.");
            return;
        }

        boolean assigned = ticketDAO.assignTicket(currentUser.getUserId(), ticket.getId());
        boolean updated = ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

        if (assigned && updated) {
            NotificationManager.notifyCreator(ticket, currentUser.getFullName());
            showInfo("Ticket added to your tasks.");
            refreshDashboard();
        } else {
            showError("Unable to take ticket.");
        }
    }

    private void markTaskAsCompleted(TicketView ticket) {
        if (ticket == null) {
            showError("No ticket selected.");
            return;
        }

        if (!isAssignedToCurrentUser(ticket)) {
            showError("You can only update tickets assigned to you.");
            return;
        }

        boolean updated = ticketDAO.updateStatus(ticket.getId(), TicketStatus.COMPLETED);

        if (updated) {
            showInfo("Task marked as completed and sent for review.");
            refreshDashboard();
        } else {
            showError("Unable to complete task.");
        }
    }

    private void openMasterTicketDetail(TicketView ticket) {
        if (ticket == null) {
            showError("No ticket selected.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/csit228/capstone/view/BaseTicketDetailModalView.fxml")
            );

            Parent root = loader.load();

            TicketDetailModelController controller = loader.getController();

            if (controller != null) {
                controller.loadTicket(ticket);
                controller.setParentController(this);
            }

            openMemberModal(root, "Ticket Details");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Unable to open ticket details.");
        }
    }

    private void openMemberTicketView(TicketView ticket) {
        if (ticket == null) {
            showError("No ticket selected.");
            return;
        }

        try {
            String fxmlPath = "/com/csit228/capstone/view/StaffTicketView.fxml";

            var resource = getClass().getResource(fxmlPath);

            if (resource == null) {
                showError("FXML file not found: " + fxmlPath);
                System.out.println("FXML file not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);

            Parent root = loader.load();

            TicketDetailModelController controller = loader.getController();

            if (controller != null) {
                controller.loadTicketForMember(ticket, this::refreshDashboard);
            }

            openMemberModal(root, "My Task Details");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Unable to open task details.");
        }
    }

    private void openMemberModal(Parent root, String title) {
        Window ownerWindow = null;

        if (availableTicketsBox != null && availableTicketsBox.getScene() != null) {
            ownerWindow = availableTicketsBox.getScene().getWindow();
        } else if (myTasksBox != null && myTasksBox.getScene() != null) {
            ownerWindow = myTasksBox.getScene().getWindow();
        }

        Stage modalStage = new Stage();
        modalStage.setTitle(title);

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
    }

    protected boolean isAvailableTicket(TicketView ticket) {
        if (ticket == null || !isUnassigned(ticket)) {
            return false;
        }

        return isStatus(ticket, TicketStatus.OPEN.name());
    }

    protected boolean isAssignedToCurrentUser(TicketView ticket) {
        User currentUser = AppSession.currentUser;

        if (currentUser == null || ticket == null) {
            return false;
        }

        String assignedToName = normalizeName(ticket.getAssignedToName());
        String currentUserName = normalizeName(currentUser.getFullName());

        return assignedToName.equals(currentUserName);
    }

    private boolean isFromUserDepartment(TicketView ticket, int userDeptId) {
        if (userDeptId <= 0) {
            return true;
        }

        String userDeptName = departmentDAO.getDepartmentByID(userDeptId) != null
                ? departmentDAO.getDepartmentByID(userDeptId).getName()
                : null;

        if (userDeptName == null) {
            return true;
        }

        return userDeptName.equalsIgnoreCase(ticket.getDepartmentName());
    }

    @FXML
    public void onClickedMyTasks() {
        showDashboardContent();

        showingMyTasks = true;

        if (myTasksPage != null) {
            myTasksPage.setVisible(true);
            myTasksPage.setManaged(true);
            myTasksPage.toFront();
        }

        setSidebarActive(false);
        currentMyTasksFilter = TicketStatus.IN_PROGRESS.name();
        updateMyTasksTabButtons();
        loadMyTasks();
    }

    @FXML
    public void onClickedDashboard() {
        showDashboardContent();

        showingMyTasks = false;
        hideMyTasksPage();
        setSidebarActive(true);
        renderDashboard();
    }

    private void hideMyTasksPage() {
        if (myTasksPage != null) {
            myTasksPage.setVisible(false);
            myTasksPage.setManaged(false);
        }
    }

    private void setSidebarActive(boolean dashboardActive) {
        if (dashboardMenuItem != null) {
            dashboardMenuItem.setStyle(dashboardActive ? activeMenuStyle : inactiveMenuStyle);
        }

        if (myTasksMenuItem != null) {
            myTasksMenuItem.setStyle(dashboardActive ? inactiveMenuStyle : activeMenuStyle);
        }
    }

    @FXML
    public void showMyTasksInProgress() {
        currentMyTasksFilter = TicketStatus.IN_PROGRESS.name();
        updateMyTasksTabButtons();
        loadMyTasks();
    }

    @FXML
    public void showMyTasksCompleted() {
        currentMyTasksFilter = TicketStatus.COMPLETED.name();
        updateMyTasksTabButtons();
        loadMyTasks();
    }

    @FXML
    public void showMyTasksResolved() {
        currentMyTasksFilter = TicketStatus.RESOLVED.name();
        updateMyTasksTabButtons();
        loadMyTasks();
    }

    @FXML
    public void onMyTasksDeadlineSortChanged() {
        loadMyTasks();
    }

    private void updateMyTasksTabButtons() {
        if (myTasksInProgressButton != null) {
            myTasksInProgressButton.setStyle(
                    currentMyTasksFilter.equals(TicketStatus.IN_PROGRESS.name()) ? activeTabStyle : inactiveTabStyle
            );
        }

        if (myTasksCompletedButton != null) {
            myTasksCompletedButton.setStyle(
                    currentMyTasksFilter.equals(TicketStatus.COMPLETED.name()) ? activeTabStyle : inactiveTabStyle
            );
        }

        if (myTasksResolvedButton != null) {
            myTasksResolvedButton.setStyle(
                    currentMyTasksFilter.equals(TicketStatus.RESOLVED.name()) ? activeTabStyle : inactiveTabStyle
            );
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        return name.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }
}