package com.csit228.capstone.dao;

import com.csit228.capstone.database.DBConnector;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Job;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobDAO {

  private static List<Job> jobs;
  private static boolean jobsLoaded;
  private static JobDAO jobDAO;

  private JobDAO() {
    jobs = new ArrayList<>();
    jobsLoaded = false;
  }

  public static JobDAO getJobDAO() {
    if (jobDAO == null) {
      jobDAO = new JobDAO();
    }
    return jobDAO;
  }

    public List<Job> getAllJobs(){
        fetchJobs();
        return jobs;
    }

    private void fetchJobs() {
        jobs.clear();
        try (Connection c = DBConnector.getConnection(); Statement s = c.createStatement()) {
            ResultSet resultSet = s.executeQuery("SELECT * FROM job");

      while (resultSet.next()) {
        jobs.add(new Job(resultSet.getInt("id"), resultSet.getString("name")));
        System.out.println("Added " + resultSet.getString("name") + " to Static List");
      }

      jobsLoaded = true;
    } catch (SQLException e) {
      System.out.println("Unable to fetch");
      throw new RuntimeException(e);
    }
  }

  private void addJob(Job job) {
    try (Connection c = DBConnector.getConnection();
         PreparedStatement ps = c.prepareStatement("INSERT INTO job (name) VALUES (?)")) {
      ps.setString(1, job.getName());

      int row = ps.executeUpdate();
      if (row > 0) {
        System.out.println("Added job: " + job.getName());
      } else {
        System.out.println("Unable to add " + job.getName());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (jobsLoaded) {
      fetchJobs();
    }
  }

    private Job searchJob(Job job) {
        fetchJobs();

    for (Job j : jobs) {
      if (j.getName().equalsIgnoreCase(job.getName())) {
        return j;
      }
    }
    return null;
  }

  public void addJobToDepartment(Department department, Job job) {
    // ayaw e add dayun kay masayup ang id number

    // make sure saktu ra id, ma update ang imo ge sulod nga job
    // if wala makitan mag add siya, nya fetch siya sa tanan jobs, then use that newly
    //      added one as the reference, if naa ra siya sa list daan, ang naa sa list iya gamiton

    Job check = searchJob(job);
    if (check != null) {
      job = check;
      System.out.println("Job exists already, using List");
    } else {
      addJob(job);
      job = searchJob(job);
    }

    if (job == null) {
      return;
    }

    try (Connection c = DBConnector.getConnection(); PreparedStatement ps = c.prepareStatement(
      "INSERT INTO job_department (job_id, department_id) " + "SELECT ?, ? FROM DUAL " + "WHERE NOT EXISTS (" +
      "    SELECT 1 FROM job_department WHERE job_id = ? AND department_id = ?" + ")")) {
      ps.setInt(1, job.getId());
      ps.setInt(2, department.getId());
      ps.setInt(3, job.getId());
      ps.setInt(4, department.getId());

      int row = ps.executeUpdate();
      if (row > 0) {
        System.out.println("Added " + job.getName() + " to " + department.getName());
      } else {
        System.out.println("Unable to add " + job.getName() + " to " + department.getName());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

    public void deleteJobFromDepartment(Department department, Job job){
        try(Connection c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("DELETE FROM job_department WHERE job_id = ? AND department_id = ?")){

            ps.setInt(1, job.getId());
            ps.setInt(2, department.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Deleted Job ID " + job.getId() + " from Department " + department.getName());
            }

        } catch (SQLException e) {
            System.out.println("Unable to delete");
            throw new RuntimeException(e);
        }
        getJobByDepartment(department);
    }

    public void getJobByDepartment(Department department) {
        department.getJobs().clear();
        try (Connection c = DBConnector.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT j.id, j.name FROM job j INNER JOIN " +
                        "job_department jd ON j.id = jd.job_id WHERE jd.department_id = ?")) {
            ps.setInt(1, department.getId());

      ResultSet resultSet = ps.executeQuery();

      while (resultSet.next()) {
        department.getJobs().add(new Job(resultSet.getInt("id"), resultSet.getString("name")));
        //                System.out.println("Added " + resultSet.getString("name") + " to " + department.getName());
      }
    } catch (SQLException e) {
      System.out.println("Unable to fetch");
      throw new RuntimeException(e);
    }
  }

  private void ensureJobsLoaded() {
    if (!jobsLoaded) {
      fetchJobs();
    }
  }
}
