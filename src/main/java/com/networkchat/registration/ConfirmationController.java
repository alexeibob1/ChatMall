package com.networkchat.registration;

import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import com.networkchat.resources.FxmlView;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ConfirmationController implements Controllable {

    @FXML
    private AnchorPane bckgLogin;

    @FXML
    private ImageView btnClose;

    @FXML
    private Button btnConfirm;

    @FXML
    private ImageView btnMinimize;

    @FXML
    private TextField eConfirmationCode;

    @FXML
    private Label lCheckInbox;

    @FXML
    private Label lCodeSent;

    @FXML
    private Label lValidTime;

    Stage stage;

    StageManager stageManager;
    ClientSocket socket;

    @FXML
    void onBtnCloseClicked(MouseEvent event) {
        stageManager.switchScene(FxmlView.LOGIN, this.socket);
    }

    @FXML
    void onBtnConfirmClicked(MouseEvent event) {

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
    public void init() {
        Scene scene = this.stage.getScene();
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/login.css").toExternalForm());
        eConfirmationCode.setText("");
    }
}
