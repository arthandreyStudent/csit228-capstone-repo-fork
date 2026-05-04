package com.csit228.capstone.dao;

import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import com.csit228.capstone.database.DBConnector;
import com.csit228.capstone.exceptions.InvalidCredentialsException;
import com.csit228.capstone.exceptions.UsernameAlreadyTakenException;
import com.csit228.capstone.model.Member;
import com.csit228.capstone.model.Role;
import com.csit228.capstone.model.User;
import com.csit228.capstone.model.UserFactory;
import com.csit228.capstone.utils.Hash;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Object.*;

public class UserDAO {
    private static List<User> users;
    private static  List<String> types;

    private static UserDAO userDAO;
//
//    public UserDAO(){
//        users = new ArrayList<>();
//        users.add((User)new Member(1,"juan","delacruz","bayan","strongpass",1));
//    }
    private UserDAO(){
        users=new ArrayList<>();
        types=new ArrayList<>();
        fetchTypes();
        fetchUsers();
    }

    public static UserDAO getUserDAO(){
        if(userDAO==null){
            userDAO = new UserDAO();
        }
        return userDAO;
    }

    public  Role getType(int id){
        for(String s : types){
            String[] res = s.split(" ");
            if(Integer.parseInt(res[0])==id){
                return Role.valueOf(res[1].toUpperCase());
            }
        }
        return null;
    }

    public  int getTypeRev(Role r){
        for(String s : types){
            String[] res = s.split(" ");
            if(Role.valueOf(res[1].toUpperCase())==r){
                return  Integer.parseInt(res[0]);
            }
        }
        return -1;
    }

    public  void createUser(User u) throws UsernameAlreadyTakenException {
        String sql = "INSERT INTO user(firstname,lastname,username,password_hash,user_type,department_id) VALUES (?,?,?,?,?,?);";
        try(Connection connection = DBConnector.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1,u.getFirstname());
            preparedStatement.setString(2,u.getLastname());
            preparedStatement.setString(3,u.getUsername());
            preparedStatement.setString(4,u.getPasswordHash());
            preparedStatement.setInt(5,getTypeRev(u.getRole()));
            preparedStatement.setInt(6,u.getDepartment_id());
            int rows = preparedStatement.executeUpdate();
            if(rows>0){
                System.out.println("added user "+ u.toString());
            }
        }catch (SQLIntegrityConstraintViolationException e){
           throw  new UsernameAlreadyTakenException();
        }catch (SQLException e){
            e.printStackTrace();
        }
        fetchUsers();
    }




    public static void fetchTypes(){

        try(Connection connection = DBConnector.getConnection();

            Statement statement = connection.createStatement();
            ){

            ResultSet rs = statement.executeQuery("SELECT * FROM type  ORDER BY id ASC");
            while(rs.next()){
                types.add(
                        rs.getInt("id") +" "+rs.getString("role")
                );
            }


        }catch( SQLException e){
            e.printStackTrace();
        }
    }

    //int userId, String firstname, String lastname, String username, String passwordHash, Role

    public User getUser(int id) {
        if(users==null){
            fetchUsers();
        }

        for(User u :  users){
            if(u.getUserId() == id){
                return u;
            }
        }
        return null;
    }


    public User getUserById(int id) {
        if(users==null){
            fetchUsers();
        }

        for(User u :  users){
            if(u.getUserId() == id){
                return u;
            }
        }
        return null;
    }

    public List<User> getUserByDepartment(int id) {
        List<User> res = new ArrayList<>();
        if(users==null){
            fetchUsers();
        }
        for(User u :  users){
            if(u.getDepartment_id()== id){
                res.add(u);
            }
        }
        return res;
    }

    public User login(String username, String password) throws InvalidCredentialsException {

            String h = Hash.hashWithSHA256(password.trim());
        System.out.println(h);
            for(User u : users){
                System.out.println(u.getPasswordHash());
                if(u.getUsername().trim().equals(username.trim())  && u.getPasswordHash().equals(h) ){
                    System.out.println("ASDAS");
                    return u;
                }
            }

            throw new InvalidCredentialsException();


    }


    public  void fetchUsers()  {
        users = new ArrayList<>();
        try(Connection connection = DBConnector.getConnection()
           ;Statement statement = connection.createStatement();){

            ResultSet rs =statement.executeQuery("SELECT * FROM user");
            while(rs.next()){

                Role role = getType(rs.getInt("user_type"));
                User curr = UserFactory.createUser(
                        role,
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getInt("department_id")
                        );
                users.add(curr);

            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InvalidCredentialsException {
//        Role r= getType(2);
//        System.out.println(r);
//        fetchUsers();
//
//        for (User u : users){
//            System.out.println(u);
//            System.out.println(u.getRole());;
//        }
//        //int userId, String firstName, String lastName, String username, String passwordHash, Role role, int department_id) {
        //Member m = new Member(68,"priwnce","tag","jake",Hash.hashWithSHA256("123"),1);
//

//            UserDAO ud= UserDAO.getUserDAO();
////            try{
////                ud.createUser(m);
////            }catch (UsernameAlreadyTakenException e){
////                System.out.println(e.getMessage());
////            }
////System.out.println(Hash.hashWithSHA256("123"));
//            User curr = ud.login("jake","123");
//
//            if(curr!=null){
//                System.out.println(curr + "is the user");
//            }else{
//                System.out.println("No user");
//            }
//        List<User> curr =ud.getUserByDepartment(1);
//        for(User u : curr){
//            System.out.println(u);
//        }
    }
}
