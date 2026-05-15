package com.csit228.capstone.controller;

import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.TicketView;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.Formatter;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DashboardEditorController extends StaffDashboardController {

    @FXML private Label reviewQueueCountLabel;
    @FXML private Label awaitingReviewLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label approvedTodayLabel;
    @FXML private Label sentBackLabel;
    @FXML private Label reviewApprovalPercentLabel;
    @FXML private Label approvedStatLabel;
    @FXML private Label sentBackStatLabel;
    @FXML private Label editedStatLabel;
    @FXML private Label totalReviewsStatLabel;
    @FXML private VBox reviewQueueBox;
    @FXML private VBox recentActivityBox;
    @FXML private Button allFilterButton;
    @FXML private Button openFilterButton;
    @FXML private Button inProgressFilterButton;
    @FXML private Button completedFilterButton;
    @FXML private Button resolvedFilterButton;

    private ReviewQueueFilter currentFilter = ReviewQueueFilter.ALL;

    private enum ReviewQueueFilter { ALL, OPEN, IN_PROGRESS, TO_BE_REVIEWED, RESOLVED }

    @Override
    protected String getDefaultRoleName() {
        return "EDITOR";
    }

    @Override
    protected void refreshDashboard() {
        ticketDAO.getTicketViews();
        tickets = new ArrayList<>(ticketDAO.getViews());

        renderDashboard();
    }

    @Override
    protected void renderDashboard() {
        updateSummaryCardsAndReviewStats();
        loadReviewQueue();
        loadRecentActivity(recentActivityBox);
    }

    @Override
    protected void onSearchChanged() {
        loadReviewQueue();
    }

    @Override
    protected void onDeadlineSortSelected() {
        loadReviewQueue();
    }

    @FXML
    public void initialize() {
        setupProfile();
        setupSearch();
        setupFilterButtons();
        setupDeadlineSortComboBox();
        loadDepartments();
        refreshDashboard();
        startWatching();
    }

    private void setupFilterButtons() {
        setActiveFilterButton(allFilterButton);
    }

    @FXML public void showAllTickets()        { currentFilter = ReviewQueueFilter.ALL;           setActiveFilterButton(allFilterButton);        loadReviewQueue(); }
    @FXML public void showOpenTickets()       { currentFilter = ReviewQueueFilter.OPEN;          setActiveFilterButton(openFilterButton);       loadReviewQueue(); }
    @FXML public void showInProgressTickets() { currentFilter = ReviewQueueFilter.IN_PROGRESS;   setActiveFilterButton(inProgressFilterButton); loadReviewQueue(); }
    @FXML public void showCompletedTickets()  { currentFilter = ReviewQueueFilter.TO_BE_REVIEWED; setActiveFilterButton(completedFilterButton);  loadReviewQueue(); }
    @FXML public void showResolvedTickets()   { currentFilter = ReviewQueueFilter.RESOLVED;       setActiveFilterButton(resolvedFilterButton);   loadReviewQueue(); }

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
        if (button == null) return;
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

    private void updateSummaryCardsAndReviewStats() {
        int inProgress = 0, toBeReviewed = 0, resolved = 0, sentBack = 0, edited = 0, totalReviews = 0;

        for (TicketView ticket : tickets) {
            if (isStatus(ticket, TicketStatus.IN_PROGRESS.name())) { inProgress++; edited++; }
            if (isStatus(ticket, TicketStatus.COMPLETED.name()))   { toBeReviewed++; totalReviews++; }
            if (isStatus(ticket, TicketStatus.RESOLVED.name()))    { resolved++; totalReviews++; }
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

        for (TicketView ticket : getSortedTicketsByDeadline()) {
            if (isVolunteerTicket(ticket) && isUnassigned(ticket)) continue;
            if (!matchesCurrentFilter(ticket)) continue;
            if (!matchesTicketSearch(ticket, keyword)) continue;

            List<User> assignableUsers = getAssignableMembersForTicket(ticket);
            ListRowItem row = ListRowItem.forEditorReview(ticket, assignableUsers);

            if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
                lockResolvedTicketRow(row);
                reviewQueueBox.getChildren().add(row);
                continue;
            }

            row.setSecondaryAction(event -> {
                if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
                    showInfo("This ticket is already resolved and closed.");
                    return;
                }
                assignTicketToUser(ticket, row.getSelectedAssignedUser());
            });

            if (!isStatus(ticket, TicketStatus.COMPLETED.name())) {
                hideReviewActionButtons(row);
                reviewQueueBox.getChildren().add(row);
                continue;
            }

            row.setAction(event -> updateTicketStatus(ticket, TicketStatus.RESOLVED, "Ticket marked as resolved."));
            row.setThirdAction(event -> updateTicketStatus(ticket, TicketStatus.IN_PROGRESS, "Ticket returned to in progress."));
            reviewQueueBox.getChildren().add(row);
        }

        reviewQueueCountLabel.setText(String.valueOf(reviewQueueBox.getChildren().size()));
    }

    private boolean matchesCurrentFilter(TicketView ticket) {
        if (ticket == null) return false;

        switch (currentFilter) {
            case ALL:            return true;
            case OPEN:           return isUnassigned(ticket);
            case IN_PROGRESS:    return isStatus(ticket, TicketStatus.IN_PROGRESS.name());
            case TO_BE_REVIEWED: return isStatus(ticket, TicketStatus.COMPLETED.name());
            case RESOLVED:       return isStatus(ticket, TicketStatus.RESOLVED.name());
            default:             return true;
        }
    }

    private int getFilteredTicketCount() {
        int count = 0;
        for (TicketView ticket : tickets) { if (matchesCurrentFilter(ticket)) count++; }
        return count;
    }

    private void hideReviewActionButtons(ListRowItem row) {
        if (row == null) return;
        setButtonHidden(row.getActionButton());
        setButtonHidden(row.getThirdActionButton());
    }

    private void lockResolvedTicketRow(ListRowItem row) {
        if (row == null) return;

        if (row.getAssignComboBox() != null) {
            row.getAssignComboBox().setDisable(true);
            row.getAssignComboBox().setPromptText("Closed");
        }

        if (row.getSecondaryActionButton() != null) {
            row.getSecondaryActionButton().setDisable(true);
            row.getSecondaryActionButton().setText("Closed");
        }

        setButtonHidden(row.getActionButton());
        setButtonHidden(row.getThirdActionButton());
    }

    private void setButtonHidden(Button button) {
        if (button == null) return;
        button.setDisable(true);
        button.setVisible(false);
        button.setManaged(false);
    }

    @FXML
    public void handleCreateTicket() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/csit228/capstone/view/CreateTicketModalEditorView.fxml"));
            Parent root = loader.load();
            CreateTicketModalEditorController controller = loader.getController();

            openModal(root, "Create New Ticket");

            if (controller != null && controller.isSubmitted()) {
                refreshDashboard();
            }
        } catch (IOException e) {
            showError("Unable to open Create Ticket modal.");
        }
    }
}
