package com.csit228.capstone.controller;

import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.utils.Controls;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class LoginViewController {

    UserDAO userDAO = UserDAO.getUserDAO();

    public TextField tfUsername;
    public TextField tfPassword;
    public PasswordField pfPassword;
    public CheckBox cbPassword;
    public Button btnLogin;
    public Label lbCreateAccount;
    public Label lbError;

    @FXML
    public void initialize(){
        lbError.setVisible(false);
        pfPassword.setText(tfPassword.getText());
        Controls.hide(tfPassword);
        Controls.show(pfPassword);
    }


    public void login() throws IOException {
        String username = tfUsername.getText();
        String password = (cbPassword.isSelected()) ? tfPassword.getText() : pfPassword.getText();

        // gamit dao here


        if(username.isBlank() || password.isBlank()){
            showError("Please fill in all fields.");
        } else if(password.equals("Hello")){
            Controls.switchScreen("MainView.fxml");
        } else {
            showError("Username or password does not exist");
        }

    }

    public void createAccount() throws IOException {
        Controls.switchScreen("RegisterView.fxml");
    }

    public void showPassword(){
        if(cbPassword.isSelected()){
            tfPassword.setText(pfPassword.getText());
            Controls.show(tfPassword);
            Controls.hide(pfPassword);
        } else {
            pfPassword.setText(tfPassword.getText());
            Controls.hide(tfPassword);
            Controls.show(pfPassword);
        }
    }

    public void showError(String error){
        lbError.setText(error);
        lbError.setVisible(true);
    }

    public void closeError(){
        lbError.setVisible(false);
        lbError.setText("");
    }
}
