package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.enums.Role;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.*;
import com.csit228.capstone.utils.NotificationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class StaffTicketController extends BaseTicketController {

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
            for (User user : userDAO.getUsersByDepartment(departmentId)) {
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
            for (User user : userDAO.getUsersByDepartment(department.getId())) {
                if (user != null && user.hasRole(Role.MEMBER)) {
                    members.add(user);
                }
            }
        }
        return members;
    }

    protected int getDepartmentIdByName(String departmentName) {
        if (departmentName == null) return -1;

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
            NotificationManager.notifyAssignee(user, ticket.getTitle(), ticket.getCreatedBy());
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
        Window ownerWindow = (createTicketButton != null && createTicketButton.getScene() != null)
                ? createTicketButton.getScene().getWindow()
                : null;

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
  
    protected void openTicketDetailModal(TicketView ticket) {
      try {
        FXMLLoader loader =
          new FXMLLoader(getClass().getResource("/com/csit228/capstone/view/BaseTicketDetailModalView.fxml"));
        Parent root = loader.load();
        
        BaseTicketDetailModalController controller = loader.getController();
        controller.loadTicket(ticket);
        controller.setOnTicketMutated(this::refreshDashboard);
        
        openModal(root, "Ticket Details");
      } catch (IOException e) {
        showError("Unable to open Ticket Details modal.");
      }
    }
  
    protected boolean isInteractiveTarget(Object target) {
      if (!(target instanceof Node)) {
        return false;
      }
      
      Node node = (Node) target;
      while (node != null) {
        if (node instanceof ButtonBase || node instanceof ComboBoxBase || node instanceof TextInputControl) {
          return true;
        }
        node = node.getParent();
      }
      
      return false;
    }
    
}