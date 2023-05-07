package com.networkchat.login;

import com.networkchat.ChatApplication;
import com.networkchat.client.User;
import com.networkchat.fxml.Controllable;
import com.networkchat.resources.FxmlView;
import com.networkchat.fxml.StageManager;
import com.networkchat.security.KeyDistributor;
import com.networkchat.sql.SQLConnection;
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

import java.io.File;
import java.io.FileInputStream;

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

    @FXML
    void onBtnLoginClicked(MouseEvent event) {
        try {
            User user = new User(eUsername.getText(), ePassword.getText());

            //обратиться к БД и, если пользователь с таким именем есть, идти дальше, иначе ОСТАНОВ
            //обратиться к БД и взять соль, зашифрованный пароль+логин+соль
            //дешифровать приватным ключом с сервера полученное ранее сообщение (пароль+логин+соль), перевести это в строку
            //зашифровать введённый пользователем соль+логин+пароль публичным ключом из файла
            //сравнить 2 строки
            SQLConnection dbConnection = new SQLConnection();
            SqlResultCode usernameExistence = dbConnection.checkUsernameExistence(user);
            if (usernameExistence == SqlResultCode.EXISTING_USERNAME) {
                user.setPublicKey(KeyDistributor.getPublicKey());
                user.setSalt(dbConnection.getSalt(user.getUsername()));
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
