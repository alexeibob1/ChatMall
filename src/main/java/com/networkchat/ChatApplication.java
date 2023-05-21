package com.networkchat;

import com.networkchat.client.ClientSocket;
import com.networkchat.fxml.StageManager;
import com.networkchat.packets.client.ClientRequest;
import com.networkchat.packets.client.IdeaKeysClientPacket;
import com.networkchat.packets.server.PublicKeyServerPacket;
import com.networkchat.resources.FxmlView;
import com.networkchat.security.idea.Idea;
import com.networkchat.utils.ByteArrayConverter;
import com.networkchat.utils.DialogWindow;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.crypto.Cipher;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            ClientSocket socket = new ClientSocket(new Socket(InetAddress.getByName("DESKTOP-VUO4BRE").getHostAddress(), 43625));
            Idea idea = new Idea();
            PublicKeyServerPacket keyServerPacket = (PublicKeyServerPacket) socket.getIn().readObject();
            PublicKey publicRsaKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyServerPacket.getPublicKey()));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicRsaKey);
            byte[] encryptedEncryptKey = cipher.doFinal(ByteArrayConverter.intArrayToByteArray(idea.getEncryptKey()));
            byte[] encryptedDecryptKey = cipher.doFinal(ByteArrayConverter.intArrayToByteArray(idea.getDecryptKey()));
            socket.getOut().writeUnshared(new IdeaKeysClientPacket(ClientRequest.SEND_IDEA_KEYS, encryptedEncryptKey, encryptedDecryptKey));
            socket.getOut().flush();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            StageManager stageManager = new StageManager(stage, FxmlView.LOGIN);
            stageManager.switchScene(FxmlView.LOGIN, socket, null, idea.getEncryptKey(), idea.getDecryptKey());
        } catch (Exception e) {
            DialogWindow.showDialog(Alert.AlertType.ERROR, "Error", "Can't connect to server", "Server don't respond");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}