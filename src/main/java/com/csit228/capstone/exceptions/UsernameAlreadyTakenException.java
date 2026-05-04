package com.csit228.capstone.exceptions;

public class UsernameAlreadyTakenException extends  Exception {
    public UsernameAlreadyTakenException() {
        super("Username Already Taken");
    }
}
