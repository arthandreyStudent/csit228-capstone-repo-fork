package com.csit228.capstone.utils;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.NotificationDAO;
import com.csit228.capstone.dao.UserDAO;
import com.csit228.capstone.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralizes all notification logic for ticket lifecycle events.
 *
 * Behavior summary:
 *  1. Ticket created WITH assigned member   → notify assigned member only
 *  2. Ticket created WITHOUT assigned member → notify all members of the ticket's
 *     department; if no department (volunteer), notify all members everywhere
 *  3. Ticket/volunteer taken by a member    → notify the ticket creator only
 *  4. Ticket assigned by editor/executive   → notify the assigned member only
 *  5. Ticket returned by editor             → notify the assigned member only
 */
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




}
