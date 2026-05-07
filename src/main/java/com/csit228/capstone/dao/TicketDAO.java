package com.csit228.capstone.dao;

import com.csit228.capstone.database.DBConnector;

import com.csit228.capstone.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    private static TicketDAO ticketDAO;
    private static List<TicketView> tickets;
    private static boolean ticketsLoaded;
    private static boolean ticketsDirty;

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
                    date_created,
                    last_updated,
                    deadline        
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, ticket.getTitle());
            stmt.setString(2, ticket.getDescription());
            stmt.setString(3, ticket.getPriority().toString());
            stmt.setString(4, ticket.getStatus().toString());
            stmt.setInt(5, ticket.getDepartmentId());
            stmt.setInt(6, ticket.getCreatedBy());
            if (ticket.getAssignedTo() == null) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, ticket.getAssignedTo());
            }
            if (ticket.getDateCreated() == null) {
                stmt.setNull(8, Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(8, Timestamp.valueOf(ticket.getDateCreated()));
            }
            if (ticket.getLastUpdated() == null) {
                stmt.setNull(9, Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(9, Timestamp.valueOf(ticket.getLastUpdated()));
            }

            // --- ADDED THE DEADLINE BINDING HERE (Index 10) ---
            if (ticket.getDeadline() == null) {
                stmt.setNull(10, Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(10, Timestamp.valueOf(ticket.getDeadline()));
            }

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

    public void getTicketViews() {
        refreshTicketViews();
    }

    private void refreshTicketViews() {
        tickets.clear();

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
                d.name AS department_name,
                CONCAT(c.firstname, ' ', c.lastname) AS created_by,
                CONCAT(u.firstname, ' ', u.lastname) AS assigned_to_name
            FROM ticket t
            LEFT JOIN department d ON t.department_id = d.id
            LEFT JOIN user u ON t.assigned_to = u.id
            LEFT JOIN user c ON t.created_by = c.id
            ORDER BY t.date_created DESC
            """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tickets.add(new TicketView(
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

            ticketsLoaded = true;
            ticketsDirty = false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<TicketView> getViews(){
        ensureTicketViewsLoaded();
        return tickets;
    }
    public static void main(String[] args) {

//        TicketDAO ticketDAO = TicketDAO.getTicketDAO();
//        Ticket t = ticketDAO.getTicketById(4);
//        List<TicketView> tv =ticketDAO.getViews();
//        for (TicketView e : tv){
//            System.out.println(e);
//        }
//        System.out.println(t);


//        Ticket t = new Ticket(
//            0,
//            "Ken Horigome BDAY Caption",
//            "Make a bday caption for KEN HORIGOME",
//            TicketPriority.HIGH,
//            LocalDateTime.now(),
//            TicketStatus.OPEN,
//            13,
//            1,
//            LocalDateTime.now(),
//            null,
//            1
//        );
//
        TicketDAO td = getTicketDAO();
        td.getTicketViews();
    }

    private void ensureTicketViewsLoaded() {
        if (!ticketsLoaded || ticketsDirty) {
            refreshTicketViews();
        }
    }

}



