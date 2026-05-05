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


    public List<Ticket> viewMyTasks() {
        //return tickets assigned to this member from TicketDAO
        return new ArrayList<>();
    }


    public void receiveNotification(Notification n) {
        //iconnect sa notification display or NotificationDAO
    }
}
