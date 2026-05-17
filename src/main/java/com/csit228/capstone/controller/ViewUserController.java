package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.dao.UserJobDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Job;
import com.csit228.capstone.model.Role;
import com.csit228.capstone.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;


public class ViewUserController {
    public Label userLabel;
    public Label departmentLabel;
    public Label jobLabel;
    public Label positionLabel;
    public ComboBox<Role> comboboxPosition;
    public ComboBox<Department> comboboxDepartment;
    public ComboBox<Job> comboboxJob;

    private ObservableList<Role> roles = FXCollections.observableArrayList();
    private ObservableList<Department> departments = FXCollections.observableArrayList();
    private ObservableList<Job> jobs = FXCollections.observableArrayList();

    private UserDAO userDAO = UserDAO.getUserDAO();
    private UserJobDAO  userJobDAO = UserJobDAO.getUserJobDao();
    private DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private ManageUserExecutiveController controller;
    private User user;
    private Department department;



    @FXML
    public void initialize(){
        comboboxPosition.setItems(roles);
        comboboxDepartment.setItems(departments);
        comboboxJob.setItems(jobs);
        setComboBoxes();
    }

    private void setComboBoxes() {
        roles.add(Role.MEMBER);
        roles.add(Role.EDITOR);
        roles.add(Role.EXECUTIVE);

        departments.clear();
        departments.addAll(departmentDAO.getDepartments());
        updateJobCombobox();
    }

    public void setController(ManageUserExecutiveController controller) {
        this.controller = controller;
    }

    public void setDepartment(Department department) {
        this.department = department;
        departmentLabel.setText(department.getName());
        comboboxDepartment.setPromptText(department.getName());
    }

    public void setUser(User user) {
        this.user = user;
        userLabel.setText(user.getFullName());
        jobLabel.setText(userJobDAO.getJobByUser(user.getUsername()));
        positionLabel.setText(user.getRole() == Role.MEMBER ? "Member" : "Editor");
        comboboxPosition.setPromptText(user.getRole() == Role.MEMBER ? "MEMBER" : "EDITOR");
        comboboxJob.setPromptText(userJobDAO.getJobByUser(user.getUsername()));
    }

    public void updateJobCombobox() {
        Department selected = comboboxDepartment.getValue();
        if(selected != null){
            jobs.clear();
            jobs.addAll(selected.getJobs());
        }
    }

    public void handleSaveChanges() {
        System.out.println("Saving");
        Role role = comboboxPosition != null ? comboboxPosition.getValue() : user.getRole();
        Department department = comboboxDepartment != null ? comboboxDepartment.getValue() : departmentDAO.getDepartmentByID(user.getDepartment_id());
        String job = comboboxJob != null ? comboboxJob.getValue().getName() : userJobDAO.getJobByUser(user.getUsername());

        userDAO.updateUser(user, role.ordinal(), department.getId(), job);

    }

    public void handleCancel() {
        System.out.println("Canceling");
    }
}
