package com.csit228.capstone.controller;

import com.csit228.capstone.dao.UserDAO; // Assuming you have a UserDAO
import com.csit228.capstone.dao.UserJobDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.User;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ViewMembersModalController {

    @FXML private Label departmentLabel;
    @FXML private ScrollPane scrollPaneMembers;
    @FXML private Button buttonCancel;
    @FXML private Button buttonViewDepartment;
    private Parent oldRoot;
    private ViewDepartmentModalController parentController;

    private Department department;
    private UserDAO userDAO = UserDAO.getUserDAO();
    private UserJobDAO userJobDAO = UserJobDAO.getUserJobDao();

    public void setDepartment(Department department) {
        this.department = department;
        departmentLabel.setText(department.getName());
        refreshMembersList();
    }

    private void refreshMembersList() {
        VBox container = new VBox(5);
        container.setPadding(new Insets(10));
        container.prefWidthProperty().bind(scrollPaneMembers.widthProperty().subtract(20));

        List<User> members = userDAO.getUserByDepartment(department.getId());

        if (members.isEmpty()) {
            container.getChildren().add(new Label("No members found in this department."));
        } else {
            for (int i = 0; i < members.size(); i++) {
                User user = members.get(i);

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 10, 5, 10));

                VBox nameBox = new VBox(2);
                Label nameLabel = new Label(user.getFirstName() + " " + user.getLastName());
                nameLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label roleLabel = new Label(userJobDAO.getJobByUser(user.getUsername())); // e.g., "Editor" or "Lead Designer"
                roleLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 11px; -fx-text-fill: #666666;");

                nameBox.getChildren().addAll(nameLabel, roleLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(nameBox);
                container.getChildren().add(row);

                if (i < members.size() - 1) {
                    Separator sep = new Separator();
                    sep.setOpacity(0.4);
                    container.getChildren().add(sep);
                }
            }
        }

        scrollPaneMembers.setContent(container);
        scrollPaneMembers.setFitToWidth(true);
    }

    @FXML
    public void backToView() {
        goBackScreen();
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

    @FXML
    public void onClickedCancel() {
        closeModal(buttonCancel);
    }

    private void goToUserManagement(User user) {
        System.out.println("Navigating to manage: " + user.getFirstName());
    }

    protected void closeModal(Button sourceButton) {
        if (sourceButton != null && sourceButton.getScene() != null
                && sourceButton.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    public void setOldRoot(Parent parent) {
        oldRoot = parent;
    }

    public void setParentController(ViewDepartmentModalController viewDepartmentModalController) {
        this.parentController = viewDepartmentModalController;
    }
}