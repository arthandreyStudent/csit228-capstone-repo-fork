// com/csit228/capstone/observer/NotificationObserver.java
package com.csit228.capstone.observer;

import com.csit228.capstone.model.Notification;
import java.util.List;

public interface NotificationObserver {
    void onNotificationsChanged(List<Notification> updatedNotifications);
    int getUserId();
}