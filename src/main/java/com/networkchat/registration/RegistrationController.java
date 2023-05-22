package com.networkchat.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import com.networkchat.packets.client.ClientPacket;
import com.networkchat.packets.client.ClientRequest;
import com.networkchat.packets.client.RegistrationClientPacket;
import com.networkchat.packets.server.ServerPacket;
import com.networkchat.resources.FxmlView;
import com.networkchat.security.SHA256;
import com.networkchat.security.idea.Idea;
import com.networkchat.tooltips.EmailTooltip;
import com.networkchat.tooltips.UsernameTooltip;
import com.networkchat.utils.DialogWindow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class RegistrationController implements Controllable {

    @FXML
    private AnchorPane bckgLogin;

    @FXML
    private ImageView btnClose;

    @FXML
    private ImageView btnMinimize;

    @FXML
    private Button btnSignUp;


    @FXML
    private TextField eEmail;

    @FXML
    private PasswordField ePassword;

    @FXML
    private TextField eUsername;

    @FXML
    private Label lMember;

    @FXML
    private Label lSignIn;

    @FXML
    private Label lWelcome;
    private String errorStyle = "-fx-border-radius: 5px;\n" +
            "-fx-border-color: red;\n" +
            "-fx-border-width: 2px;";

    private static final String ALLOWED_PATTERN = "[a-zA-Z0-9@.$\\-+]";

    Stage stage;
    StageManager stageManager;
    ClientSocket socket;
    String username;

    int[] encryptKey;
    int[] decryptKey;

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
    void onBtnSignInClicked(MouseEvent event) {
        this.stageManager.switchScene(FxmlView.LOGIN, this.socket, null, encryptKey, decryptKey);
    }

    @FXML
    void onBtnSignUpClicked(MouseEvent event) {
        removeTooltips();
        if (eUsername.getText().isEmpty() || ePassword.getText().isEmpty() || eEmail.getText().isEmpty()) {
            DialogWindow.showDialog(Alert.AlertType.ERROR, "Error!", "Enter text in all fields", "Sorry, invalid input.");
            return;
        }
        try {
            registerUser();
            byte[] encryptedJson = (byte[]) this.socket.getIn().readObject();
            Idea idea = new Idea(encryptKey, decryptKey);
            String decryptedJson = new String(idea.crypt(encryptedJson, false), StandardCharsets.UTF_8);

            ServerPacket serverPacket = ServerPacket.jsonDeserialize(decryptedJson);

            switch (serverPacket.getResponse()) {
                case EXISTING_USERNAME -> {
                    eUsername.setTooltip(UsernameTooltip.getTooltip());
                    eUsername.setStyle(eUsername.getStyle() + errorStyle);
                }
                case REPEATED_EMAIL -> {
                    eEmail.setTooltip(EmailTooltip.getTooltip());
                    eEmail.setStyle(eUsername.getStyle() + errorStyle);
                }
                case SUCCESSFUL_REGISTRATION -> stageManager.switchScene(FxmlView.CONFIRMATION, this.socket, eUsername.getText(), encryptKey, decryptKey);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerUser() throws IOException, NoSuchAlgorithmException {
        ClientPacket clientPacket = new RegistrationClientPacket(ClientRequest.REGISTER, eUsername.getText(), eEmail.getText(), SHA256.getHashString(ePassword.getText()));
        Idea idea = new Idea(encryptKey, decryptKey);
        this.socket.getOut().writeUnshared(idea.crypt(clientPacket.jsonSerialize().getBytes(), true));
        this.socket.getOut().flush();
    }


    @FXML
    void onBtnCloseClicked(MouseEvent event) {
        this.stage.close();
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
        setKeyFilter(eEmail);
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
        this.eEmail.setText("");
        this.removeTooltips();
    }

    private void removeTooltips() {
        this.eUsername.setTooltip(null);
        this.ePassword.setTooltip(null);
        this.eEmail.setTooltip(null);
        eUsername.setStyle(eUsername.getStyle().replaceAll(errorStyle, ""));
        eEmail.setStyle(eUsername.getStyle().replaceAll(errorStyle, ""));
    }
}