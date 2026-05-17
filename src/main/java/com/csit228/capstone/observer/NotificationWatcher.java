// com/csit228/capstone/observer/NotificationWatcher.java
package com.csit228.capstone.observer;

import com.csit228.capstone.dao.NotificationDAO;
import com.csit228.capstone.model.Notification;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationWatcher extends BaseWatcher<NotificationObserver> {

    private static NotificationWatcher instance;
    private final Map<Integer, Integer> lastNotifCountPerUser = new HashMap<>();

    private NotificationWatcher() {}

    public static synchronized NotificationWatcher getInstance() {
        if (instance == null) instance = new NotificationWatcher();
        return instance;
    }

    @Override
    protected String getThreadName() {
        return "notification-watcher";
    }

    @Override
    protected void fetchAndNotify() {
        try {
            if (observers.isEmpty()) {
                stop();
                return;
            }

            NotificationDAO dao = NotificationDAO.getNotificationDAO();

            for (NotificationObserver observer : observers) {
                int userId = observer.getUserId();

                List<Notification> userUnreadNotifs = dao.fetchUnreadNotificationsForUser(userId);
                int count = userUnreadNotifs.size();

                if (count != lastNotifCountPerUser.getOrDefault(userId, -1)) {
                    lastNotifCountPerUser.put(userId, count);

                    final List<Notification> toSend = new ArrayList<>(userUnreadNotifs);

                    Platform.runLater(() -> {
                        try {
                            observer.onNotificationsChanged(toSend);
                        } catch (Exception e) {
                            System.err.println("[NotificationWatcher] Observer error: " + e.getMessage());
                        }
                    });
                }
            }

        } catch (Exception e) {
            System.err.println("[NotificationWatcher] Error: " + e.getMessage());
        }
    }

    public void setInitialCount(int userId, int count) {
        lastNotifCountPerUser.put(userId, count);
    }
}