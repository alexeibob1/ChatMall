package com.networkchat.login;

import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.client.User;
import com.networkchat.fxml.Controllable;
import com.networkchat.resources.FxmlView;
import com.networkchat.fxml.StageManager;
import com.networkchat.security.KeyDistributor;
import com.networkchat.server.ClientRequest;
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
import java.net.Socket;

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
    User user;

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
        this.stageManager.switchScene(FxmlView.REGISTRATION, this.socket, null);
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
            this.socket.getOut().writeObject(user);
            this.socket.getOut().flush();

            //обратиться к БД и, если пользователь с таким именем есть, идти дальше, иначе ОСТАНОВ
            //обратиться к БД и взять соль, зашифрованный пароль+логин+соль
            //дешифровать приватным ключом с сервера полученное ранее сообщение (пароль+логин+соль), перевести это в строку
            //зашифровать введённый пользователем соль+логин+пароль публичным ключом из файла
            //сравнить 2 строки
            Object response = this.socket.getIn().readObject();




        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
