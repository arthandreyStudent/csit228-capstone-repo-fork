package com.csit228.capstone.dao;

import com.csit228.capstone.database.DBConnector;
import com.csit228.capstone.model.Comment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class CommentDAO {

    private static CommentDAO commentDAO;

    private CommentDAO() {
    }

    public static CommentDAO getCommentDAO() {
        if (commentDAO == null) {
            commentDAO = new CommentDAO();
        }
        return commentDAO;
    }

    public boolean delete(int id) {
        String sql = """
                DELETE FROM comment
                WHERE id = ?;
                """;
        try (Connection connection = DBConnector.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createComment(int userId, int ticketId, String content) {
        String sql = """
                INSERT INTO comment(user_id , ticket_id, content)
                VALUES(?,?,?);
                """;
        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, ticketId);
            stmt.setString(3, content);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Comment> findByTicketId(int id) {
        String sql = """
                SELECT
                   c.id,
                   c.ticket_id,
                   c.content,
                   c.date_created,
                   CONCAT(u.firstname, ' ', u.lastname) AS created_by
                   FROM comment c
                   JOIN user u ON c.user_id =u.id
                   WHERE c.ticket_id = ?
                   ;
                """;
        try (Connection connection = DBConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            List<Comment> res = new ArrayList<>();
            while (rs.next()) {
                res.add(
                        new Comment(rs.getInt("id"), rs.getInt("ticket_id"), rs.getString("content"), rs.getString("created_by"),
                                rs.getObject("date_created", LocalDateTime.class)));
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
