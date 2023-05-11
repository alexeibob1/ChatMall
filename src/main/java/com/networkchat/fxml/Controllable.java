package com.networkchat.fxml;

import com.networkchat.client.ClientSocket;
import com.networkchat.client.User;
import javafx.stage.Stage;

public interface Controllable {
    void setStage(Stage stage);
    void setStageManager(StageManager stageManager);
    void setSocket(ClientSocket socket);

    void setUsername(String username);
    void init();
}
