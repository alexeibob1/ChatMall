package com.networkchat.registration;

import com.networkchat.ChatApplication;

import com.networkchat.client.User;
import com.networkchat.fxml.Controllable;
import com.networkchat.resources.FxmlView;
import com.networkchat.fxml.StageManager;
import com.networkchat.security.AuthDataEncryptor;
import com.networkchat.security.KeyDistributor;
import com.networkchat.sql.SqlUserErrorCode;
import com.networkchat.tooltips.EmailTooltip;
import com.networkchat.tooltips.UsernameTooltip;
import com.networkchat.sql.SQLConnection;
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

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

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
//            Cipher encryptCipher = Cipher.getInstance("RSA");
//            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            byte[] encryptedPassword = encryptCipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
//            String encodedPassword = Base64.getEncoder().encodeToString(encryptedPassword);
//            int l = encodedPassword.length();
            SQLConnection dbConnection = new SQLConnection();
            User user = new User(eUsername.getText(), eEmail.getText(), ePassword.getText(), new Date());
            SqlUserErrorCode sqlResult = dbConnection.checkNewUserInfo(user);
            switch (sqlResult) {
                case REPEATED_USERNAME -> eUsername.setTooltip(UsernameTooltip.getTooltip());
                case REPEATED_EMAIL -> eEmail.setTooltip(EmailTooltip.getTooltip());
                case SUCCESS -> {
                    KeyDistributor.generateKeys(user, dbConnection);
                    AuthDataEncryptor.encryptUserData(user, dbConnection);
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