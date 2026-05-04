package com.csit228.capstone.model;

public class Editor extends User {

    public Editor() {
        setRole(Role.EDITOR);
    }

    public Editor(int userId, String firstName, String lastName, String username, String passwordHash, int department_id) {
        super(userId, firstName, lastName, username, passwordHash, Role.EDITOR, department_id);
    }



    public boolean editTicket(Ticket t) {
        // TO DO: update ticket details once TicketDAO or controller logic is created
        return t != null;
    }

    public boolean updateTicketStatus(Ticket t, TicketStatus status) {
        if (t == null || status == null) {
            return false;
        }

        t.setStatus(status);
        return true;
    }

    public boolean reviewTicket(Ticket t) {
        if (t == null) {
            return false;
        }

        // connect to review screen logic or TicketDAO.
        return true;
    }

    public void notifyMember(Member m) {
        // iconnect sa NotificationDAO once created
    }
}