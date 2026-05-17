package com.csit228.capstone.dao;


import com.csit228.capstone.database.DBConnector;
import com.csit228.capstone.model.Job;
import com.csit228.capstone.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserJobDAO {

    private static UserJobDAO instance;


    public static UserJobDAO getUserJobDao() {
        if (instance == null) {
            instance = new UserJobDAO();
        }
        return instance;
    }


    public String getJobByUser(String username) {
        String job = "";
        String query = "SELECT j.name FROM job j " +
                "JOIN user_job uj ON j.id = uj.job_id " +
                "JOIN user u ON u.id = uj.user_id " +
                "WHERE u.username = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                job = (rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return job;
    }

    public void assignJobToUser(int userId, int jobId) {
        String query = "INSERT INTO user_job (user_id, job_id) VALUES (?, ?)";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, jobId);
            stmt.executeUpdate();
            System.out.println("Job " + jobId + " assigned to User " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Job> getJobsByUserId(int userId) {
        List<Job> userJobs = new ArrayList<>();
        String query = "SELECT j.id, j.name FROM job j " +
                "JOIN user_job uj ON j.id = uj.job_id " +
                "WHERE uj.user_id = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userJobs.add(new Job(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userJobs;
    }

    public void removeJobFromUser(int userId, int jobId) {
        String query = "DELETE FROM user_job WHERE user_id = ? AND job_id = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, jobId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(getUserJobDao().getJobByUser("asmith_ed"));
    }
}