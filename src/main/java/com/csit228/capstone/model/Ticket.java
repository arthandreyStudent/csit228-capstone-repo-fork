package com.csit228.capstone.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    private int ticketId;
    private String title;
    private String description;
    private String category;
    private TicketPriority priority;
    private LocalDate deadline;
    private TicketStatus status;
    private User createdBy;
    private Member assignedTo;
    private LocalDate dateCreated;
    private LocalDate lastUpdated;
    private final List<Attachment> attachments = new ArrayList<>();

    public Ticket() {
        this.status = TicketStatus.OPEN;
        this.priority = TicketPriority.MEDIUM;
        this.dateCreated = LocalDate.now();
        this.lastUpdated = LocalDate.now();
    }

    public Ticket(int ticketId, String title, String description, String category,
                  TicketPriority priority, LocalDate deadline, TicketStatus status,
                  User createdBy, Member assignedTo) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : TicketPriority.MEDIUM;
        this.deadline = deadline;
        this.status = status != null ? status : TicketStatus.OPEN;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.dateCreated = LocalDate.now();
        this.lastUpdated = LocalDate.now();
    }

    public Ticket(int ticketId, String title, String description, String category,
                  TicketPriority priority, LocalDate deadline, TicketStatus status,
                  User createdBy, Member assignedTo,
                  LocalDate dateCreated, LocalDate lastUpdated) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : TicketPriority.MEDIUM;
        this.deadline = deadline;
        this.status = status != null ? status : TicketStatus.OPEN;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.dateCreated = dateCreated != null ? dateCreated : LocalDate.now();
        this.lastUpdated = lastUpdated != null ? lastUpdated : LocalDate.now();
    }

    public TicketMemento createMemento() {
        return new TicketMemento(title, description, category, priority, deadline);
    }

    public void restore(TicketMemento m) {
        if (m == null) {
            return;
        }

        this.title = m.getTitle();
        this.description = m.getDescription();
        this.category = m.getCategory();
        this.priority = m.getPriority();
        this.deadline = m.getDeadline();
        touch();
    }

    public void assignTo(Member m) {
        this.assignedTo = m;

        if (m != null) {
            this.status = TicketStatus.IN_PROGRESS;
        }

        touch();
    }

    public void markInProgress() {
        this.status = TicketStatus.IN_PROGRESS;
        touch();
    }

    public void complete() {
        this.status = TicketStatus.COMPLETED;
        touch();
    }

    public void resolve() {
        this.status = TicketStatus.RESOLVED;
        touch();
    }

    public void addAttachment(Attachment a) {
        if (a == null) {
            return;
        }

        attachments.add(a);
        touch();
    }

    public boolean removeAttachment(Attachment a) {
        if (a == null) {
            return false;
        }

        boolean removed = attachments.remove(a);

        if (removed) {
            touch();
        }

        return removed;
    }

    public boolean isAssigned() {
        return assignedTo != null;
    }

    public boolean isAvailableForVolunteer() {
        return assignedTo == null && status == TicketStatus.OPEN;
    }

    public boolean isOverdue() {
        return deadline != null
                && LocalDate.now().isAfter(deadline)
                && status != TicketStatus.COMPLETED
                && status != TicketStatus.RESOLVED;
    }

    public void touch() {
        this.lastUpdated = LocalDate.now();
    }

    public int getTicketId() {
        return ticketId;
    }

    public int getId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public void setId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        touch();
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        if (priority == null) {
            return;
        }

        this.priority = priority;
        touch();
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
        touch();
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        if (status == null) {
            return;
        }

        this.status = status;
        touch();
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
        touch();
    }

    public Member getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Member assignedTo) {
        this.assignedTo = assignedTo;
        touch();
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<Attachment> getAttachments() {
        return new ArrayList<>(attachments);
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments.clear();

        if (attachments != null) {
            this.attachments.addAll(attachments);
        }

        touch();
    }

    public int getCreatedById() {
        if (createdBy == null) {
            return 0;
        }

        return createdBy.getUserId();
    }

    public int getAssignedToId() {
        if (assignedTo == null) {
            return 0;
        }

        return assignedTo.getUserId();
    }

    @Override
    public String toString() {
        if (title == null || title.trim().isEmpty()) {
            return "Ticket " + ticketId;
        }

        return title + " - " + status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ticket)) {
            return false;
        }

        Ticket ticket = (Ticket) o;
        return ticketId == ticket.ticketId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }
}