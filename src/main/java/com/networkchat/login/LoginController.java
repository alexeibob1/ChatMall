package com.networkchat.login;

import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import com.networkchat.packets.client.ClientPacket;
import com.networkchat.packets.client.ClientRequest;
import com.networkchat.packets.client.LoginClientPacket;
import com.networkchat.packets.server.ServerPacket;
import com.networkchat.resources.FxmlView;
import com.networkchat.security.SHA256;
import com.networkchat.security.idea.Idea;
import com.networkchat.utils.DialogWindow;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;

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

    private static final String ALLOWED_PATTERN = "[a-zA-Z0-9@.$\\-+]";

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
        setKeyFilter(eUsername);
        setKeyFilter(ePassword);
    }

    private void setKeyFilter(TextField field) {
        field.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.matches(ALLOWED_PATTERN)) {
                field.setText(newValue.replaceAll("[^" + ALLOWED_PATTERN + "]", ""));
            }
        });
    }

    private void cleanFields() {
        this.ePassword.setText("");
        this.eUsername.setText("");
    }

    @FXML
    void onBtnLoginClicked(MouseEvent event) {
        if (eUsername.getText().isEmpty() || ePassword.getText().isEmpty()) {
            DialogWindow.showDialog(Alert.AlertType.ERROR, "Error!", "Enter text in all fields", "Sorry, invalid input.");
            return;
        }
        try {
            ClientPacket clientPacket = new LoginClientPacket(ClientRequest.LOGIN, eUsername.getText(), SHA256.getHashString(ePassword.getText()));
            Idea idea = new Idea(this.encryptKey, this.decryptKey);
            this.socket.getOut().writeUnshared(idea.crypt(clientPacket.jsonSerialize().getBytes(), true));
            this.socket.getOut().flush();
            byte[] encryptedJson = (byte[]) this.socket.getIn().readObject();
            String decryptedJson = new String(idea.crypt(encryptedJson, false), StandardCharsets.UTF_8);
            ServerPacket serverPacket = ServerPacket.jsonDeserialize(decryptedJson);
            switch (serverPacket.getResponse()) {
                case LOGIN_ALLOWED -> {
                    stageManager.switchScene(FxmlView.CHATROOM, this.socket, eUsername.getText(), encryptKey, decryptKey);
                }
                case LOGIN_DENIED -> {
                    DialogWindow.showDialog(Alert.AlertType.ERROR, "Access denied", "Invalid authentication data", "User with specified data is not found");
                }
                case USER_NOT_CONFIRMED -> {
                    stageManager.switchScene(FxmlView.CONFIRMATION, this.socket, eUsername.getText(), encryptKey, decryptKey);
                }
                case ALREADY_LOGGED_IN -> {
                    DialogWindow.showDialog(Alert.AlertType.ERROR, "Error!", "Already logged in!", "Sorry, but user is already logged in.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
