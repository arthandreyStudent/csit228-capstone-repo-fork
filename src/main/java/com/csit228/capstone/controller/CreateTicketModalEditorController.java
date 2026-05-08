package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.Department;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class CreateTicketModalEditorController extends BaseCreateTicketModalController {

    @FXML private TextField textfieldDept;

    private final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();

    @FXML
    public void initialize() {
        setupCommonFields();
        Department dept = resolveDepartment();
        if (dept != null) textfieldDept.setText(dept.getName());
    }

    @Override
    protected Department resolveDepartment() {
        return departmentDAO.getDepartmentByID(currentUser.getDepartment_id());
    }

    @Override
    protected Integer resolveDepartmentId(Department department) {
        return department.getId();
    }
}