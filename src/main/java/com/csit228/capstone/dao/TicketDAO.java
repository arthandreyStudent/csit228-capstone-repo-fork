package com.csit228.capstone.dao;

import com.csit228.capstone.database.DBConnector;

import com.csit228.capstone.enums.TicketPriority;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    private static TicketDAO ticketDAO;
    private static volatile List<TicketView> tickets;
    private static volatile boolean ticketsLoaded;
    private static volatile boolean ticketsDirty;

    private TicketDAO() {
        tickets = new ArrayList<>();
        ticketsLoaded = false;
        ticketsDirty = true;
    }

    public boolean assignTicket(int userId, int ticketId) {
        String sql = """
                UPDATE ticket
                SET assigned_to = ? 
                WHERE id = ?;
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, ticketId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ticketsDirty = true;
            }
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public List<TicketView> getTicketByDepartment(Department department) {
        if (department == null) {
            return new ArrayList<>();
        }

        List<TicketView> allTickets = getViews();
        List<TicketView> ticketViews = new ArrayList<>();
        for (TicketView ticketView : allTickets) {
            if (ticketView.getDepartmentName() != null && ticketView.getDepartmentName().equalsIgnoreCase(department.getName())) {
                ticketViews.add(ticketView);
            }
        }
        return ticketViews;
    }

    public boolean updateStatus(int ticketId, TicketStatus ticketStatus) {
        String sql = """
                UPDATE ticket
                SET status = ? 
                WHERE id = ?;
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, ticketStatus.toString());
            stmt.setInt(2, ticketId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ticketsDirty = true;
            }
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static TicketDAO getTicketDAO() {
        if (ticketDAO == null) {
            ticketDAO = new TicketDAO();
        }
        return ticketDAO;
    }


    public boolean createTicket(Ticket ticket) {
        String sql = """
                INSERT INTO ticket (
                    title,
                    description,
                    priority,
                    status,
                    department_id,
                    created_by,
                    assigned_to,
                    deadline
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, ticket.getTitle());
            stmt.setString(2, ticket.getDescription());
            stmt.setString(3, ticket.getPriority().toString());
            stmt.setString(4, ticket.getStatus().toString());
            stmt.setInt(6, ticket.getCreatedBy());
            if (ticket.getDepartmentId() == null) {
                stmt.setNull(5, Types.INTEGER);
            } else {
                stmt.setInt(5, ticket.getDepartmentId());
            }
            if (ticket.getAssignedTo() == null) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, ticket.getAssignedTo());
            }
            stmt.setTimestamp(8, Timestamp.valueOf(ticket.getDeadline()));
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ticketsDirty = true;
            }

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Ticket getTicketById(int ticketId) {
        String sql = """
                SELECT *
                FROM ticket t
                WHERE t.id = ?
                """;
        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    String priorityString = rs.getString("priority");
                    TicketPriority priority = priorityString != null
                            ? TicketPriority.valueOf(priorityString)
                            : null;

                    String statusString = rs.getString("status");
                    TicketStatus status = statusString != null
                            ? TicketStatus.valueOf(statusString)
                            : null;

                    Integer assignedTo = rs.getObject("assigned_to", Integer.class);
                    Integer departmentId = rs.getObject("department_id", Integer.class);

                    LocalDateTime deadline = rs.getObject("deadline", LocalDateTime.class);
                    LocalDateTime dateCreated = rs.getObject("date_created", LocalDateTime.class);
                    LocalDateTime lastUpdated = rs.getObject("last_updated", LocalDateTime.class);

                    return new Ticket(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            priority,
                            deadline,
                            status,
                            rs.getInt("created_by"),
                            assignedTo,
                            dateCreated,
                            lastUpdated,
                            departmentId
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void getTicketViews() {
        refreshTicketViews();
    }

    private synchronized void ensureTicketViewsLoaded() {
        if (!ticketsLoaded || ticketsDirty) {
            refreshTicketViews();
        }
    }

    private synchronized void refreshTicketViews() {
        String sql = """
                SELECT
                    t.id,
                    t.title,
                    t.description,
                    t.last_updated,
                    t.date_created,
                    t.deadline,
                    t.priority,
                    t.status,
                    CASE
                        WHEN d.name IS NULL THEN 'volunteer'
                        ELSE d.name
                    END AS department_name,
                    CONCAT(c.firstname, ' ', c.lastname) AS created_by,
                    CONCAT(u.firstname, ' ', u.lastname) AS assigned_to_name
                FROM ticket t
                LEFT JOIN department d ON t.department_id = d.id
                LEFT JOIN user u ON t.assigned_to = u.id
                LEFT JOIN user c ON t.created_by = c.id
                ORDER BY t.date_created DESC
                """;
        
        List<TicketView> freshTickets = new ArrayList<>();

        long start = System.nanoTime();
        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                freshTickets.add(new TicketView(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getString("department_name"),
                        rs.getString("created_by"),
                        rs.getString("assigned_to_name"),
                        rs.getObject("last_updated", LocalDateTime.class),
                        rs.getObject("date_created", LocalDateTime.class),
                        rs.getObject("deadline", LocalDateTime.class)
                ));

            }
            long end = System.nanoTime();
            double elapsedMs = (end - start) / 1_000_000.0;

            System.out.println("Loaded " + freshTickets.size() + " tickets"+ " in "+ elapsedMs+" ms");
            tickets = freshTickets;
            ticketsLoaded = true;
            ticketsDirty = false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<TicketView> getViews() {
        ensureTicketViewsLoaded();
        return new ArrayList<>(tickets);
    }

  public TicketView getTicketViewById(int id) {
    ensureTicketViewsLoaded();

    for (TicketView t : tickets) {
      if (t.getId() == id) {
        return t;
      }
    }
    return null;
  }

}
