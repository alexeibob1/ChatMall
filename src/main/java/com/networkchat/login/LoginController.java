package com.networkchat.login;

import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.client.User;
import com.networkchat.fxml.Controllable;
import com.networkchat.resources.FxmlView;
import com.networkchat.fxml.StageManager;
import com.networkchat.packets.client.ClientRequest;
import com.networkchat.sql.SqlResultCode;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;

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
    ClientSocket socket;
    String username;

    int[] encryptKey;
    int[] decryptKey;

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
        this.stageManager.switchScene(FxmlView.REGISTRATION, this.socket, null, encryptKey, decryptKey);
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
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/login.css").toExternalForm());
        cleanFields();
    }

    private void cleanFields() {
        this.ePassword.setText("");
        this.eUsername.setText("");
    }

    @FXML
    void onBtnLoginClicked(MouseEvent event) {
        try {
            User user = new User(eUsername.getText(), ePassword.getText());
            user.setRequest(ClientRequest.LOGIN);
            this.socket.getOut().writeUnshared(user);
            this.socket.getOut().flush();

            Object response = this.socket.getIn().readObject();

            if (response.getClass() == SqlResultCode.class) {
                SqlResultCode resultCode = (SqlResultCode) response;
                switch (resultCode) {
                    case ALLOW_LOGIN -> {
                        System.out.println("Access allowed");
                    }
                    case ACCESS_DENIED -> {
                        System.out.println("Incorrect authentication data.");
                    }
                    case NOT_CONFIRMED -> {
                        user.setPassword("");
                        user.setEmail("");
                        stageManager.switchScene(FxmlView.CONFIRMATION, this.socket, username, encryptKey, decryptKey);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
