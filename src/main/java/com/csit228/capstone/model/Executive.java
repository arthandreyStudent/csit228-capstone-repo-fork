package com.csit228.capstone.model;

import com.csit228.capstone.enums.Role;

public class Executive extends User {

  public Executive() {
    setRole(Role.EXECUTIVE);
  }

  public Executive(
    int userId,
    String firstName,
    String lastName,
    String username,
    String passwordHash,
    int departmentId
  ) {
    super(
      userId,
      firstName,
      lastName,
      username,
      passwordHash,
      Role.EXECUTIVE,
      departmentId
    );
  }

}
