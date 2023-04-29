package com.networkchat.sql;
import java.sql.*;

public class SQLConnection {
    private Connection connection;
    private final String DB_ADDRESS = "jdbc:mysql://localhost:3306/chatmall";
    private final String ADMIN_USERNAME = "root";
    private final String ADMIN_PASSWORD = "";
    public SQLConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_ADDRESS, ADMIN_USERNAME, ADMIN_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean createNewUser(String username, String password) {
        return false;
    }
}
