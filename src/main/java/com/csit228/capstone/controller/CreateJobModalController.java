package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.JobDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Job;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;


public class CreateJobModalController {
    private Department department;
    @FXML protected TextField textfieldJob;
    @FXML protected Button buttonAddJob;
    @FXML protected Button buttonCancel;
    @FXML protected Button buttonClose;
    @FXML protected ComboBox<Job> comboboxJob;
    @FXML protected Label labelDepartment;
    private ObservableList<Job> jobs = FXCollections.observableArrayList();
    private Parent oldRoot;
    private ViewDepartmentModalController parentController;

    JobDAO jobDAO = JobDAO.getJobDAO();
    DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    @FXML
    public void initialize(){
        comboboxJob.setItems(jobs);
        jobs.add(new Job(0, "Create New Job"));
        refreshData();
    }

    public void setOldRoot(Parent oldRoot) {
        this.oldRoot = oldRoot;
    }

    private void refreshData() {
        jobs.clear();
        jobs.add(new Job(0, "Create New Job"));
        jobs.addAll(jobDAO.getAllJobs());
    }

    public void onSelectChange(){
        if(comboboxJob.getValue().getName().equalsIgnoreCase("Create New Job")){
            textfieldJob.setManaged(true);
        } else {
            textfieldJob.setManaged(false);
            textfieldJob.setEditable(false);
            textfieldJob.setText("");
        }
    }

    private boolean submitted = false;

    public boolean isSubmitted(){
        return submitted;
    }

    public void onClickClose(){
        closeModal(buttonClose);
    }

    public void onClickedCancel(){
        goBackScreen();
    }

    public void onClickCreateJob() {
        String name = !textfieldJob.getText().isBlank() ? textfieldJob.getText() :
                (comboboxJob.getValue() != null ? comboboxJob.getValue().toString() : null);

        if (name != null && !name.isBlank() && !name.equalsIgnoreCase("Create New Job")) {
            Job newJob = new Job(0, name);
            jobDAO.addJobToDepartment(department, newJob);

            if (department.getJobs() != null) {
                department.getJobs().add(newJob);
            }

            goBackScreen();
        } else {
            System.err.println("Validation Failed: Job name is empty or invalid.");
        }
    }

    protected void closeModal(Button sourceButton) {
        if (sourceButton != null
                && sourceButton.getScene() != null
                && sourceButton.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    public Button getSubmitButton(){
        return buttonAddJob;
    }

    private void goBackScreen(){
        Scene currentScene = buttonCancel.getScene();
        parentController.refreshView();
        if(currentScene != null){
            currentScene.setRoot(oldRoot);
            Stage newstage = (Stage)currentScene.getWindow();
            newstage.sizeToScene();
            newstage.centerOnScreen();
        }
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setParentController(ViewDepartmentModalController viewDepartmentModalController) {
        parentController = viewDepartmentModalController;
    }
}
