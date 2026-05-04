package com.csit228.capstone.model;

import java.io.Serializable;
import java.time.LocalDate;

public class TicketMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String title;
    private final String description;
    private final String category;
    private final TicketPriority priority;
    private final LocalDate deadline;

    public TicketMemento(String title, String description, String category,
                         TicketPriority priority, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
    }

    public TicketMemento getSavedState() {
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }
}