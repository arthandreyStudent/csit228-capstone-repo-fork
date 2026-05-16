package com.csit228.capstone.model;

import java.io.*;

public class Serializer {

  private User user;
  private Ticket ticket;
  private String filePath;

  public Serializer() {}

  public Serializer setUser(User u) {
    this.user = u;
    return this;
  }

  public Serializer setTicket(Ticket t) {
    this.ticket = t;
    return this;
  }

  public Serializer setFilePath(String path) {
    this.filePath = path;
    return this;
  }

  public Serializer build() {
    return this;
  }

  public boolean serialize() {
    if (filePath == null || filePath.trim().isEmpty()) {
      return false;
    }

    Object objectToSave = null;

    if (ticket != null) {
      objectToSave = ticket;
    } else if (user != null) {
      objectToSave = user;
    }

    if (objectToSave == null) {
      return false;
    }

    try (
      ObjectOutputStream outputStream = new ObjectOutputStream(
        new FileOutputStream(filePath)
      )
    ) {
      outputStream.writeObject(objectToSave);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public Object deserialize() {
    if (filePath == null || filePath.trim().isEmpty()) {
      return null;
    }

    try (
      ObjectInputStream inputStream = new ObjectInputStream(
        new FileInputStream(filePath)
      )
    ) {
      return inputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  public User getUser() {
    return user;
  }

  public Ticket getTicket() {
    return ticket;
  }

  public String getFilePath() {
    return filePath;
  }
}
