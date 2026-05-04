package com.csit228.capstone.controller;

import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.exceptions.UsernameAlreadyTakenException;
import com.csit228.capstone.model.Role;
import com.csit228.capstone.model.User;
import com.csit228.capstone.model.UserFactory;
import com.csit228.capstone.utils.Controls;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegisterViewController {

    private UserDAO userDAO = UserDAO.getUserDAO();

    public TextField tfFirstname;
    public TextField tfLastname;
    public TextField tfUsername;
    public TextField tfPassword;
    public TextField tfConfirmPassword;
    public Label lbError;


    public void goToLogin() throws IOException {
        Controls.switchScreen("LoginView.fxml");
    }

    public void createAccount() throws IOException {
        String firstname = tfFirstname.getText();
        String lastname = tfLastname.getText();
        String username = tfUsername.getText();
        String password = tfPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();

        if(firstname.isBlank() || lastname.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank()){
            showError("Please fill in all fields!");
        } else if (confirmPassword.equals(password)){
            closeError();

            Controls.switchScreen("LoginView.fxml");
            System.out.println("User: " + username + " created");
        } else {
            showError("Passwords don't match.");
        }
    }

    public void showError(String error){
        lbError.setText(error);
        lbError.setVisible(true);
        lbError.setManaged(true);
    }

    public void closeError(){
        lbError.setVisible(false);
        lbError.setText("");
    }

}
