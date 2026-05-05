package com.csit228.capstone.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    private int ticketId;
    private String title;
    private String description;
    private TicketPriority priority;
    private LocalDateTime deadline;
    private TicketStatus status;
    private Integer createdBy;
    private Integer assignedTo;
    private LocalDateTime dateCreated;
    private LocalDateTime lastUpdated;
    private Integer departmentId;


    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }


    private final List<Attachment> attachments = new ArrayList<>();

    public Ticket() {
        this.status = TicketStatus.OPEN;
        this.priority = TicketPriority.MEDIUM;
        this.dateCreated = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    public Ticket(
            int ticketId,
            String title,
            String description,
            TicketPriority priority,
            LocalDateTime deadline,
            TicketStatus status,
            Integer createdBy,
            Integer assignedTo,
            LocalDateTime dateCreated,
            LocalDateTime lastUpdated,
            Integer departmentId
    ) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.status = status;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.dateCreated = dateCreated;
        this.lastUpdated = lastUpdated;
        this.departmentId = departmentId;
    }
//    //public TicketMemento createMemento() {
//        return new TicketMemento(title, description, priority, deadline);
//    }

    public void restore(TicketMemento m) {
        if (m == null) {
            return;
        }

        this.title = m.getTitle();
        this.description = m.getDescription();

        this.priority = m.getPriority();
        this.deadline = m.getDeadline().atStartOfDay();
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

    public int getCreatedBy() {
        return createdBy;
    }

    public int getAssignedTo() {
        if(assignedTo==-1){
            return  -1;
        }
        return assignedTo;
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


    public boolean isOverdue() {
        return deadline != null
                && LocalDate.now().isAfter(ChronoLocalDate.from(deadline))
                && status != TicketStatus.COMPLETED
                && status != TicketStatus.RESOLVED;
    }

    public void touch() {
        this.lastUpdated = LocalDate.now().atStartOfDay();
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

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline.atStartOfDay();
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

    @Override
    public String toString() {
        return "Ticket{" +
                "\nticketId=" + ticketId +
                "\n, title='" + title + '\'' +
                "\n, description='" + description + '\'' +
                "\n, priority=" + priority +
                "\n, deadline=" + deadline +
                "\n, status=" + status +
                "\n, createdBy=" + createdBy +
                "\n, assignedTo=" + assignedTo +
                "\n, dateCreated=" + dateCreated +
                "\n, lastUpdated=" + lastUpdated +
                "\n, departmentId=" + departmentId +
                "\n, attachments=" + attachments +
                '}';
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