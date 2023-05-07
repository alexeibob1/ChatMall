package com.networkchat.fxml;

import com.networkchat.client.ClientSocket;
import javafx.stage.Stage;

public interface Controllable {
    void setStage(Stage stage);
    void setStageManager(StageManager stageManager);
    void setSocket(ClientSocket socket);
    void init();
}
