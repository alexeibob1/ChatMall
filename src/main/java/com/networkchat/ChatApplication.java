package com.networkchat;

import com.networkchat.resources.FxmlView;
import com.networkchat.fxml.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);

        StageManager stageManager = new StageManager(stage, FxmlView.LOGIN);
        stageManager.switchScene(FxmlView.LOGIN);
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 4000);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.err.println("Can't connect to server.");
            e.printStackTrace();
        }
        launch();
    }
}