package com.networkchat.server;

import com.networkchat.client.User;
import com.networkchat.security.AuthDataEncryptor;
import com.networkchat.security.KeyDistributor;
import com.networkchat.sql.SQLConnection;
import com.networkchat.sql.SqlResultCode;
import com.networkchat.tooltips.EmailTooltip;
import com.networkchat.tooltips.UsernameTooltip;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private ObjectInputStream in;
    private ObjectOutputStream out;
//    private SQLConnection dbConnection;
    private final Socket socket;

    public ClientHandler(Socket socket) throws IOException, SQLException, ClassNotFoundException {
        this.socket = socket;

    }

    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream())) {
            while (true) {
                User user = (User)in.readObject();
                switch (user.getRequest()) {
                    case REGISTER -> {
                        SQLConnection dbConnection = new SQLConnection();
                        SqlResultCode sqlResult = dbConnection.checkNewUserInfo(user);
                        switch (sqlResult) {
                            case EXISTING_USERNAME -> {
                                out.writeObject(SqlResultCode.EXISTING_USERNAME);
                                out.flush();
                            }
                            case REPEATED_EMAIL -> {
                                out.writeObject(SqlResultCode.REPEATED_EMAIL);
                                out.flush();
                            }
                            case SUCCESS -> {
                                AuthDataEncryptor.encryptRegistrationData(user);
                                dbConnection.safeUserData(user);
                                dbConnection.sendConfirmationCode(user);
                                out.writeObject(SqlResultCode.SUCCESS);
                                out.flush();
                            }
                        }
                        dbConnection.close();
                    } case LOGIN -> {
                        SQLConnection dbConnection = new SQLConnection();
                        SqlResultCode usernameExistence = dbConnection.checkUsernameExistence(user);
                        if (usernameExistence == SqlResultCode.EXISTING_USERNAME) {
                            String salt = dbConnection.getSalt(user.getUsername());
                            PublicKey publicKey = KeyDistributor.getPublicKey();
                        }
                        dbConnection.close();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
