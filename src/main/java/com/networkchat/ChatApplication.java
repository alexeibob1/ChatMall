package com.networkchat;

import com.networkchat.fxml.FxmlView;
import com.networkchat.fxml.LoginController;
import com.networkchat.fxml.StageManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);

        StageManager stageManager = new StageManager(stage, FxmlView.LOGIN);
        stageManager.switchScene(FxmlView.LOGIN);
    }

    public static void main(String[] args) {
        launch();
    }
}