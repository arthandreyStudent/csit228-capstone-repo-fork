package com.csit228.capstone.model;

import java.util.ArrayList;
import java.util.List;

public class Member extends User {

    public Member() {
        setRole(Role.MEMBER);
    }

    public Member(int userId, String firstName, String lastName, String username, String passwordHash, int departmentId) {
        super(userId, firstName, lastName, username, passwordHash, Role.MEMBER, departmentId);
    }

    public Member(int i, String juan, String delacruz, String bayan, String strongpass, Role role) {
    }

    public void viewVolunteerBoard() {
        // connect this to VolunteerBoardController or VolunteerBoard screen
    }

    public boolean volunteerForTicket(Ticket t) {
        if (t == null) {
            return false;
        }

        if (!t.isAvailableForVolunteer()) {
            return false;
        }

        t.assignTo(this);
        return true;
    }

    public List<Ticket> viewMyTasks() {
        //return tickets assigned to this member from TicketDAO
        return new ArrayList<>();
    }

    public boolean updateTaskStatus(Ticket t) {
        if (t == null) {
            return false;
        }

        if (t.getAssignedTo() == null) {
            return false;
        }

        if (t.getAssignedTo().getUserId() != getUserId()) {
            return false;
        }

        if (t.getStatus() == TicketStatus.OPEN) {
            t.markInProgress();
        }

        return true;
    }

    public void receiveNotification(Notification n) {
        //iconnect sa notification display or NotificationDAO
    }
}
