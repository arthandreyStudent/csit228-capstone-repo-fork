package com.csit228.capstone.model;

import com.csit228.capstone.utils.Formatter;

import java.util.ArrayList;
import java.util.List;

public class Department {
  
  private int id;
  private String name;
  private String description;
  
  private List<Job> jobs = new ArrayList<>();
  
  public Department(int id, String name, String description) {
    this.id = id;
    this.name = Formatter.formatDepartmentName(name);
    this.description = description;
  }
  
  public int getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }
  
  public String getDescription() {
    return description;
  }
  
  public List<Job> getJobs() {
    return jobs;
  }
  
  public String toString() {
    return name;
  }
}
