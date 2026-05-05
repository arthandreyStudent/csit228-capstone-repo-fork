
package com.csit228.capstone.model;

import java.time.LocalDateTime;

public class TicketView {
    private int id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String departmentName;
    private String createdBy;
    private String assignedToName;
    private LocalDateTime lastUpdated;
    private LocalDateTime dateCreated;
    private LocalDateTime deadline;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    @Override
    public String toString() {
        return "TicketView{" +
                "\nid=" + id +
                ", \ntitle='" + title + '\'' +
                ", \ndescription='" + description + '\'' +
                ", \npriority='" + priority + '\'' +
                ", \nstatus='" + status + '\'' +
                ",\ndepartmentName='" + departmentName + '\'' +
                ", \ncreatedBy='" + createdBy + '\'' +
                ", \nassignedToName='" + assignedToName + '\'' +
                '}';
    }

    public TicketView(int id, String title, String description, String priority, String status, String departmentName, String createdBy, String assignedToName, LocalDateTime lastUpdated, LocalDateTime dateCreated, LocalDateTime deadline) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.departmentName = departmentName;
        this.createdBy = createdBy;
        this.assignedToName = assignedToName;
        this.lastUpdated = lastUpdated;
        this.dateCreated = dateCreated;
        this.deadline = deadline;
    }
}