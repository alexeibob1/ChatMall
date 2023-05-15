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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ChatroomController implements Controllable {

    @FXML
    private Pane bckgHeader;

    @FXML
    private Button btnChangeUsername;

    @FXML
    private ImageView btnClose;

    @FXML
    private ImageView btnMinimize;

    @FXML
    private Button btnSend;

    @FXML
    private TextArea eMessage;

    @FXML
    private TextField eNewUsername;

    @FXML
    private GridPane gpMessages;

    @FXML
    private GridPane gpUsers;

    @FXML
    private Label lChatWelcome;

    @FXML
    private Label lUsername;

    @FXML
    private Pane pAccount;

    @FXML
    private ScrollPane spMessages;

    @FXML
    private ScrollPane spUsers;

    Stage stage;
    StageManager stageManager;
    ClientSocket socket;
    String username;

    int[] encryptKey;
    int[] decryptKey;

    @FXML
    void onBtnCloseClicked(MouseEvent event) {
        this.stageManager.switchScene(FxmlView.LOGIN, this.socket, this.username, this.encryptKey, this.decryptKey);
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
        lUsername.setText(this.username);
        eNewUsername.setText("");
        eMessage.setText("");
        gpUsers.getChildren().clear();
        gpMessages.getChildren().clear();
        new Thread(() -> {
            Idea idea = new Idea(this.encryptKey, this.decryptKey);
            ServerPacket serverPacket = null;
            ClientPacket clientPacket = null;
            try {
                while (true) {
                    byte[] encryptedJson = (byte[]) this.socket.getIn().readObject();
                    String decryptedJson = new String(idea.crypt(encryptedJson, false), StandardCharsets.UTF_8);
                    serverPacket = ServerPacket.jsonDeserialize(decryptedJson);
                    switch (serverPacket.getResponse()) {
                        case NEW_USER_CONNECTED -> {
                            Text username = new Text(((UserConnectionServerPacket)serverPacket).getUsername());
                            TextFlow messageFlow = new TextFlow(username);
                            username.getStyleClass().add("username");
                            messageFlow.getStyleClass().addAll("userBlock");
                            messageFlow.setLineSpacing(2);
                            int columnIndex = 1;
                            int columnSpan = 1;
                            int rowIndex = gpUsers.getRowCount();
                            int rowSpan = 1;
                            VBox vBox = new VBox(messageFlow);
                            vBox.setPadding(new Insets(5));
                            Platform.runLater(() -> {
                                gpUsers.add(vBox, columnIndex, rowIndex, columnSpan, rowSpan);
                            });
                            System.out.println(username.getText() + " connected to chat");
                        }
                        case MESSAGE -> {
                            MessageStatus status = ((MessageServerPacket)serverPacket).getMessageStatus();
                            String message = ((MessageServerPacket)serverPacket).getMessage();
                            String sender = ((MessageServerPacket)serverPacket).getSender();
                            ZonedDateTime dateTime = ((MessageServerPacket)serverPacket).getDateTime();
                            switch (status) {
                                case IS_GET -> {
                                    Text messageText = new Text(message);
                                    TextFlow messageFlow = new TextFlow(messageText);
                                    messageText.getStyleClass().add("receivedMessageContent");
                                    messageFlow.getStyleClass().addAll("receivedMessage", "message");
                                    messageFlow.setLineSpacing(2);

                                    Text senderText = new Text(sender);

                                    ZonedDateTime time = dateTime;
                                    Text recordDate = new Text(time.format(DateTimeFormatter.ofPattern("hh : mm a ")));
                                    int columnIndex = 0;
                                    int columnSpan = 2;
                                    int rowIndex = gpMessages.getRowCount();
                                    int rowSpan = 1;
                                    VBox vBox = new VBox(senderText, messageFlow, recordDate);
                                    vBox.setPadding(new Insets(10));
                                    Platform.runLater(() -> {
                                        gpMessages.add(vBox, columnIndex, rowIndex, columnSpan, rowSpan);
                                    });
                                }
                                case IS_SENT -> {
                                    Text messageText = new Text(message);
                                    TextFlow messageFlow = new TextFlow(messageText);
                                    messageText.getStyleClass().add("sendMessageContent");
                                    messageFlow.getStyleClass().addAll("sentMessage", "message");
                                    messageFlow.setLineSpacing(2);

                                    Text senderText = new Text(sender);

                                    ZonedDateTime time = dateTime;
                                    Text recordDate = new Text(time.format(DateTimeFormatter.ofPattern("hh : mm a ")));
                                    int columnIndex = 1;
                                    int columnSpan = 2;
                                    int rowIndex = gpMessages.getRowCount();
                                    int rowSpan = 1;
                                    VBox vBox = new VBox(senderText, messageFlow, recordDate);
                                    vBox.setPadding(new Insets(10));
                                    Platform.runLater(() -> {
                                        gpMessages.add(vBox, columnIndex, rowIndex, columnSpan, rowSpan);
                                    });
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void onBtnSendClicked(MouseEvent event) throws IOException {
        String message = eMessage.getText();
        ClientPacket clientPacket = new MessageClientPacket(ClientRequest.MESSAGE, username, message, ZonedDateTime.now());
        Idea idea = new Idea(this.encryptKey, this.decryptKey);
        this.socket.getOut().writeUnshared(idea.crypt(clientPacket.jsonSerialize().getBytes(), true));
        this.socket.getOut().flush();
    }
}