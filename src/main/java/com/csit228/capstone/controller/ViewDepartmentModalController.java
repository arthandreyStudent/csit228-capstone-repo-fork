package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.JobDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Job;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewDepartmentModalController {
    private Department department;
    @FXML private Label departmentLabel;
    @FXML private Button buttonCancel;
    @FXML private Button buttonAddJob;
    @FXML private Button buttonClose;
    @FXML private Label descriptionLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private Button buttonSaveChanges;
    private DepartmentExecutiveController departmentExecutiveController;
    private List<Job> jobsToDelete = new ArrayList<>();

    private DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private JobDAO jobDAO = JobDAO.getJobDAO();

    @FXML
    public void initialize(){
        departmentDAO.fetchDepartments();
        descriptionLabel.setWrapText(true);
    }

    public void onClickedCancel(){
        closeModal(buttonCancel);
    }

    public void setDepartment(Department department){
        this.department = department;
        departmentLabel.setText(department.getName());
        descriptionLabel.setText(department.getDescription());

        refreshView();
    }

    public void onClickedCreateJob(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/CreateJobModal.fxml"));
            Parent root = loader.load();

            CreateJobModalController controller = loader.getController();
            controller.setDepartment(department);
            controller.setOldRoot(departmentLabel.getParent());
            controller.setParentController(this);
            Scene currentScene = buttonCancel.getScene();

            if(currentScene != null){
                currentScene.setRoot(root);
                Stage newStage = (Stage) currentScene.getWindow();

                newStage.sizeToScene();
                newStage.centerOnScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteJobs(){
        if(jobsToDelete.isEmpty()) return;
        for(Job j: jobsToDelete){
            jobDAO.deleteJobFromDepartment(department, j);
            System.out.println("Successfully deleted " + j.getName());
        }
    }

    public void refreshView() {
        scrollPane.setContent(null);
        Department refreshedDepartment = departmentDAO.getDepartmentByID(department.getId());
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.prefWidthProperty().bind(scrollPane.widthProperty().subtract(25));

        int count = 0;
        for(Job j : refreshedDepartment.getJobs()){

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 10, 5, 10));

            Label title = new Label(j.getName());
            title.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button delete = new Button("X");
            delete.setMinSize(30, 30);
            delete.setMaxSize(30, 30);
            delete.setStyle(
                    "-fx-background-radius: 50; " +
                            "-fx-background-color: #333333; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;"
            );


            row.getChildren().addAll(title, spacer, delete);
            container.getChildren().add(row);
            Separator line = new Separator();
            if (count < refreshedDepartment.getJobs().size()) {

                line.setPadding(new Insets(5, 0, 5, 0));
                container.getChildren().add(line);
                count++;
            }

            delete.setOnAction(event -> {
                System.out.println("Deleting " + j.getName());
                jobsToDelete.add(j);
                container.getChildren().removeAll(row, line);
            });
        }
        department = refreshedDepartment;
        scrollPane.setContent(container);
        scrollPane.setFitToWidth(true);
    }

    public void viewTeam(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/ViewMembers.fxml"));
            Parent root = loader.load();

            ViewMembersModalController controller = loader.getController();
            controller.setDepartment(department);
            controller.setOldRoot(departmentLabel.getParent());
            controller.setParentController(this);
            Scene currentScene = buttonCancel.getScene();

            if(currentScene != null){
                currentScene.setRoot(root);
                Stage newStage = (Stage) currentScene.getWindow();

                newStage.sizeToScene();
                newStage.centerOnScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickedButtonClose(){
        departmentExecutiveController.renderDepartment();
        closeModal(buttonClose);
    }

    public void onClickedSaveChanges(){
        deleteJobs();
        departmentExecutiveController.renderDepartment();
        closeModal(buttonSaveChanges);
    }

    public Button getButtonSaveChanges() {
        return buttonSaveChanges;
    }

    protected void closeModal(Button sourceButton) {
        if (sourceButton != null
                && sourceButton.getScene() != null
                && sourceButton.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    public void setParentController(DepartmentExecutiveController departmentExecutiveController) {
        this.departmentExecutiveController = departmentExecutiveController;
    }


}

