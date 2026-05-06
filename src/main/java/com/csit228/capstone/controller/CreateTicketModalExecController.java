package com.csit228.capstone.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.TicketPriority;

import java.util.Arrays;
import java.time.LocalDate;

public class CreateTicketModalExecController {

    private boolean submitted;

    @FXML private Button buttonClose;
    @FXML private Button buttonCancel;
    @FXML private Button buttonCreateTicket;
    @FXML private ComboBox<String> comboBoxDepartment;
    @FXML private ComboBox<String> comboBoxPriority;

    @FXML private TextField textfieldTicketTitle;
    @FXML private TextArea textAreaTicketDesc;
    @FXML private DatePicker datePickerDeadline;

    @FXML
    public void initialize() {
        populateDepartmentComboBox();
        populatePriorityComboBox();
    }

    private void populateDepartmentComboBox() {
        DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();

        comboBoxDepartment.getItems().setAll(
                departmentDAO.getDepartments()
                        .stream()
                        .map(Department::getName)
                        .toList()
        );
    }

    private void populatePriorityComboBox() {
        comboBoxPriority.getItems().setAll(
                Arrays.stream(TicketPriority.values())
                        .map(this::formatPriority)
                        .toList()
        );
    }

    private String formatPriority(TicketPriority priority) {
        String name = priority.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    @FXML
    public void onClickedButtonClose() {
        closeModal(buttonClose);
    }

    @FXML
    public void onClickedCancel() {
        closeModal(buttonCancel);
    }

    @FXML
    public void onClickedCreateTicket() {
        String title = textfieldTicketTitle != null ? textfieldTicketTitle.getText() : "";
        String description = textAreaTicketDesc != null ? textAreaTicketDesc.getText() : "";
        String department = comboBoxDepartment != null ? comboBoxDepartment.getValue() : null;
        String priority = comboBoxPriority != null ? comboBoxPriority.getValue() : null;
        LocalDate deadline = datePickerDeadline != null ? datePickerDeadline.getValue() : null;

        submitTicketForm(title, description, department, priority, deadline);
        closeModal(buttonCreateTicket);
    }

    public boolean isSubmitted() {
        return submitted;
    }

    private void submitTicketForm(String title, String description, String department, String priority, LocalDate deadline) {
        boolean hasValues = (title != null && !title.isBlank())
                && (description != null && !description.isBlank())
                && department != null
                && priority != null
                && deadline != null;

        if (hasValues) {
            submitted = true;
            System.out.println("Ticket form captured: " + title + " | " + department + " | " + priority + " | " + deadline);
        }
    }

    private void closeModal(Button sourceButton) {
        if (sourceButton != null
                && sourceButton.getScene() != null
                && sourceButton.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

}
