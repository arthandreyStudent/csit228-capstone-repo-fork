package com.csit228.capstone.dao;

import com.csit228.capstone.database.DBConnector;
import com.csit228.capstone.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {
  
  private static List<Department> departments;
  private static boolean departmentsLoaded;
  private static DepartmentDAO departmentDAO;
  private final JobDAO jobDAO = JobDAO.getJobDAO();
  
  private DepartmentDAO() {
    departments = new ArrayList<>();
    departmentsLoaded = false;
  }
  
  public List<Department> getDepartments() {
    ensureDepartmentsLoaded();
    return new ArrayList<>(departments);
  }

  public Integer getDepartmentByName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return null;
    }

    ensureDepartmentsLoaded();
    for (Department d : departments) {
      if (d.getName().equalsIgnoreCase(name.trim())) return d.getId();
    }
    return null;
  }
  
  public Department getDepartmentByID(int id) {
    ensureDepartmentsLoaded();
    for (Department d : departments) {
        if (d.getId() == id)
            return d;
    }
    return null;
   }

    public static DepartmentDAO getDepartmentDAO() {
        if (departmentDAO == null) {
            departmentDAO = new DepartmentDAO();
        }
        return departmentDAO;
    }

    public void addDepartment(Department department) {
        try (Connection c = DBConnector.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO department (name, description) VALUES (?,?)")) {
            ps.setString(1, department.getName());
            ps.setString(2, department.getDescription());

            int row = ps.executeUpdate();

            if (row > 0) {
                System.out.println("Added department: " + department.getName());
            } else {
                System.out.println("Unable to add " + department.getName());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (departmentsLoaded) {
            fetchDepartments();
        }
    }

    public void fetchDepartments() {
        departments.clear();
        try (Connection c = DBConnector.getConnection(); Statement s = c.createStatement()) {
            ResultSet resultSet = s.executeQuery("SELECT * from department");

            while (resultSet.next()) {
                departments.add(
                        new Department(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("description")));
            }
            for (Department d : departments) {
                jobDAO.getJobByDepartment(d);
            }
            departmentsLoaded = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureDepartmentsLoaded() {
        if (!departmentsLoaded) {
            fetchDepartments();
        }
    }

}
