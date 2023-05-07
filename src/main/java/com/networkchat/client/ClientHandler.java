package com.networkchat.client;

import com.networkchat.sql.SQLConnection;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SQLConnection dbConnection;
    private final Socket socket;

    public ClientHandler(Socket socket) throws IOException, SQLException, ClassNotFoundException {
        this.socket = socket;

    }

    public void run() {

        try {
            this.dbConnection = new SQLConnection();
        } catch (Exception e) {
            System.err.println("Can't connect to database.");
            e.printStackTrace();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream())) {
            while (true) {
                User user = (User)in.readObject();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
