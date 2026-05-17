package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.Department;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class CreateDepartmentModalController {

    public Department department;
    @FXML protected TextField textfieldTicketTitle;
    @FXML protected TextArea textAreaTicketDesc;
    @FXML protected Button buttonCreateDepartment;
    @FXML protected Button buttonCancel;
    @FXML protected Button buttonClose;

    private boolean submitted = false;

    DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();

    public boolean isSubmitted(){
        return submitted;
    }

    public void onClickedButtonClose(){
        closeModal(buttonClose);
    }

    public void onClickedCancel(){
        closeModal(buttonCancel);
    }

    public void onClickedCreateDepartment(){
        closeModal(buttonCreateDepartment);

        if(textAreaTicketDesc.getText().isBlank() || textfieldTicketTitle.getText().isBlank()){
            System.out.println("Please fill in all fields");
        } else {
            Department department = new Department(0, textfieldTicketTitle.getText(), textAreaTicketDesc.getText());
            departmentDAO.addDepartment(department);
            submitted = true;
            closeModal(buttonCreateDepartment);
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
        return buttonCreateDepartment;
    }
}
