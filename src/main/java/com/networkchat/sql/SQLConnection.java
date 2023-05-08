package com.networkchat.sql;


import com.networkchat.client.User;
import com.networkchat.security.AuthDataEncryptor;
import com.networkchat.smtp.SSLEmail;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class SQLConnection {
    private Connection connection;
    private final String DB_ADDRESS = "jdbc:mysql://localhost:3306/chatmall";
    private final String ADMIN_USERNAME = "root";
    private final String ADMIN_PASSWORD = "";

    //SQL Queries
    //private final String safePrivateKeyQuery = "INSERT INTO `private_keys` (username, rsa_key) VALUES (?, ?)";
    private final String safeNewUserQuery = "INSERT INTO `users_auth` (username, email, salt, password, enabled) " +
                                            "VALUES (?, ?, ?, ?, ?)";

    private final String safeConfirmationCodeQuery = "INSERT INTO `registration_codes` (user_id, code)" +
                                                     "VALUES ((SELECT user_id FROM `users_auth` WHERE username=?), ?)";
    private final String checkUsernameExistenceQuery = "SELECT * FROM `users_auth` WHERE username=? LIMIT 1";

    private final String checkEmailExistenceQuery = "SELECT * FROM `users_auth` WHERE email=? LIMIT 1";

    private final String checkConfirmationCodeQuery = "SELECT user_id FROM `registration_codes` WHERE code=? AND user_id=(" +
            "SELECT user_id FROM `users_auth` WHERE username=?)";

    private final String updateUserStatusQuery = "UPDATE `users_auth` SET enabled=1 WHERE username=?";
    private final String deleteConfirmationCodeQuery = "DELETE FROM `registration_codes` WHERE user_id=(" +
            "SELECT user_id FROM `users_auth` WHERE username=?)";

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
        try (PreparedStatement stmt = this.connection.prepareStatement(checkEmailExistenceQuery)) {
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

    public void sendConfirmationCode(User user) throws NoSuchAlgorithmException {
        String hash = generateVerificationCode();
        SSLEmail emailConnection = new SSLEmail(user);
        emailConnection.sendConfirmationMessage(hash);
        safeConfirmationCode(hash, user);
    }

    private void safeConfirmationCode(String hash, User user) {
        try (PreparedStatement stmt = this.connection.prepareStatement(safeConfirmationCodeQuery)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hash);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        SecureRandom random = new SecureRandom();
        byte[] byteCode = new byte[8];
        random.nextBytes(byteCode);
        byte[] encodedHash = digest.digest(Base64.getEncoder().encodeToString(byteCode).getBytes(StandardCharsets.UTF_8));
        return AuthDataEncryptor.bytesToHex(encodedHash);
    }

//    public void safePrivateKey(String key, String username) {
//        try (PreparedStatement stmt = this.connection.prepareStatement(safePrivateKeyQuery)) {
//            stmt.setString(1, username);
//            stmt.setString(2, key);
//            stmt.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void safeUserData(User user) {
        try (PreparedStatement stmt = this.connection.prepareStatement(safeNewUserQuery)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getSalt());
            stmt.setString(4, user.getEncryptedData());
            stmt.setByte(5, (byte) 0);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SqlResultCode checkUsernameExistence(User user) {
        try (PreparedStatement stmt = this.connection.prepareStatement(checkUsernameExistenceQuery)) {
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

    public SqlResultCode checkConfirmationCode(User user) {
        try (PreparedStatement stmt = this.connection.prepareStatement(checkConfirmationCodeQuery)) {
            stmt.setString(1, user.getConfirmationCode());
            stmt.setString(2, user.getUsername());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.isBeforeFirst()) {
                try (PreparedStatement updStmt = this.connection.prepareStatement(updateUserStatusQuery)) {
                    updStmt.setString(1, user.getUsername());
                    updStmt.executeUpdate();
                }
                try (PreparedStatement dltStmt = this.connection.prepareStatement(deleteConfirmationCodeQuery)) {
                    dltStmt.setString(1, user.getUsername());
                    dltStmt.executeUpdate();
                }
                return SqlResultCode.CORRECT_CODE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return SqlResultCode.WRONG_CODE;
    }

    public String getSalt(String username) {
        return "";

    }

    public void close() throws SQLException {
        this.connection.close();
    }
}
