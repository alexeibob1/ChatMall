package com.networkchat;

import com.networkchat.client.ClientSocket;
import com.networkchat.fxml.StageManager;
import com.networkchat.resources.FxmlView;
import com.networkchat.security.idea.Idea;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.PublicKey;
import java.security.SecureRandom;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            ClientSocket socket = new ClientSocket(new Socket("localhost", 4000));

            //generate IDEA key
            SecureRandom random = new SecureRandom();
            byte[] initVector = new byte[16];
            random.nextBytes(initVector);
            Idea idea = new Idea(initVector);

            //IdeaKeys keys = new IdeaKeys(idea.getStrEncryptKeys(), idea.getStrDecryptKeys());

            PublicKey rsaKey = (PublicKey) socket.getIn().readObject();

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, rsaKey);


            //compare this!!!
            int[] encrKey = idea.getEncryptKeys();

            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * encrKey.length);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(encrKey);

            byte[] encrByteKey = byteBuffer.array();

            byte[] encryptedEncryptKey = cipher.doFinal(encrByteKey);





            socket.getOut().writeUnshared(encryptedEncryptKey);
            socket.getOut().flush();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);


            StageManager stageManager = new StageManager(stage, FxmlView.LOGIN);
            stageManager.switchScene(FxmlView.LOGIN, socket, null);
        } catch (Exception e) {
            System.err.println("Can't connect to server.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}