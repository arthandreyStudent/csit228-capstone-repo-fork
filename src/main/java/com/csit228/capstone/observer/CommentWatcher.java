package com.csit228.capstone.observer;

import com.csit228.capstone.dao.CommentDAO;
import com.csit228.capstone.model.Comment;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentWatcher extends BaseWatcher<CommentObserver> {

    private static CommentWatcher instance;

    private final Map<Integer, Integer> lastCommentCountPerTicket = new HashMap<>();

    private CommentWatcher() {}

    public static synchronized CommentWatcher getInstance() {
        if (instance == null) instance = new CommentWatcher();
        return instance;
    }

    @Override
    protected String getThreadName() {
        return "comment-watcher";
    }

    @Override
    protected void fetchAndNotify() {
        try {
            if (observers.isEmpty()) { stop(); return; }

            CommentDAO dao = CommentDAO.getCommentDAO();

            for (CommentObserver observer : observers) {
                int ticketId = observer.getTicketId();

                List<Comment> fresh = dao.findByTicketId(ticketId);
                int count = fresh != null ? fresh.size() : 0;

                if (count != lastCommentCountPerTicket.getOrDefault(ticketId, -1)) {
                    lastCommentCountPerTicket.put(ticketId, count);
                    final List<Comment> toSend = fresh != null ? new ArrayList<>(fresh) : new ArrayList<>();
                    Platform.runLater(() -> {
                        try { observer.onCommentsChanged(toSend); }
                        catch (Exception e) { System.err.println("[CommentWatcher] Observer error: " + e.getMessage()); }
                    });
                }
            }

        } catch (Exception e) {
            System.err.println("[CommentWatcher] Error: " + e.getMessage());
        }
    }

    public void setInitialCount(int ticketId, int count) {
        lastCommentCountPerTicket.put(ticketId, count);
    }

    public void clearTicket(int ticketId) {
        lastCommentCountPerTicket.remove(ticketId);
    }
}
