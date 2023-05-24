package com.networkchat.registration;

import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import com.networkchat.packets.client.ClientPacket;
import com.networkchat.packets.client.ClientRequest;
import com.networkchat.packets.client.ConfirmationClientPacket;
import com.networkchat.packets.server.ServerPacket;
import com.networkchat.resources.FxmlView;
import com.networkchat.security.idea.Idea;
import com.networkchat.utils.DialogWindow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;

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

    private final String errorStyle = "-fx-border-radius: 5px;\n" +
            "-fx-border-color: red;\n" +
            "-fx-border-width: 2px;";

    private final String correctStyle = "-fx-border-radius: 5px;\n" +
            "-fx-border-color: green;\n" +
            "-fx-border-width: 2px;";

    private final int CODE_LENGTH = 6;

    Stage stage;

    StageManager stageManager;
    ClientSocket socket;
    String username;

    int[] encryptKey;
    int[] decryptKey;
    @FXML
    void onBtnCloseClicked(MouseEvent event) {
        stageManager.switchScene(FxmlView.LOGIN, this.socket, null, encryptKey, decryptKey);
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
    void onBtnConfirmClicked(MouseEvent event) {
        eConfirmationCode.setStyle(eConfirmationCode.getStyle().replaceAll(errorStyle, ""));
        checkConfirmationCode(eConfirmationCode.getText());
    }

    private void checkConfirmationCode(String strCode) {
        try {
            int confirmationCode;
            try {
                confirmationCode = Integer.parseInt(strCode);
            } catch (Exception e) {
                eConfirmationCode.setStyle(eConfirmationCode.getStyle() + errorStyle);
                return;
            }
            ClientPacket clientPacket = new ConfirmationClientPacket(ClientRequest.CONFIRM_REGISTRATION, this.username, confirmationCode);
            this.socket.getOut().writeUnshared(Idea.crypt(clientPacket.jsonSerialize(), true, this.encryptKey, this.decryptKey));
            this.socket.getOut().flush();
//            byte[] encryptedJson = (byte[]) this.socket.getIn().readObject();
//            String decryptedJson = new String(Idea.crypt(encryptedJson, false, this.encryptKey, this.decryptKey), StandardCharsets.UTF_8);
            ServerPacket serverPacket = ServerPacket.jsonDeserialize(Idea.crypt((byte[]) this.socket.getIn().readObject(), false, this.encryptKey, this.decryptKey));
            switch (serverPacket.getResponse()) {
                case INVALID_CODE -> {
                    eConfirmationCode.setStyle(eConfirmationCode.getStyle() + errorStyle);
                }
                case VALID_CODE -> {
                    DialogWindow.showDialog(Alert.AlertType.INFORMATION, "Successful registration", "You have submitted your registration", "Now you can sign in and start chatting!");
                    stageManager.switchScene(FxmlView.LOGIN, this.socket, null, encryptKey, decryptKey);
                }
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                DialogWindow.showDialog(Alert.AlertType.ERROR, "Error", "Server was shutdown", "Unexpected server error happened!");
                this.stage.close();
            });
        }
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
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/login.css").toExternalForm());
        eConfirmationCode.setText("");
        eConfirmationCode.setStyle(eConfirmationCode.getStyle().replaceAll(errorStyle, ""));
        eConfirmationCode.setStyle(eConfirmationCode.getStyle().replaceAll(correctStyle, ""));
        eConfirmationCode.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (eConfirmationCode.getText().length() > CODE_LENGTH) {
                String s = eConfirmationCode.getText().substring(0, CODE_LENGTH);
                eConfirmationCode.setText(s);
            }
        });
    }
}