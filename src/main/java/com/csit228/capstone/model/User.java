package com.csit228.capstone.model;

import java.io.Serializable;
import java.util.Objects;

public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;
    private String firstName;
    private String lastName;
    private String username;
    private transient String passwordHash;
    private Role role;


    private int department_id;

    public User() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(int department_id) {
        this.department_id = department_id;
    }

    public User(int userId, String firstName, String lastName, String username, String passwordHash, Role role, int department_id) {

        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.department_id = department_id;
    }

    public void viewDashboard() {
        // TO DO: connect this to the JavaFX dashboard screen
    }

    public void viewProfile() {
        // TO DO: connect this to the JavaFX profile screen
    }

    public int getUserId() {
        return userId;
    }

    public int getId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFirstname() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setFirstname(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLastname() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLastname(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";

        String fullName = (first + " " + last).trim();

        if (fullName.isEmpty()) {
            return username != null ? username : "User " + userId;
        }

        return fullName;
    }

    public String getUsername() {
        return username;
    }



    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean hasRole(Role role) {
        return this.role == role;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}