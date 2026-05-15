// com/csit228/capstone/observer/TicketWatcher.java
package com.csit228.capstone.observer;

import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.model.TicketView;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TicketWatcher {

    private static TicketWatcher instance;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ticket-watcher");
                t.setDaemon(true); // auto-dies when the app closes
                return t;
            });

    private final List<DashboardObserver> observers = new CopyOnWriteArrayList<>();
    private ScheduledFuture<?> task;

    // The previous result set we compare against
    private String lastSnapshot = "";

    private TicketWatcher() {}

    public static synchronized TicketWatcher getInstance() {
        if (instance == null) instance = new TicketWatcher();
        return instance;
    }

    public void addObserver(DashboardObserver o)    { if (!observers.contains(o)) observers.add(o); }
    public void removeObserver(DashboardObserver o) { observers.remove(o); }

    public void start(int intervalSeconds) {
        if (task != null && !task.isCancelled()) return;
        task = executor.scheduleAtFixedRate(this::fetchAndCompare, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        if (task != null) task.cancel(false);
    }

    public void shutdown() {
        stop();
        executor.shutdownNow();
    }

    // ── Core logic ──────────────────────────────────────────────────

    private void fetchAndCompare() {
        try {
            TicketDAO dao = TicketDAO.getTicketDAO();
            dao.getTicketViews();
            List<TicketView> freshResults = new ArrayList<>(dao.getViews());

            String newSnapshot = buildSnapshot(freshResults);

            if (!newSnapshot.equals(lastSnapshot)) {
                lastSnapshot = newSnapshot;
                // UI updates MUST run on the JavaFX thread
                Platform.runLater(() -> notifyObservers(freshResults));
            }

        } catch (Exception e) {
            System.err.println("[TicketWatcher] Error: " + e.getMessage());
        }
    }

    /**
     * Turns the result set into a comparable string.
     * Compares id + status + assignedTo + lastUpdated per ticket.
     * Any row change → different snapshot → triggers notify.
     */
    private String buildSnapshot(List<TicketView> tickets) {
        StringBuilder sb = new StringBuilder();
        for (TicketView t : tickets) {
            sb.append(t.getId())
                    .append(t.getStatus())
                    .append(t.getAssignedToName() != null ? t.getAssignedToName() : "unassigned")
                    .append(t.getLastUpdated() != null ? t.getLastUpdated().toString() : "no-date")
                    .append("|");
        }
        return sb.toString();
    }

    private void notifyObservers(List<TicketView> tickets) {
        for (DashboardObserver o : observers) {
            try { o.onDataChanged(tickets); }
            catch (Exception e) { System.err.println("[TicketWatcher] Observer error: " + e.getMessage()); }
        }
    }
}