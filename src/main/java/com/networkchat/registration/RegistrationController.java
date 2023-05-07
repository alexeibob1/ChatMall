package com.networkchat.registration;

import com.networkchat.ChatApplication;
import com.networkchat.client.User;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import com.networkchat.resources.FxmlView;
import com.networkchat.security.AuthDataEncryptor;
import com.networkchat.security.KeyDistributor;
import com.networkchat.sql.SQLConnection;
import com.networkchat.sql.SqlResultCode;
import com.networkchat.tooltips.EmailTooltip;
import com.networkchat.tooltips.UsernameTooltip;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.time.LocalDateTime;

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

    Stage stage;

    StageManager stageManager;

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
        this.stageManager.switchScene(FxmlView.LOGIN);
    }

    @FXML
    void onBtnSignUpClicked(MouseEvent event) {
        removeTooltips();
        try {
            SQLConnection dbConnection = new SQLConnection();
            User user = new User(eUsername.getText(), eEmail.getText(), ePassword.getText(), LocalDateTime.now());
            SqlResultCode sqlResult = dbConnection.checkNewUserInfo(user);
            switch (sqlResult) {
                case EXISTING_USERNAME -> eUsername.setTooltip(UsernameTooltip.getTooltip());
                case REPEATED_EMAIL -> eEmail.setTooltip(EmailTooltip.getTooltip());
                case SUCCESS -> {
                    KeyDistributor.generateKeys(user, dbConnection);
                    AuthDataEncryptor.encryptRegistrationData(user);
                    dbConnection.safeUserData(user);
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
    public void init() {
        Scene scene = this.stage.getScene();
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/login.css").toExternalForm());
        cleanFields();
        ePassword.setText("pupsik");
        eEmail.setText("masha@gmail.com");
        eUsername.setText("masha");
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
    }
}