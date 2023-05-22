package com.networkchat.chat;

import com.networkchat.ChatApplication;
import com.networkchat.client.ClientSocket;
import com.networkchat.fxml.Controllable;
import com.networkchat.fxml.StageManager;
import com.networkchat.packets.client.ClientPacket;
import com.networkchat.packets.client.ClientRequest;
import com.networkchat.packets.client.MessageClientPacket;
import com.networkchat.packets.server.MessageServerPacket;
import com.networkchat.packets.server.MessageStatus;
import com.networkchat.packets.server.ServerPacket;
import com.networkchat.packets.server.UserConnectionServerPacket;
import com.networkchat.resources.FxmlView;
import com.networkchat.security.idea.Idea;
import com.networkchat.utils.DialogWindow;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class ChatroomController implements Controllable {

    @FXML
    private Pane bckgHeader;

    @FXML
    private ImageView btnClose;

    @FXML
    private ImageView btnMinimize;

    @FXML
    private Button btnSend;

    @FXML
    private TextArea eMessage;

    @FXML
    private GridPane gpMessages;

    @FXML
    private GridPane gpUsers;

    @FXML
    private Label lUsers;

    @FXML
    private ScrollPane spMessages;

    @FXML
    private ScrollPane spUsers;

    private final KeyCombination keyCombinationCtrlEnter = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
    private final KeyCombination keyCombinationShiftEnter = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHIFT_DOWN);

    Stage stage;
    StageManager stageManager;
    ClientSocket socket;
    String username;

    int[] encryptKey;
    int[] decryptKey;

    @FXML
    void onBtnCloseClicked(MouseEvent event) throws IOException {
        Optional<ButtonType> dlgRes = DialogWindow.showDialog(Alert.AlertType.CONFIRMATION, "Logout", "You want to exit?", "Please, press OK if you want to leave chat room");
        if (dlgRes.isPresent() && dlgRes.get() == ButtonType.OK) {
            ClientPacket clientPacket = new ClientPacket(ClientRequest.DISCONNECT);
            Idea idea = new Idea(this.encryptKey, this.decryptKey);
            this.socket.getOut().writeUnshared(idea.crypt(clientPacket.jsonSerialize().getBytes(), true));
            this.socket.getOut().flush();
            this.stageManager.switchScene(FxmlView.LOGIN, this.socket, this.username, this.encryptKey, this.decryptKey);
        }
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
        scene.getStylesheets().add(ChatApplication.class.getResource("styles/chatroom.css").toExternalForm());
        eMessage.setText("");
        gpUsers.getChildren().clear();
        gpMessages.getChildren().clear();
        eMessage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
            }
        });
        new Thread(() -> {
            Idea idea = new Idea(this.encryptKey, this.decryptKey);
            ServerPacket serverPacket;
            try {
                while (true) {
                    byte[] encryptedJson = (byte[]) this.socket.getIn().readObject();
                    String decryptedJson = new String(idea.crypt(encryptedJson, false), StandardCharsets.UTF_8);
                    serverPacket = ServerPacket.jsonDeserialize(decryptedJson);
                    switch (serverPacket.getResponse()) {
                        case NEW_USER_CONNECTED -> {
                            updateUsersList(((UserConnectionServerPacket)serverPacket).getUsernames());
                        }
                        case MESSAGE -> {
                            processMessage((MessageServerPacket)serverPacket);
                        }
                        case DISCONNECTED -> {
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void processMessage(MessageServerPacket serverPacket) {
        String message = serverPacket.getMessage();
        if (!message.isEmpty()) {
            MessageStatus status = serverPacket.getMessageStatus();
            String sender = serverPacket.getSender();
            ZonedDateTime dateTime = serverPacket.getDateTime();
            Text messageText = new Text(message);
            TextFlow messageFlow = new TextFlow(messageText);
            messageText.getStyleClass().add("messageContent");
            messageFlow.setLineSpacing(2);
            messageFlow.getStyleClass().add("message");
            Text recordDate = new Text(dateTime.format(DateTimeFormatter.ofPattern("hh : mm a ")));
            recordDate.getStyleClass().add("date");
            switch (status) {
                case IS_PERSONAL_GET, IS_GET -> {
                    Text senderText = new Text(sender);
                    senderText.getStyleClass().add("sender");
                    messageFlow.getStyleClass().add(status == MessageStatus.IS_PERSONAL_GET ? "receivedPersonalMessage" : "receivedMessage");
                    VBox vBox = new VBox(senderText, messageFlow, recordDate);
                    vBox.setPadding(new Insets(5));
                    Platform.runLater(() -> {
                        gpMessages.add(vBox, 0, gpMessages.getRowCount(), 2, 1);
                    });
                }
                case IS_SENT -> {
                    messageFlow.getStyleClass().addAll("sentMessage");
                    VBox vBox = new VBox(messageFlow, recordDate);
                    vBox.setPadding(new Insets(5));
                    Platform.runLater(() -> {
                        gpMessages.add(vBox, 1, gpMessages.getRowCount(), 2, 1);
                        spMessages.applyCss();
                        spMessages.layout();
                        spMessages.setVvalue(1.0);
                    });
                }
            }
        }
    }

    private void updateUsersList(ArrayList<String> usernames) {
        Platform.runLater(() -> {
            gpUsers.getChildren().clear();
            for (String username : usernames) {
                Text usernameText = new Text(username);
                TextFlow messageFlow = new TextFlow(usernameText);
                usernameText.getStyleClass().add("username");
                messageFlow.getStyleClass().add("userBlock");
                if (Objects.equals(this.username, username)) {
                    messageFlow.getStyleClass().add("userMe");
                } else {
                    messageFlow.getStyleClass().add("userNotMe");
                }
                messageFlow.setLineSpacing(2);
                VBox vBox = new VBox(messageFlow);
                vBox.setPadding(new Insets(5));
                gpUsers.add(vBox, 1, gpUsers.getRowCount(), 1, 1);
            }
        });
    }

    @FXML
    void onBtnSendClicked(MouseEvent event) {
        sendMessage(eMessage.getText());
        eMessage.setText("");
        Platform.runLater(() -> spUsers.setVvalue(1.0));
    }

    @FXML
    void onEnterKeyPressed(KeyEvent event) {
        if (keyCombinationCtrlEnter.match(event) || keyCombinationShiftEnter.match(event)) {
            eMessage.appendText("\n");
        }
        else if (event.getCode() == KeyCode.ENTER) {
            sendMessage(eMessage.getText());
            eMessage.setText("");
            Platform.runLater(() -> {
                spUsers.applyCss();
                spUsers.layout();
                spUsers.setVvalue(0.0);
            });
        }
    }

    private void sendMessage(String message) {
        message = message.trim();
        try {
            if (!message.isEmpty()) {
                ClientPacket clientPacket = new MessageClientPacket(ClientRequest.MESSAGE, username, message, ZonedDateTime.now(Clock.system(ZoneId.of("Europe/Minsk"))));
                Idea idea = new Idea(this.encryptKey, this.decryptKey);
                this.socket.getOut().writeUnshared(idea.crypt(clientPacket.jsonSerialize().getBytes(), true));
                this.socket.getOut().flush();
            }
        } catch (Exception e) {
            DialogWindow.showDialog(Alert.AlertType.ERROR, "Error", "Can't send message", "Unexpected error happened during sending message!");
        }
    }
}