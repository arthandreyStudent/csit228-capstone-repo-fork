package com.csit228.capstone.dao;

import com.csit228.capstone.database.DBConnector;
import com.csit228.capstone.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private static NotificationDAO notificationDAO;
    private static List<Notification> notifications;
    private static boolean notificationsLoaded;
    private static boolean notificationsDirty;

    private NotificationDAO() {
        notifications = new ArrayList<>();
        notificationsLoaded = false;
        notificationsDirty = true;
    }

    public static NotificationDAO getNotificationDAO() {
        if (notificationDAO == null) {
            notificationDAO = new NotificationDAO();
        }
        return notificationDAO;
    }

    public boolean createNotification(Notification notification) {
        if (notification == null) {
            return false;
        }

        String sql = """
                INSERT INTO notification (
                    message,
                    is_read,
                    created_at,
                    user_id
                )
                VALUES (?, ?, ?, ?);
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime createdAt = notification.getCreatedAt() != null
                    ? notification.getCreatedAt()
                    : LocalDateTime.now();

            stmt.setString(1, notification.getMessage());
            stmt.setBoolean(2, notification.isRead());
            stmt.setTimestamp(3, Timestamp.valueOf(createdAt));
            stmt.setInt(4, notification.getUserId());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notification.setNotificationId(generatedKeys.getInt(1));
                    }
                }

                notificationsDirty = true;
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public boolean createNotification(int userId, String message) {
        Notification notification = new Notification(
                0,
                message,
                false,
                LocalDateTime.now(),
                userId
        );

        return createNotification(notification);
    }

    public Notification getNotificationById(int id) {
        ensureNotificationsLoaded();

        for (Notification notification : notifications) {
            if (notification.getNotificationId() == id) {
                return notification;
            }
        }

        return null;
    }

    public List<Notification> getNotifications() {
        ensureNotificationsLoaded();
        return new ArrayList<>(notifications);
    }

    public List<Notification> getNotificationsByUserId(int userId) {
        ensureNotificationsLoaded();

        List<Notification> userNotifications = new ArrayList<>();

        for (Notification notification : notifications) {
            if (notification.getUserId() == userId) {
                userNotifications.add(notification);
            }
        }

        return userNotifications;
    }

    public List<Notification> getUnreadNotificationsByUserId(int userId) {
        ensureNotificationsLoaded();

        List<Notification> unreadNotifications = new ArrayList<>();

        for (Notification notification : notifications) {
            if (notification.getUserId() == userId && !notification.isRead()) {
                unreadNotifications.add(notification);
            }
        }

        return unreadNotifications;
    }

    public int getUnreadCount(int userId) {
        return getUnreadNotificationsByUserId(userId).size();
    }

    public boolean markAsRead(int notificationId) {
        String sql = """
                UPDATE notification
                SET is_read = ?
                WHERE id = ?;
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setBoolean(1, true);
            stmt.setInt(2, notificationId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                notificationsDirty = true;
            }

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAllAsRead(int userId) {
        String sql = """
                UPDATE notification
                SET is_read = ?
                WHERE user_id = ? AND is_read = ?;
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setBoolean(1, true);
            stmt.setInt(2, userId);
            stmt.setBoolean(3, false);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                notificationsDirty = true;
            }

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int notificationId) {
        String sql = """
                DELETE FROM notification
                WHERE id = ?;
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                notificationsDirty = true;
            }

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void fetchNotifications() {
        notifications.clear();

        String sql = """
                SELECT
                    id,
                    message,
                    is_read,
                    created_at,
                    user_id
                FROM notification
                ORDER BY created_at DESC;
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                notifications.add(mapNotification(rs));
            }

            notificationsLoaded = true;
            notificationsDirty = false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getInt("id"),
                rs.getString("message"),
                rs.getBoolean("is_read"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getInt("user_id")
        );
    }

    private void ensureNotificationsLoaded() {
        if (!notificationsLoaded || notificationsDirty) {
            fetchNotifications();
        }
    }

    public static void main(String[] args) {
        NotificationDAO notificationDAO = getNotificationDAO();
        List <Notification> jek = notificationDAO.getUnreadNotificationsByUserId(19)  ;
        for(Notification n : jek){
            System.out.println(n);
        }

    }
}
