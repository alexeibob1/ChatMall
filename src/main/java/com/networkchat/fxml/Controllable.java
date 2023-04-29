package com.networkchat.fxml;

import javafx.stage.Stage;

public interface Controllable {
    void setStage(Stage stage);
    void setStageManager(StageManager stageManager);
    void init();
}
