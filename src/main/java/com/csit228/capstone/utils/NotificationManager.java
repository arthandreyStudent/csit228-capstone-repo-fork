package com.csit228.capstone.utils;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.NotificationDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.model.*;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private static final NotificationDAO notificationDAO = NotificationDAO.getNotificationDAO();
    private static final UserDAO         userDAO         = UserDAO.getUserDAO();
    private static final DepartmentDAO   departmentDAO   = DepartmentDAO.getDepartmentDAO();

    public static void notifyAssignee(User assignedUser, String ticketTitle, String assignerName) {
        if (assignedUser == null) return;

        String title   = "You have been assigned a ticket";
        String message = assignerName + " assigned you the ticket: \"" + ticketTitle + "\"";

        notificationDAO.createNotification(assignedUser.getUserId(), title, message);
    }

    public static void notifyCreation(Ticket ticket, String creatorName) {
        if (ticket == null) return;

        boolean isVolunteer = ticket.getDepartmentId() == null;

        String title   = isVolunteer ? creatorName + " created a volunteer ticket" : creatorName + " created a new ticket";
        String message = isVolunteer ? creatorName + " created a volunteer ticket: \"" + ticket.getTitle() + "\""
                                     : creatorName + " created a ticket in your department: \"" + ticket.getTitle() + "\"";

        List<User> recipients = isVolunteer ? userDAO.getMembers() : getDepartmentMembers(ticket.getDepartmentId());

        if (!recipients.isEmpty()) {
            notificationDAO.createNotifications(recipients, title, message);
        }
    }

    private static List<User> getDepartmentMembers(Integer departmentId) {
        if (departmentId == null || departmentDAO.getDepartmentByID(departmentId) == null) {
            return new ArrayList<>();
        }

        return userDAO.getMembersByDepartment(departmentId);
    }

    public static void notifyCreator(TicketView ticket, String memberName) {
        if (ticket == null || ticket.getCreatedBy() == null) return;

        User creator = userDAO.getUserByName(ticket.getCreatedBy());
        if (creator == null) return;

        String title   = "A member picked up your ticket";
        String message = memberName + " will work on your ticket: \"" + ticket.getTitle() + "\"";

        notificationDAO.createNotification(creator.getUserId(), title, message);
    }

    public static void notifyReturnTicket(TicketView ticket, String editorName) {
        if (ticket == null || ticket.getAssignedToName() == null) return;

        User assignedMember = userDAO.getUserByName(ticket.getAssignedToName());
        if (assignedMember == null) return;

        String title   = "Your ticket was sent back";
        String message = editorName + " returned \"" + ticket.getTitle() + "\" to in-progress for revision.";

        notificationDAO.createNotification(assignedMember.getUserId(), title, message);
    }

    public static void notifyApprove(TicketView ticket, String editorName) {
        if (ticket == null || ticket.getAssignedToName() == null) return;

        User assignedMember = userDAO.getUserByName(ticket.getAssignedToName());
        if (assignedMember == null) return;

        String title   = "Your ticket was approved";
        String message = editorName + " approved \"" + ticket.getTitle() + "\"";

        notificationDAO.createNotification(assignedMember.getUserId(), title, message);
    }

    public static void notifySubmitted(TicketView ticket, String memberName) {
        if (ticket == null || ticket.getCreatedBy() == null) return;

        User creator = userDAO.getUserByName(ticket.getCreatedBy());
        if (creator == null) return;

        String title   = "A ticket is ready for review";
        String message = memberName + " submitted \"" + ticket.getTitle() + "\" for review.";

        notificationDAO.createNotification(creator.getUserId(), title, message);
    }


}
