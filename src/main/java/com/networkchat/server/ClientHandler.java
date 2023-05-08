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
                                out.writeObject(SqlResultCode.SUCCESS);
                                out.flush();
                                dbConnection.sendConfirmationCode(user);
                            }
                        }
                        dbConnection.close();
                    } case LOGIN -> {
                        SQLConnection dbConnection = new SQLConnection();

                        SqlResultCode accessPermission = dbConnection.checkPermission(user);
                        switch (accessPermission) {
                            case ACCESS_DENIED -> {
                                out.writeObject(SqlResultCode.ACCESS_DENIED);
                                out.flush();
                            }
                            case NOT_CONFIRMED -> {
                                out.writeObject(SqlResultCode.NOT_CONFIRMED);
                                out.flush();
                            }
                            case ALLOW_LOGIN -> {
                                out.writeObject(SqlResultCode.ALLOW_LOGIN);
                                out.flush();
                            }
                        }
                        dbConnection.close();
                    } case CONFIRM_REGISTRATION -> {
                        SQLConnection dbConnection = new SQLConnection();
                        if (user.getEmail() == "") {
                            user.setEmail(dbConnection.getEmail(user.getUsername()));
                        }
                        SqlResultCode codeCorrectness = dbConnection.checkConfirmationCode(user);
                        out.writeObject(codeCorrectness);
                        out.flush();
                        dbConnection.close();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
