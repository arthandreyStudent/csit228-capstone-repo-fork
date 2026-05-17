package com.csit228.capstone.model;

public class Job {

  private int id;
  private String name;

  public Job(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }
}
