package com.csit228.capstone.controller;

import com.csit228.capstone.application.TixApp;
import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.Controls;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainViewController {

    private User user = TixApp.currentUser;
    private DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private Department userDep = departmentDAO.getDepartmentByID(user.getDepartment_id());
    public Button btnLogout;
    public Label lblInitials;
    public Label lblName;
    public Label lblDepartment;

    @FXML
    public void initialize(){
        lblName.setText(user.getFirstName() + " " + user.getLastName().charAt(0));
        lblInitials.setText(user.getFirstName().toUpperCase().charAt(0)  + user.getLastName().toUpperCase().charAt(0) + "");
        lblDepartment.setText(userDep.getName());
    }

    public void Logout() throws IOException {
        Controls.switchScreen("LoginView.fxml");
        File file = new File("user.ser");
        if(file.exists()) file.delete();
    }
}
