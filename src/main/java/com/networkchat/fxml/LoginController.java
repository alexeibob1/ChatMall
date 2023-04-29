package com.networkchat.fxml;

import com.networkchat.ChatApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class LoginController implements Controllable {

    @FXML
    private AnchorPane bckgLogin;

    @FXML
    private Label lWelcome;

    @FXML
    private ImageView btnClose;

    @FXML
    private ImageView btnMinimize;

    @FXML
    private TextField eUsername;
    @FXML
    private PasswordField ePassword;

    Stage stage;
    StageManager stageManager;

    @FXML
    void onBtnCloseClicked(MouseEvent event) {
        this.stage.close();
    }

    @FXML
    void onBtnMinimizeClicked(MouseEvent event) {
        this.stage.setIconified(true);
    }

    @FXML
    void onFormDragEntered(MouseEvent event) {
        this.stageManager.onFormDragEntered(event);
    }

    @FXML
    void onMousePressed(MouseEvent event) {
        this.stageManager.onMousePressed(event);
    }

    @FXML
    void onRegisterBtnClicked(MouseEvent event) {
        this.stageManager.switchScene(FxmlView.REGISTRATION);
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
    public void init() {
        Scene scene = this.stage.getScene();
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/login.css").toExternalForm());
        cleanFields();
    }

    private void cleanFields() {
        this.ePassword.setText("");
        this.eUsername.setText("");
    }
}
