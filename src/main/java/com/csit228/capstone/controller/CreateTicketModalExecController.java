package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.model.Department;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;

public class CreateTicketModalExecController extends BaseCreateTicketModalController {
  
  private static final int VOLUNTEER_DEPARTMENT_ID = 0;
  private static final String VOLUNTEER_DEPARTMENT_NAME = "Volunteer";
  
  @FXML
  private ComboBox<Department> comboBoxDepartment;
  
  @FXML
  public void initialize() {
    setupCommonFields();
    populateDepartmentComboBox();
    configureDepartmentDisplay();
  }
  
  @Override
  protected Control[] extraFieldsToValidate() {
    return new Control[] {comboBoxDepartment};
  }
  
  @Override
  protected Department resolveDepartment() {
    return comboBoxDepartment.getValue();
  }
  
  @Override
  protected Integer resolveDepartmentId(Department department) {
    return isVolunteerDepartment(department) ? null : department.getId();
  }
  
  private void populateDepartmentComboBox() {
    ObservableList<Department> departments =
      FXCollections.observableArrayList(DepartmentDAO.getDepartmentDAO().getDepartments());
    departments.add(0, new Department(VOLUNTEER_DEPARTMENT_ID, VOLUNTEER_DEPARTMENT_NAME,
                                      "Tickets available for any member to volunteer"));
    comboBoxDepartment.setItems(departments);
  }
  
  private void configureDepartmentDisplay() {
    comboBoxDepartment.setButtonCell(departmentCell());
    comboBoxDepartment.setCellFactory(lv -> departmentCell());
  }
  
  private ListCell<Department> departmentCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(Department item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName());
      }
    };
  }
  
  private boolean isVolunteerDepartment(Department department) {
    if (department == null)
      return false;
    return (department.getId() == VOLUNTEER_DEPARTMENT_ID ||
            department.getName().equalsIgnoreCase(VOLUNTEER_DEPARTMENT_NAME));
  }
}
