package com.networkchat.server;

import com.networkchat.client.ChatClientThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    static ServerSocket serverSocket;
    static int portNumber;
    public static List<ChatClientThread> clients;

    public static void acceptClients() {
        clients = new ArrayList<>();
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ChatClientThread client = new ChatClientThread(socket);
                Thread thread = new Thread(client);
                thread.start();
                clients.add(client);
            } catch (IOException e) {
                System.err.println("Accept failed on: " + portNumber);
            }
        }
    }
    public static void main(String[] args) {
        portNumber = 4444;
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNumber);
            acceptClients();
        } catch (IOException e) {
            System.err.println("Can't listen on port " + portNumber);
            System.exit(1);
        }
    }
}
