package com.networkchat.sql;


import com.networkchat.chat.Message;
import com.networkchat.packets.server.MessageServerPacket;
import com.networkchat.security.SHA256;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SQLConnection {
    private Connection connection;
    private final String DB_ADDRESS = "jdbc:mysql://chatmall.server.com:3306/chatmall";
    private final String ADMIN_USERNAME = "root";
    private final String ADMIN_PASSWORD = "";

    private final String safeNewUserQuery = "INSERT INTO `users_auth` (username, email, salt, password, enabled) " +
                                            "VALUES (?, ?, ?, ?, ?)";

    private final String safeConfirmationCodeQuery = "INSERT INTO `registration_codes` (user_id, code)" +
                                                     "VALUES ((SELECT user_id FROM `users_auth` WHERE username=?), ?)";
    private final String checkUsernameExistenceQuery = "SELECT * FROM `users_auth` WHERE username=? LIMIT 1";

    private final String checkEmailExistenceQuery = "SELECT * FROM `users_auth` WHERE email=? LIMIT 1";

    private final String checkConfirmationCodeQuery = "SELECT user_id FROM `registration_codes` WHERE code=? AND user_id=(" +
            "SELECT user_id FROM `users_auth` WHERE username=? LIMIT 1)";

    private final String updateUserStatusQuery = "UPDATE `users_auth` SET enabled=1 WHERE username=?";
    private final String deleteConfirmationCodeQuery = "DELETE FROM `registration_codes` WHERE user_id=(" +
            "SELECT user_id FROM `users_auth` WHERE username=? LIMIT 1)";

    private final String getSaltQuery = "SELECT salt FROM `users_auth` WHERE username=? LIMIT 1";

    private final String getUserStatusQuery = "SELECT enabled FROM `users_auth` WHERE username=? LIMIT 1";

    private final String getHashedDataQuery = "SELECT password FROM `users_auth` WHERE username=? LIMIT 1";

    private final String saveMessageQuery = "INSERT INTO `messages` (sender_name, receiver_name, message, message_time) VALUES " +
            "(?, ?, ?, ?)";

    public SQLConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection(DB_ADDRESS, ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public SqlResultCode checkNewUserInfo(String username, String email) {
        if (checkUsernameExistence(username) == SqlResultCode.EXISTING_USERNAME) {
            return SqlResultCode.EXISTING_USERNAME;
        }

        try (PreparedStatement stmt = this.connection.prepareStatement(checkEmailExistenceQuery)) {
            stmt.setString(1, email);
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

    public void safeConfirmationCode(String hash, String username) {
        try (PreparedStatement stmt = this.connection.prepareStatement(safeConfirmationCodeQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void safeUserData(String username, String email, String salt, String encryptedData) {
        try (PreparedStatement stmt = this.connection.prepareStatement(safeNewUserQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, salt);
            stmt.setString(4, encryptedData);
            stmt.setByte(5, (byte) 0);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SqlResultCode checkUsernameExistence(String username) {
        try (PreparedStatement stmt = this.connection.prepareStatement(checkUsernameExistenceQuery)) {
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.isBeforeFirst()) {
                return SqlResultCode.EXISTING_USERNAME;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return SqlResultCode.NOT_EXISTING_USERNAME;
    }

    public SqlResultCode checkConfirmationCode(String username, int code) {
        try (PreparedStatement stmt = this.connection.prepareStatement(checkConfirmationCodeQuery)) {
            stmt.setInt(1, code);
            stmt.setString(2, username);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.isBeforeFirst()) {
                try (PreparedStatement updStmt = this.connection.prepareStatement(updateUserStatusQuery)) {
                    updStmt.setString(1, username);
                    updStmt.executeUpdate();
                }
                try (PreparedStatement dltStmt = this.connection.prepareStatement(deleteConfirmationCodeQuery)) {
                    dltStmt.setString(1, username);
                    dltStmt.executeUpdate();
                }
                return SqlResultCode.CORRECT_CODE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return SqlResultCode.WRONG_CODE;
    }

    public int getUserStatus(String username) {
        try (PreparedStatement stmt = this.connection.prepareStatement(getUserStatusQuery)) {
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getByte("enabled");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getSalt(String username) {
        try (PreparedStatement stmt = this.connection.prepareStatement(getSaltQuery)) {
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public SqlResultCode checkPermission(String username, String password) throws NoSuchAlgorithmException {
        if (checkUsernameExistence(username) == SqlResultCode.EXISTING_USERNAME) {
            int userStatus = getUserStatus(username);
            String salt = getSalt(username);
            String hashedData = SHA256.getHashString(salt + username + password);
            String expectedHashedData = getHashedData(username);
            if (Objects.equals(expectedHashedData, hashedData) && userStatus == 1) {
                return SqlResultCode.ALLOW_LOGIN;
            } else if (Objects.equals(expectedHashedData, hashedData) && userStatus == 0) {
                return SqlResultCode.NOT_CONFIRMED;
            }
        }

        return SqlResultCode.ACCESS_DENIED;
    }

    public String getHashedData(String username) {
        try (PreparedStatement stmt = this.connection.prepareStatement(getHashedDataQuery)) {
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void saveMessage(String sender, String receiver, ZonedDateTime dateTime, String message) {
        try (PreparedStatement stmt = this.connection.prepareStatement(saveMessageQuery)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, message);
            stmt.setTimestamp(4, Timestamp.valueOf(dateTime.toLocalDateTime()));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement stmt = this.connection.prepareStatement("SELECT * FROM `messages`")) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String sender = resultSet.getString("sender_name");
                String receiver = resultSet.getString("receiver_name");
                String message = resultSet.getString("message");
                ZonedDateTime timestamp = resultSet.getTimestamp("message_time").toLocalDateTime().atZone(ZoneId.of("Europe/Minsk"));
                Message m = new Message(sender, receiver, timestamp, message);
                messages.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

    public void close() throws SQLException {
        this.connection.close();
    }
}