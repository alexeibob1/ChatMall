package com.networkchat.registration;

import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.client.User;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import com.networkchat.resources.FxmlView;
import com.networkchat.server.ClientRequest;
import com.networkchat.sql.SqlResultCode;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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

    Stage stage;

    StageManager stageManager;
    ClientSocket socket;
    User user;

    @FXML
    void onBtnCloseClicked(MouseEvent event) {
        stageManager.switchScene(FxmlView.LOGIN, this.socket, null);
    }

    @FXML
    void onBtnConfirmClicked(MouseEvent event) {
        try {
            user.setRequest(ClientRequest.CONFIRM_REGISTRATION);
            user.setPassword("");
            user.setConfirmationCode(eConfirmationCode.getText());
            this.socket.getOut().writeUnshared(user);
            this.socket.getOut().flush();

            Object response = this.socket.getIn().readObject();

            if (response.getClass() == SqlResultCode.class) {
                SqlResultCode resultCode = (SqlResultCode) response;
                if (resultCode == SqlResultCode.WRONG_CODE) {
                    System.err.println("WRONG CODE!");
                } else if (resultCode == SqlResultCode.CORRECT_CODE) {
                    System.out.println("CORRECT CODE!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void init() {
        Scene scene = this.stage.getScene();
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/login.css").toExternalForm());
        eConfirmationCode.setText("");
    }
}
