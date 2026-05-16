package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Role;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.Formatter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.net.URL;
import java.util.ResourceBundle;

public class ProfileViewController implements Initializable {

    @FXML private Label labelInitials;
    @FXML private Label labelFullName;
    @FXML private Label labelRoleBadge;

    @FXML private Label labelFirstName;
    @FXML private Label labelLastName;
    @FXML private Label labelUsername;
    @FXML private Label labelRole;
    @FXML private VBox basicInfoCard;
    @FXML private VBox organizationCard;

    @FXML private HBox departmentRow;
    @FXML private Label labelDepartment;

    @FXML private HBox jobRow;
    @FXML private Label labelJob;

    private Runnable backAction;

    private final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private final UserDAO userDAO = UserDAO.getUserDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadProfile();
    }

    private void loadProfile() {
        User user = AppSession.currentUser;

        if (user == null) {
            labelInitials.setText("NA");
            labelFullName.setText("No user logged in");
            labelRoleBadge.setText("N/A");

            labelFirstName.setText("N/A");
            labelLastName.setText("N/A");
            labelUsername.setText("N/A");
            labelRole.setText("N/A");

            hideRow(departmentRow);
            hideRow(jobRow);
            return;
        }

        Role role = user.getRole();

        labelInitials.setText(Formatter.getInitials(user));
        labelFullName.setText(user.getFullName());
        labelRoleBadge.setText(role != null ? role.name() : "N/A");

        labelFirstName.setText(Formatter.trimOrNA(user.getFirstName()));
        labelLastName.setText(Formatter.trimOrNA(user.getLastName()));
        labelUsername.setText(Formatter.trimOrNA(user.getUsername()));
        labelRole.setText(role != null ? role.name() : "N/A");

        if (role == Role.EXECUTIVE) {
            hideCard(organizationCard);

            if (basicInfoCard != null) {
                basicInfoCard.setPrefWidth(1185.0);
            }

            return;
        }

        showCard(organizationCard);

        if (basicInfoCard != null) {
            basicInfoCard.setPrefWidth(585.0);
        }

        showRow(departmentRow);
        labelDepartment.setText(getDepartmentName(user));

        if (role == Role.EDITOR) {
            hideRow(jobRow);
            return;
        }

        if (role == Role.MEMBER) {
            showRow(jobRow);
            labelJob.setText(Formatter.trimOrNA(userDAO.getJobNameByUserId(user.getUserId())));
        } else {
            hideRow(jobRow);
        }
    }

    private void showCard(VBox card) {
        if (card != null) {
            card.setVisible(true);
            card.setManaged(true);
        }
    }

    private void hideCard(VBox card) {
        if (card != null) {
            card.setVisible(false);
            card.setManaged(false);
        }
    }

    private String getDepartmentName(User user) {
        Department department = departmentDAO.getDepartmentByID(user.getDepartment_id());

        if (department == null) {
            return "N/A";
        }

        return Formatter.formatDepartmentName(department.getName());
    }

    private void showRow(HBox row) {
        if (row != null) {
            row.setVisible(true);
            row.setManaged(true);
        }
    }

    private void hideRow(HBox row) {
        if (row != null) {
            row.setVisible(false);
            row.setManaged(false);
        }
    }



    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    @FXML
    public void onClickedBack() {
        if (backAction != null) {
            backAction.run();
        }
    }
}