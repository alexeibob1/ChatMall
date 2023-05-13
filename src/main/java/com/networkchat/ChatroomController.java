package com.networkchat;

import com.networkchat.client.ClientSocket;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ChatroomController implements Controllable {

    @FXML
    private Pane bckgHeader;

    @FXML
    private ImageView btnClose;

    @FXML
    private ImageView btnMinimize;

    @FXML
    private Pane pChat;

    @FXML
    private Pane pLeftMenu;

    @FXML
    private Pane pRightMenu;

    Stage stage;
    StageManager stageManager;
    ClientSocket socket;
    String username;

    int[] encryptKey;
    int[] decryptKey;

    @FXML
    void onBtnCloseClicked(MouseEvent event) {

    }

    @FXML
    void onBtnMinimizeClicked(MouseEvent event) {
        this.stage.setIconified(true);
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void setSocket(ClientSocket socket) {
        this.socket = socket;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setEncryptKey(int[] encryptKey) {
        this.encryptKey = encryptKey;
    }

    @Override
    public void setDecryptKey(int[] decryptKey) {
        this.decryptKey = decryptKey;
    }

    @Override
    public void init() {
        Scene scene = this.stage.getScene();
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/chatroom.css").toExternalForm());
    }
}