package com.networkchat.server;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Base64;

public class Server {
    private ServerSocket serverSocket;
    private final int serverPort;

    public Server(int serverPort) throws IOException {
        this.serverPort = serverPort;
        serverSocket = new ServerSocket(this.serverPort);
    }

    public void start() {
        try {
            while (true) {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Client connected from IP " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            System.out.println("Server was shut down!");
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
