package com.csit228.capstone.observer;

import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.model.TicketView;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class TicketWatcher extends BaseWatcher<TicketObserver> {

    private static TicketWatcher instance;

    private String lastSnapshot = "";

    private TicketWatcher() {}

    public static synchronized TicketWatcher getInstance() {
        if (instance == null) instance = new TicketWatcher();
        return instance;
    }

    @Override
    protected String getThreadName() {
        return "ticket-watcher";
    }

    @Override
    protected void fetchAndNotify() {
        try {
            if (observers.isEmpty()) { stop(); return; }

            TicketDAO dao = TicketDAO.getTicketDAO();
            dao.getTicketViews();
            List<TicketView> fresh = new ArrayList<>(dao.getViews());

            String newSnapshot = buildSnapshot(fresh);
            if (!newSnapshot.equals(lastSnapshot)) {
                lastSnapshot = newSnapshot;
                Platform.runLater(() -> notifyObservers(fresh));
            }

        } catch (Exception e) {
            System.err.println("[TicketWatcher] Error: " + e.getMessage());
        }
    }

    private String buildSnapshot(List<TicketView> tickets) {
        if (tickets == null) return "";
        StringBuilder sb = new StringBuilder();
        for (TicketView t : tickets) {
            if (t == null) { sb.append("null-ticket\n"); continue; }
            appendField(sb, t.getId());
            appendField(sb, t.getTitle());
            appendField(sb, t.getDescription());
            appendField(sb, t.getPriority());
            appendField(sb, t.getStatus());
            appendField(sb, t.getDepartmentName());
            appendField(sb, t.getCreatedBy());
            appendField(sb, t.getAssignedToName());
            appendField(sb, t.getDeadline());
            sb.append("\n");
        }
        return sb.toString();
    }

    private void appendField(StringBuilder sb, Object value) {
        sb.append(value != null ? value : "null").append("|");
    }

    private void notifyObservers(List<TicketView> tickets) {
        for (TicketObserver o : observers) {
            try { o.onTicketChange(tickets); }
            catch (Exception e) { System.err.println("[TicketWatcher] Observer error: " + e.getMessage()); }
        }
    }
}