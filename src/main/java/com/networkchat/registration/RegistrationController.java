package com.networkchat.registration;

import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.client.User;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import com.networkchat.packets.client.ClientPacket;
import com.networkchat.packets.client.RegistrationClientPacket;
import com.networkchat.packets.server.ServerPacket;
import com.networkchat.resources.FxmlView;
import com.networkchat.packets.client.ClientRequest;
import com.networkchat.security.SHA256;
import com.networkchat.sql.SqlResultCode;
import com.networkchat.tooltips.EmailTooltip;
import com.networkchat.tooltips.UsernameTooltip;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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

    Stage stage;


    StageManager stageManager;
    ClientSocket socket;

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
        this.stageManager.switchScene(FxmlView.LOGIN, this.socket, null);
    }

    @FXML
    void onBtnSignUpClicked(MouseEvent event) {
        removeTooltips();
        try {
            ClientPacket clientPacket = new RegistrationClientPacket(ClientRequest.REGISTER, eUsername.getText(), eEmail.getText(), SHA256.getHashString(ePassword.getText()));
            this.socket.getOut().writeUnshared(clientPacket);
            this.socket.getOut().flush();

            String encryptedJson = (String) this.socket.getIn().readObject();

            //!!remember to decrypt
            String decryptedJson = encryptedJson;

            ServerPacket serverPacket = this.socket.getIn().readObject();

            if (response.getClass() == SqlResultCode.class) {
                SqlResultCode resultCode = (SqlResultCode) response;
                switch (resultCode) {
                    case EXISTING_USERNAME -> {
                        eUsername.setTooltip(UsernameTooltip.getTooltip());
                        eUsername.setStyle(eUsername.getStyle() + errorStyle);
                    }
                    case REPEATED_EMAIL -> {
                        eEmail.setTooltip(EmailTooltip.getTooltip());
                        eEmail.setStyle(eUsername.getStyle() + errorStyle);
                    }
                    case SUCCESS -> {
                        stageManager.switchScene(FxmlView.CONFIRMATION, this.socket, user);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void init() {
        Scene scene = this.stage.getScene();
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/login.css").toExternalForm());
        cleanFields();
        ePassword.setText("12345678");
        eEmail.setText("funnyguylo0937@gmail.com");
        eUsername.setText("alexeibob");
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