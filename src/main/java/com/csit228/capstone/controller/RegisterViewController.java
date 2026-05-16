package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.JobDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.exceptions.UsernameAlreadyTakenException;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Job;
import com.csit228.capstone.enums.Role;
import com.csit228.capstone.model.UserFactory;
import com.csit228.capstone.utils.Controls;
import com.csit228.capstone.utils.Hash;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterViewController implements Initializable {

    private UserDAO userDAO = UserDAO.getUserDAO();
    private DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private JobDAO jobDAO = JobDAO.getJobDAO();

    public TextField tfFirstname;
    public TextField tfLastname;
    public TextField tfUsername;
    public TextField tfPassword;
    public TextField tfConfirmPassword;
    public Label lbError;
    public ComboBox<Job> cbJob;
    public ComboBox<Department> cbDepartment;

    private Department selectedDepartment = null;

    private ObservableList<Department> departments = FXCollections.observableArrayList();
    private ObservableList<Job> jobs = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbDepartment.setItems(departments);
        cbJob.setItems(jobs);
        refreshData();
    }

    public void refreshData() {
        departments.clear();
        jobs.clear();
        departments.addAll(departmentDAO.getDepartments());
    }

    public void goToLogin() throws IOException {
        Controls.switchScreen("LoginView.fxml");
    }

    public void createAccount() throws IOException {
        String firstname = tfFirstname.getText();
        String lastname = tfLastname.getText();
        String username = tfUsername.getText();
        String password = tfPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();
        int department_ID = cbDepartment.getValue().getId();
        String job = cbJob.getValue().getName();

        if (firstname.isBlank() || lastname.isBlank() || username.isBlank() || password.isBlank() ||
                confirmPassword.isBlank() || job.isBlank()) {
            showError("Please fill in all fields!");
        } else if (confirmPassword.equals(password)) {
            closeError();
            try {
                userDAO.createUser(
                        UserFactory.createUser(Role.MEMBER, 1, firstname, lastname, username, Hash.hashWithSHA256(confirmPassword),
                                department_ID));
            } catch (UsernameAlreadyTakenException e) {
                showError("Username already exists.");
            }
            Controls.switchScreen("LoginView.fxml");
            System.out.println("User: " + username + " created");
        } else {
            showError("Passwords don't match.");
        }
    }

    public void showError(String error) {
        lbError.setText(error);
        lbError.setVisible(true);
        lbError.setManaged(true);
    }

    public void closeError() {
        lbError.setVisible(false);
        lbError.setText("");
    }

    public void updatePositionComboBox() {
        selectedDepartment = cbDepartment.getValue();

        if (selectedDepartment != null) {
            jobs.clear();
            jobDAO.getJobByDepartment(selectedDepartment);
            jobs.addAll(selectedDepartment.getJobs());

            cbJob.getSelectionModel().clearSelection();
        }
    }
}
