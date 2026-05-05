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

    private TicketDAO() {
        tickets = new ArrayList<>();
        getTicketViews();
    }

    public boolean assignTicket(int userId, int ticketId){
        String sql = """
                UPDATE ticket SET assigned_to = ? WHERE id = ?;
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1,userId);
            stmt.setInt(2, ticketId);
            int rows = stmt.executeUpdate();
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
                    date_created
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, ticket.getTitle());
            stmt.setString(2, ticket.getDescription());
            stmt.setString(3, ticket.getPriority().toString());
            stmt.setString(4, ticket.getStatus().toString());
            stmt.setInt(5, ticket.getDepartmentId());
            stmt.setInt(6, ticket.getCreatedBy().getId());

            if (ticket.getAssignedTo() != null) {
                stmt.setInt(7, ticket.getAssignedTo().getId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void getTicketViews() {
        String sql = """
                    SELECT
                        t.id,
                        t.title,
                        t.description,
                        t.priority,
                        t.status,
                        d.name AS department_name,
                
                        CONCAT( c.firstname,' ',c.lastname) AS created_by,
                        CONCAT(u.firstname,' ',u.lastname) AS assigned_to_name
                
                        FROM ticket t
                        LEFT JOIN department d ON t.department_id = d.id
                        LEFT JOIN user u ON t.assigned_to = u.id
                        LEFT JOIN user c ON t.created_by = c.id
                        ORDER BY t.date_created DESC
                """;


        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                tickets.add(new TicketView(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getString("department_name"),
                        rs.getString("created_by"),
                        rs.getString("assigned_to_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        TicketDAO ticketDAO = TicketDAO.getTicketDAO();


    }

}
