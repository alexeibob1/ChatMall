package com.networkchat.server;

import com.networkchat.client.ClientSocket;
import com.networkchat.client.ClientStatus;
import com.networkchat.packets.server.ClientInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;
    private final int serverPort;
    private final Map<ClientSocket, ClientInfo> clients = new HashMap<>();

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
                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            System.out.println("Error happened in Server");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(4000);
            server.start();
        } catch (Exception e) {
            System.err.println("Cannot run server on specified port.");
            e.printStackTrace();
        }
    }
}
