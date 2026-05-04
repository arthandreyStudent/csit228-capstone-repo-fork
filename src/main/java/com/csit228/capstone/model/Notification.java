package com.csit228.capstone.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    private int notificationId;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;
    private int userId;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public Notification(int notificationId, String message, String type,
                        boolean read, LocalDateTime createdAt, int userId) {
        this.notificationId = notificationId;
        this.message = message;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public void markAsRead() {
        this.read = true;
    }

    public void sendTo(User u) {
        if (u != null) {
            this.userId = u.getUserId();
        }

        //iconnect sa NotificationDAO
    }

    public int getNotificationId() {
        return notificationId;
    }

    public int getId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public void setId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public boolean getIsRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setIsRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public int getReceiverId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setReceiverId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        if (message == null || message.trim().isEmpty()) {
            return "Notification " + notificationId;
        }

        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }

        Notification that = (Notification) o;
        return notificationId == that.notificationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }
}