package com.csit228.capstone.model;

import com.csit228.capstone.enums.Role;
import com.csit228.capstone.enums.TicketStatus;

public class Editor extends User {

  public Editor() {
    setRole(Role.EDITOR);
  }

  public Editor(
    int userId,
    String firstName,
    String lastName,
    String username,
    String passwordHash,
    int department_id
  ) {
    super(
      userId,
      firstName,
      lastName,
      username,
      passwordHash,
      Role.EDITOR,
      department_id
    );
  }

}
