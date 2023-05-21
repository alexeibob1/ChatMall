package com.networkchat.server;

import com.networkchat.client.ClientSocket;
import com.networkchat.packets.server.ClientInfo;
import com.networkchat.sql.SQLConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;
    private final int serverPort;
    private final Map<ClientSocket, ClientInfo> clients = new HashMap<>();
    private static SQLConnection dbConnection;

    public Server(int serverPort) throws IOException {
        this.serverPort = serverPort;
        serverSocket = new ServerSocket(this.serverPort);
    }

    public void start() {
        try {
            while (true) {
                Socket socket = this.serverSocket.accept();
                ClientSocket clientSocket = new ClientSocket(socket);
                System.out.println("Client connected from IP " + socket.getInetAddress().getHostAddress());
                synchronized (clients) {
                    clients.put(clientSocket, new ClientInfo());
                }
                ClientHandler clientHandler = new ClientHandler(clientSocket, clients, dbConnection);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException {
        try {
            Server server = new Server(43625);
            dbConnection = new SQLConnection();
            server.start();
        } catch (Exception e) {
            dbConnection.close();
            System.err.println("Cannot run server on specified port.");
            e.printStackTrace();
        }
    }
}
