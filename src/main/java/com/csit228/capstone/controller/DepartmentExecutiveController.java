package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.JobDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Job;
import com.csit228.capstone.utils.Controls;
import com.csit228.capstone.utils.ListRowItem;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class DepartmentExecutiveController {
    @FXML protected ScrollPane scrollpaneDepartment;
    @FXML protected Button createDepartmentButton;
    @FXML protected HBox hboxDepartment;

    @FXML protected HBox hboxDashboard;

    DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    JobDAO jobDAO = JobDAO.getJobDAO();

    public DepartmentExecutiveController getDepartmentExecutiveController(){
        return this;
    }

    @FXML
    public void initialize(){
        createDepartmentRowView();
        renderDepartment();
    }

    public void refreshDepartment(){
        System.out.println("Refreshing");
        createDepartmentRowView();
    }

    public static void showInfo(Department d){
        System.out.println(d.getId());
        System.out.println(d.getName());
        System.out.println(d.getDescription());
    }

    public void renderDepartment(){
        scrollpaneDepartment.setContent(null);
        VBox container = new VBox();
        for(Department d: departmentDAO.getDepartments()){
            ListRowItem item = ListRowItem.forDepartment(d, 10);

            item.setOnMousePressed(mouseEvent -> {
                System.out.println(d.getName() + "is pressed");
                handleViewDepartment(d);
            });

            container.getChildren().add(item);
            System.out.println("adding " + d.getName());
        }
        scrollpaneDepartment.setContent(container);
    }


    public void goToDashboard() throws IOException {
        Controls.switchScreen("DashboardExecutiveView.fxml");
    }

    public void goToDepartment() throws IOException {
        Controls.switchScreen("DepartmentExecutiveView.fxml");
    }

    public void goToManageUsers() throws IOException {
        Controls.switchScreen("ManageUserExecutive.fxml");
    }

    public void onClickedLogout(){
        System.out.println("logout");
    }

    public void deadlineSortComboBox(){
        System.out.println("deadline Sort");
    }

    public void  onDeadlineSortChanged(){
        System.out.println("deadline Sort");
    }

    public void handleViewDepartment(Department department){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/csit228/capstone/view/ViewDepartment.fxml"));
            Parent root = loader.load();

            ViewDepartmentModalController controller = loader.getController();
            controller.setDepartment(department);
            controller.setParentController(this);
            System.out.println("Setting " + department.getName());

            Stage modalStage = new Stage();
            modalStage.setTitle(department.getName());
            modalStage.setScene(new Scene(root));
            modalStage.sizeToScene();
            modalStage.centerOnScreen();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();


            controller.getButtonSaveChanges().setOnAction(event -> {
                renderDepartment();
            });

        } catch (IOException e) {
            System.out.println("Unable to open View Department modal.");
            e.printStackTrace();
        }
    }

    public void handleCreateDepartment(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/csit228/capstone/view/CreateDepartmentModal.fxml"));
            Parent root = loader.load();

            openModal(root, "Create New Department");

            CreateDepartmentModalController controller = loader.getController();

            controller.getSubmitButton().setOnAction(event -> {
                renderDepartment();
            });

            if (controller != null && controller.isSubmitted()) {
                renderDepartment();
            }

        } catch (IOException e) {
            System.out.println("Unable to open Create Department modal.");
            e.printStackTrace();
        }
    }

    protected void openModal(Parent root, String title) {
        Window ownerWindow = createDepartmentButton != null && createDepartmentButton.getScene() != null
                ? createDepartmentButton.getScene().getWindow() : null;

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

    public void createDepartmentRowView() {
        scrollpaneDepartment.setContent(null);

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.prefWidthProperty().bind(scrollpaneDepartment.widthProperty().subtract(25));

        int count = 0;
        for(Department d : departmentDAO.getDepartments()){

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 10, 5, 10));

            Label title = new Label(d.getName());
            title.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 20px;");

            Label description = new Label(d.getName());
            title.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button ViewMembers = new Button("ViewMembers");
            ViewMembers.setMinSize(30, 30);
            ViewMembers.setMaxSize(30, 30);
            ViewMembers.setStyle(
                    "-fx-background-radius: 50; " +
                            "-fx-background-color: #333333; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;"
            );


            row.getChildren().addAll(title, description, spacer, ViewMembers);
            container.getChildren().add(row);
            Separator line = new Separator();
            if (count < departmentDAO.getDepartments().size()) {

                line.setPadding(new Insets(5, 0, 5, 0));
                container.getChildren().add(line);
                count++;
            }


        }

        scrollpaneDepartment.setContent(container);
        scrollpaneDepartment.setFitToWidth(true);
    }




}
