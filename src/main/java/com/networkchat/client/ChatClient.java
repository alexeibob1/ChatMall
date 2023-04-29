package com.networkchat.client;

import com.networkchat.server.ChatServerThread;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        Socket socket = null;
        System.out.println("Please, print username: ");
        Scanner scan = new Scanner(System.in);
        String name = scan.nextLine();
        scan.close();
        int portNumber = 4444;
        try {
            socket = new Socket("localhost", portNumber);
            Thread.sleep(1000);
            Thread server = new Thread(new ChatServerThread(socket, name));
            server.start();
        } catch (IOException | InterruptedException e) {
            System.err.println("Fatal connection error!");
            e.printStackTrace();
        }
    }
}
