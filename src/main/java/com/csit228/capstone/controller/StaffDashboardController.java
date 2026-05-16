package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.model.*;
import com.csit228.capstone.utils.Formatter;
import com.csit228.capstone.utils.ListRowItem;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class StaffDashboardController extends BaseDashboardController {
  
  @FXML
  protected Button createTicketButton;
  
  protected final DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
  protected final UserDAO userDAO = UserDAO.getUserDAO();
  protected List<Department> departments = new ArrayList<>();
  
  protected void loadDepartments() {
    departments = new ArrayList<>(departmentDAO.getDepartments());
  }
  
  protected List<User> getAssignableMembersForTicket(TicketView ticket) {
    int departmentId = getDepartmentIdByName(ticket.getDepartmentName());
    
    if (departmentId > 0) {
      List<User> result = new ArrayList<>();
      for (User user : userDAO.getUserByDepartment(departmentId)) {
        if (user != null && user.hasRole(Role.MEMBER)) {
          result.add(user);
        }
      }
      return result;
    }
    
    return getAllMembers();
  }
  
  protected List<User> getAllMembers() {
    List<User> members = new ArrayList<>();
    for (Department department : departments) {
      for (User user : userDAO.getUserByDepartment(department.getId())) {
        if (user != null && user.hasRole(Role.MEMBER)) {
          members.add(user);
        }
      }
    }
    return members;
  }
  
  protected int getDepartmentIdByName(String departmentName) {
    if (departmentName == null)
      return -1;
    
    for (Department department : departments) {
      if (department.getName().equalsIgnoreCase(departmentName.trim())) {
        return department.getId();
      }
    }
    
    return -1;
  }
  
  protected void assignTicketToUser(TicketView ticket, User user) {
    if (user == null) {
      showInfo("Please select a member first.");
      return;
    }
    
    boolean assigned = ticketDAO.assignTicket(user.getUserId(), ticket.getId());
    boolean updated = ticketDAO.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
    
    if (assigned && updated) {
      showInfo("Ticket assigned to " + user.getFullName() + ".");
      refreshDashboard();
    } else {
      showError("Unable to assign ticket.");
    }
  }
  
  protected void updateTicketStatus(TicketView ticket, TicketStatus status, String successMessage) {
    if (ticket == null) {
      showError("No ticket selected.");
      return;
    }
    
    if (ticketDAO.updateStatus(ticket.getId(), status)) {
      showInfo(successMessage);
      refreshDashboard();
    } else {
      showError("Unable to update ticket status.");
    }
  }
  
  protected void openModal(Parent root, String title) {
    Window ownerWindow =
      createTicketButton != null && createTicketButton.getScene() != null ? createTicketButton.getScene().getWindow() :
      null;
    
    Stage modalStage = new Stage();
    modalStage.setTitle(title);
    
    if (ownerWindow != null) {
      modalStage.initOwner(ownerWindow);
      modalStage.initModality(Modality.WINDOW_MODAL);
    } else {
      modalStage.initModality(Modality.APPLICATION_MODAL);
    }
    
    modalStage.setScene(new Scene(root));
    modalStage.setResizable(false);
    modalStage.sizeToScene();
    modalStage.setOnShown(event -> modalStage.centerOnScreen());
    modalStage.showAndWait();
  }
  
  protected void loadRecentActivity(VBox activityBox) {
    activityBox.getChildren().clear();
    
    int count = 0;
    
    for (TicketView ticket : tickets) {
      Notification notification = new Notification(
        ticket.getId(),
        buildActivityMessage(ticket),
        false,                  // isRead
        LocalDateTime.now(),
        getCurrentUserId()
      );

      activityBox.getChildren().add(ListRowItem.forActivity(notification));
      
      if (++count >= 8)
        break;
    }
  }
  
  protected String buildActivityMessage(TicketView ticket) {
    if (isUnassigned(ticket)) {
      return "\"" + ticket.getTitle() + "\" is waiting for assignment";
    }
    
    if (isStatus(ticket, TicketStatus.COMPLETED.name())) {
      return "\"" + ticket.getTitle() + "\" is waiting for review";
    }
    
    if (isStatus(ticket, TicketStatus.RESOLVED.name())) {
      return "\"" + ticket.getTitle() + "\" has been resolved";
    }
    
    if (isStatus(ticket, TicketStatus.IN_PROGRESS.name())) {
      return "\"" + ticket.getTitle() + "\" is still in progress";
    }
    
    return ("\"" + ticket.getTitle() + "\" has status " + Formatter.trimOrNA(ticket.getStatus()));
  }
}
