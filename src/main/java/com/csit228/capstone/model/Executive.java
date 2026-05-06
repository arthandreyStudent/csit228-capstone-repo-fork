package com.csit228.capstone.model;

public class Executive extends User {

    public Executive() {
        setRole(Role.EXECUTIVE);
    }

    public Executive(int userId, String firstName, String lastName, String username, String passwordHash,int departmentId) {
        super(userId, firstName, lastName, username, passwordHash, Role.EXECUTIVE, departmentId);
    }

    public boolean createTicket(Ticket t) {
        // TO DO: pass ticket to TicketDAO once TicketDAO is created
        return t != null;
    }

    public void manageUsers() {
        // TO DO: connect this to user management screen or UserDAO
    }

    public void viewAnalytics() {
        // TO DO: connect this to dashboard analytics
    }

    public void sendNotification(Notification n) {
        // TO DO: connect this to NotificationDAO once created
    }
}