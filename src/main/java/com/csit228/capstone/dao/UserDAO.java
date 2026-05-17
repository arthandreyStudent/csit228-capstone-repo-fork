package com.csit228.capstone.dao;

import com.csit228.capstone.database.DBConnector;
import com.csit228.capstone.exceptions.InvalidCredentialsException;
import com.csit228.capstone.exceptions.UsernameAlreadyTakenException;
import com.csit228.capstone.enums.Role;
import com.csit228.capstone.model.Member;
import com.csit228.capstone.model.User;
import com.csit228.capstone.model.UserFactory;
import com.csit228.capstone.utils.Hash;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {

  private static List<User> users;
  private static List<String> types;
  private static Map<Integer, List<User>> usersByDepartment;
  private static boolean usersLoaded;
  private static boolean typesLoaded;
  private static UserDAO userDAO;

  private UserDAO() {
    users = new ArrayList<>();
    types = new ArrayList<>();
    usersByDepartment = new LinkedHashMap<>();
    usersLoaded = false;
    typesLoaded = false;
  }

  public static UserDAO getUserDAO() {
    if (userDAO == null) {
      userDAO = new UserDAO();
    }
    return userDAO;
  }

  public Role getType(int id) {
    ensureTypesLoaded();

    for (String s : types) {
      String[] res = s.split(" ");
      if (Integer.parseInt(res[0]) == id) {
        return Role.valueOf(res[1].toUpperCase());
      }
    }
    return null;
  }

  public int getTypeRev(Role r) {
    ensureTypesLoaded();

    for (String s : types) {
      String[] res = s.split(" ");
      if (Role.valueOf(res[1].toUpperCase()) == r) {
        return Integer.parseInt(res[0]);
      }
    }
    return -1;
  }

  public void createUser(User u) throws UsernameAlreadyTakenException {
    ensureTypesLoaded();

    String sql =
      "INSERT INTO user(firstname,lastname,username,password_hash,user_type,department_id) VALUES (?,?,?,?,?,?);";
    try (Connection connection = DBConnector.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, u.getFirstName());
      preparedStatement.setString(2, u.getLastName());
      preparedStatement.setString(3, u.getUsername());
      preparedStatement.setString(4, u.getPasswordHash());
      preparedStatement.setInt(5, getTypeRev(u.getRole()));
      preparedStatement.setInt(6, u.getDepartment_id());
      int rows = preparedStatement.executeUpdate();
      if (rows > 0) {
        System.out.println("added user " + u);
      } else {
        System.out.println("User not added");
      }
    } catch (SQLIntegrityConstraintViolationException e) {
      throw new UsernameAlreadyTakenException();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (usersLoaded) {
      fetchUsers();
    }
  }

  public static void fetchTypes() {
    if (types == null) {
      types = new ArrayList<>();
    } else {
      types.clear();
    }
    
    try (Connection connection = DBConnector.getConnection(); Statement statement = connection.createStatement()) {
      ResultSet rs = statement.executeQuery("SELECT * FROM type  ORDER BY id ASC");
      while (rs.next()) {
        types.add(rs.getInt("id") + " " + rs.getString("role"));
      }

      typesLoaded = true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  public User getUser(int id) {
    ensureUsersLoaded();

    for (User u : users) {
      if (u.getUserId() == id) {
        return u;
      }
    }
    return null;
  }
  
  public User getUserById(int id) {
    return getUser(id);
  }
  
  public User getUserByName(String fullname) {
    ensureUsersLoaded();
    for (User u : users) {
      if (u.getFullName().equals(fullname)) {
        return u;
      }
    }
    return null;
  }
  
  public List<User> getMembers(){
    ensureUsersLoaded();
    List<User> members = new ArrayList<>();
    for (User u : users) {
      if (u instanceof Member) {
        members.add(u);
      }
    }
    return members;
  }
  
  public List<User> getMembersByDepartment(int id){
    ensureUsersLoaded();
    List<User> listMembers = new ArrayList<>();
    for (User user : getUsersByDepartment(id)) {
      if (user instanceof Member) {
        listMembers.add(user);
      }
    }
    return listMembers;
  }
  
  public List<User> getUserByDepartment(int id) {
    return getUsersByDepartment(id);
  }
  
  public User login(String username, String password) throws InvalidCredentialsException {
    ensureUsersLoaded();
    String h = Hash.hashWithSHA256(password.trim());
    for (User u : users) {
      if (u.getUsername().trim().equals(username.trim()) && u.getPasswordHash().equals(h)) {
        return u;
      }
    }
    throw new InvalidCredentialsException();
  }
  
  public void fetchUsers() {
    users.clear(); // Clear existing to prevent duplicates on refresh
    try (Connection connection = DBConnector.getConnection();
         Statement statement = connection.createStatement()) {
      ResultSet rs = statement.executeQuery("SELECT * FROM user");
      while (rs.next()) {
        Role role = getType(rs.getInt("user_type"));
        User curr = UserFactory.createUser(role, rs.getInt("id"), rs.getString("firstname"), rs.getString("lastname"),
                                           rs.getString("username"), rs.getString("password_hash"),
                                           rs.getInt("department_id"));
        users.add(curr);
      }
      rebuildDepartmentCache();
      usersLoaded = true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  public List<User> getUsers() {
    ensureUsersLoaded();
    return new ArrayList<>(users);
  }
  
  public String getJobNameByUserId(int userId) {
    String sql = "SELECT j.name FROM user_job uj INNER JOIN job j ON uj.job_id = j.id WHERE uj.user_id = ? LIMIT 1";
    try (Connection connection = DBConnector.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setInt(1, userId);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getString("name");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void ensureUsersLoaded() {
    if (!usersLoaded) {
      fetchUsers();
    }
  }

  private void ensureTypesLoaded() {
    if (!typesLoaded) {
      fetchTypes();
    }
  }
  
  private void rebuildDepartmentCache() {
    usersByDepartment.clear();
    for (User user : users) {
      usersByDepartment.computeIfAbsent(user.getDepartment_id(), key -> new ArrayList<>()).add(user);
    }
  }
}
