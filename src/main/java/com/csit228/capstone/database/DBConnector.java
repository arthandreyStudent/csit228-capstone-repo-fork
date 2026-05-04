package com.csit228.capstone.database;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.sql.Connection;  // Correct JDBC import

public class DBConnector {
    static String url;
    static String user;
    static String password;

    public static Connection getConnection(){
        loadEnv();
        try{
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("CONNECTED");
            return connection;
        } catch (SQLException e) {
            System.out.println("SAD");
            throw new RuntimeException(e);
        }
    }
    private static void loadEnv(){
        try(BufferedReader br = new BufferedReader(new FileReader(".env"))){
            url = br.readLine().trim();
            user= br.readLine().trim();
            password= br.readLine().trim();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        getConnection();
    }
}
