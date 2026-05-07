package com.csit228.capstone.model;

import java.time.LocalDateTime;

public class Comment {
    private Integer id;
    private Integer ticketId;
    private String content;
    private String createdBy;
    private LocalDateTime dateCreated;

    public Comment(Integer id, Integer ticketId, String content, String createdBy, LocalDateTime dateCreated) {
        this.id = id;
        this.ticketId = ticketId;
        this.content = content;
        this.createdBy = createdBy;
        this.dateCreated = dateCreated;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", ticketId=" + ticketId +
                ", content='" + content + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", dateCreated=" + dateCreated +
                '}';
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
}
