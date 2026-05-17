package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.JobDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.dao.UserJobDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Job;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.Controls;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class ManageUserExecutiveController extends BaseTicketController {
    @FXML protected ScrollPane scrollpaneDepartment;
    @FXML protected ScrollPane scrollpaneUsers;
    @FXML protected Button createUserAccount;


    @FXML protected HBox hboxDashboard;

    DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    JobDAO jobDAO = JobDAO.getJobDAO();
    UserDAO userDAO = UserDAO.getUserDAO();
    UserJobDAO userJobDAO = UserJobDAO.getUserJobDao();


    @FXML
    public void initialize(){
        setupProfile();
        renderDepartment();
    }

    public void refreshDepartment(){
        System.out.println("Refreshing");

    }

    public static void showInfo(Department d){
        System.out.println(d.getId());
        System.out.println(d.getName());
        System.out.println(d.getDescription());
    }

    public void renderDepartment(){
        scrollpaneDepartment.setContent(null);
        HBox container = new HBox();
        Button allUsers = new Button("All Users");
        allUsers.setPrefHeight(25);
        HBox.setMargin(allUsers, new Insets(5));
        allUsers.setPadding(new Insets(10));
        allUsers.setStyle("-fx-background-color: #444444; -fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-text-fill: #FFFFFF; -fx-background-radius: 20;");
        allUsers.setOnMousePressed(mouseEvent -> {
            System.out.println("All users is pressed");
        });
        for(Department d: departmentDAO.getDepartments()){
            Button button = new Button(d.getName());
            button.setPrefHeight(25);
            HBox.setMargin(button, new Insets(5));
            button.setPadding(new Insets(10));
            button.setStyle("-fx-background-color: #444444; -fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-text-fill: #FFFFFF; -fx-background-radius: 20;");
            button.setOnMousePressed(mouseEvent -> {
                createUserRowView(d);
                System.out.println(d.getName() + " is pressed");
            });

            container.getChildren().add(button);
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

    @Override
    @FXML
    public void onClickedLogout() throws IOException {
        super.onClickedLogout();
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

    public void handleCreateUserAccount(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/csit228/capstone/view/CreateDepartmentModal.fxml"));
            Parent root = loader.load();


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



    public void createUserRowView(Department department) {
        scrollpaneUsers.setContent(null);

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.prefWidthProperty().bind(scrollpaneUsers.widthProperty().subtract(25));

        int count = 0;
        for(User u : userDAO.getUserByDepartment(department.getId())){

            HBox row = new HBox();
            row.setPrefWidth(850);
            row.setMinHeight(60);
            row.setSpacing(10);
            row.setPadding(new Insets(10, 15, 10, 15));
            row.setAlignment(Pos.CENTER_LEFT);


            Label name = new Label(u.getFullName());
            name.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-text-fill: #666666;");
            name.setPadding(new Insets(5));
            name.setPrefWidth(200);

            Label job = new Label(userJobDAO.getJobByUser(u.getUsername()));
            job.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 10px; -fx-text-fill: #666666;");
            job.setPadding(new Insets(5));
            job.setPrefWidth(150);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button ViewMembers = new Button("Save");
            ViewMembers.setStyle(
                    "-fx-background-radius: 50; " +
                            "-fx-background-color: #333333; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;"
            );

            row.setOnMouseClicked(mouseEvent -> {
                System.out.println(u.getFirstName() + " is clicked");
                handleViewUser(u, department);
            });


            row.getChildren().addAll(name, job, spacer, ViewMembers);
            addCardHoverEffect(row);
            container.getChildren().add(row);

        }

        scrollpaneUsers.setContent(container);
        scrollpaneUsers.setFitToWidth(true);
    }

    private void addCardHoverEffect(Node node) {
        String normalStyle = node.getStyle();
        node.setOnMouseEntered(event -> node.setStyle("-fx-background-color: #f8faff; -fx-border-color: #d7e4fb; -fx-border-radius: 12; -fx-background-radius: 12;"));
        node.setOnMouseExited(event -> node.setStyle(normalStyle));
    }

    public void handleViewUser(User user, Department department){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/csit228/capstone/view/ViewUser.fxml"));
            Parent root = loader.load();

            ViewUserController controller = loader.getController();
            controller.setDepartment(department);
            controller.setController(this);
            controller.setUser(user);
            System.out.println("Setting " + user.getFullName());

            Stage modalStage = new Stage();
            modalStage.setTitle(department.getName());
            modalStage.setScene(new Scene(root));
            modalStage.sizeToScene();
            modalStage.centerOnScreen();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();



        } catch (IOException e) {
            System.out.println("Unable to open View Department modal.");
            e.printStackTrace();
        }
    }

    @Override
    protected String getDefaultRoleName() {
        return "EXECUTIVE";
    }

    @Override
    protected void refreshDashboard() {
        renderDepartment();
    }

    @Override
    protected void renderDashboard() {
        renderDepartment();
    }


}
