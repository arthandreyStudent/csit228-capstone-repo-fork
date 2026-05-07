// TO BE DELETED

//package com.csit228.capstone.controller;
//
//import com.csit228.capstone.utils.AppSession;
//import com.csit228.capstone.dao.DepartmentDAO;
//import com.csit228.capstone.model.Department;
//import com.csit228.capstone.model.User;
//import com.csit228.capstone.utils.Controls;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//
//import java.io.File;
//import java.io.IOException;
//
//public class MainViewController {
//
//    private final User user = AppSession.currentUser;
//    private final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
//    private final Department userDep = departmentDAO.getDepartmentByID(user.getDepartment_id());
//    public Button btnLogout;
//    public Label lblInitials;
//    public Label lblName;
//    public Label lblDepartment;
//
//    @FXML
//    public void initialize(){
//        lblName.setText(user.getFirstName() + " " + user.getLastName().charAt(0));
//        lblInitials.setText(user.getFirstName().toUpperCase().charAt(0)  + user.getLastName().toUpperCase().charAt(0) + "");
//        lblDepartment.setText(userDep.getName());
//    }
//
//    public void Logout() throws IOException {
//        AppSession.clearSession();  // Uses the newly implemented clear session method.
//        Controls.switchScreen("LoginView.fxml");
//        File file = new File("user.ser");
//        if (file.exists() && !file.delete()) {
//            file.deleteOnExit();
//        }
//    }
//}
