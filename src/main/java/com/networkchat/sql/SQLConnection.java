package com.networkchat.sql;


import com.networkchat.client.User;

import java.sql.*;

public class SQLConnection {
    private Connection connection;
    private final String DB_ADDRESS = "jdbc:mysql://localhost:3306/chatmall";
    private final String ADMIN_USERNAME = "root";
    private final String ADMIN_PASSWORD = "";
    public SQLConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection(DB_ADDRESS, ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public SqlResultCode checkNewUserInfo(User user) {
        //check for existing username
        if (checkUsernameExistence(user) == SqlResultCode.EXISTING_USERNAME) {
            return SqlResultCode.EXISTING_USERNAME;
        }

        //check for existing email
        try (PreparedStatement stmt = this.connection.prepareStatement("SELECT * FROM users_auth WHERE email=? LIMIT 1")) {
            stmt.setString(1, user.getEmail());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.isBeforeFirst()) {
                return SqlResultCode.REPEATED_EMAIL;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return SqlResultCode.SUCCESS;
    }

    public void sendConfirmationCode() {

    }

    public void safePrivateKey(String key, String username) {
        try (PreparedStatement stmt = this.connection.prepareStatement(
                "INSERT INTO `private_keys` (username, rsa_key) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, key);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void safeUserData(User user) {
        try (PreparedStatement stmt = this.connection.prepareStatement(
                "INSERT INTO `users_auth` (user_id, email, salt, password, time_stamp, enabled) " +
                        "VALUES ((SELECT k.user_id FROM `private_keys` k WHERE k.username = ?), ?, ?, ?, ?, ?)"
        )) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getSalt());
            stmt.setString(4, user.getEncryptedData());
            stmt.setTimestamp(5, Timestamp.valueOf(user.getTimeStamp()));
            stmt.setByte(6, (byte) 0);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SqlResultCode checkUsernameExistence(User user) {
        try (PreparedStatement stmt = this.connection.prepareStatement("SELECT * FROM private_keys WHERE username=? LIMIT 1")) {
            stmt.setString(1, user.getUsername());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.isBeforeFirst()) {
                return SqlResultCode.EXISTING_USERNAME;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return SqlResultCode.NOT_EXISTING_USERNAME;
    }

    public String getSalt(String username) {
        return "";
    }
}
