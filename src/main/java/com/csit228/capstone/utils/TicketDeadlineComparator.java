package com.csit228.capstone.utils;

import com.csit228.capstone.model.TicketView;

import java.time.LocalDateTime;
import java.util.Comparator;

public class TicketDeadlineComparator implements Comparator<TicketView> {

    public enum SortMode {
        NEAREST,
        FARTHEST
    }

    private final SortMode sortMode;

    public TicketDeadlineComparator(SortMode sortMode) {
        this.sortMode = sortMode != null ? sortMode : SortMode.NEAREST;
    }

    @Override
    public int compare(TicketView ticketA, TicketView ticketB) {
        LocalDateTime deadlineA = ticketA != null ? ticketA.getDeadline() : null;
        LocalDateTime deadlineB = ticketB != null ? ticketB.getDeadline() : null;

        if (deadlineA == null && deadlineB == null) {
            return 0;
        }

        if (deadlineA == null) {
            return 1;
        }

        if (deadlineB == null) {
            return -1;
        }

        if (sortMode == SortMode.FARTHEST) {
            return deadlineB.compareTo(deadlineA);
        }

        return deadlineA.compareTo(deadlineB);
    }

    public static SortMode getSortModeFromText(String selectedText) {
        if (selectedText == null) {
            return SortMode.NEAREST;
        }

        if (selectedText.equalsIgnoreCase("Farthest Deadline")) {
            return SortMode.FARTHEST;
        }

        return SortMode.NEAREST;
    }
}