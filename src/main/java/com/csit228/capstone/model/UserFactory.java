package com.csit228.capstone.model;

public class UserFactory {

    private UserFactory() {
    }

    public static User createUser(Role role, int userId, String firstName, String lastName,
                                  String username, String passwordHash, int departmentId) {
        if (role == null) {
            return null;
        }

        return switch (role) {
            case EXECUTIVE -> new Executive(userId, firstName, lastName, username, passwordHash,departmentId);
            case EDITOR -> new Editor(userId, firstName, lastName, username, passwordHash,departmentId);
            case MEMBER -> new Member(userId, firstName, lastName, username, passwordHash,departmentId);
            default -> null;
        };
    }
}