package com.networkchat;

import com.networkchat.resources.FxmlView;
import com.networkchat.fxml.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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