package com.networkchat.server;

import com.networkchat.client.User;
import com.networkchat.security.AuthDataEncryptor;
import com.networkchat.security.KeyDistributor;
import com.networkchat.security.idea.Idea;
import com.networkchat.security.idea.IdeaKeys;
import com.networkchat.sql.SQLConnection;
import com.networkchat.sql.SqlResultCode;

import javax.crypto.Cipher;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private ObjectInputStream in;
    private ObjectOutputStream out;
//    private SQLConnection dbConnection;
    private final Socket socket;

    public ClientHandler(Socket socket) throws IOException, SQLException, ClassNotFoundException {
        this.socket = socket;
    }

    public void run() {
        //generate connection identifier
        UUID uuid = UUID.randomUUID();
        String connectionID = uuid.toString();
        SQLConnection dbConnection;


        try (ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream())) {

            //generate RSA key
            KeyPair keyPair = KeyDistributor.getKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            out.writeObject(publicKey);
            out.flush();

            //read keys from client
            byte[] encryptedEncryptKey = (byte[])in.readObject();


            Cipher decipher = Cipher.getInstance("RSA");
            decipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedEncrypted = decipher.doFinal(encryptedEncryptKey);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decryptedEncrypted);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            int[] finalIdeaEncryptKey = new int[decryptedEncrypted.length / 4];
            int i = 0;
            while (intBuffer.hasRemaining()) {
                finalIdeaEncryptKey[i++] = intBuffer.get();
            }



            dbConnection = new SQLConnection();
            //dbConnection.safePublicKey(connectionID, rsaKey);
            dbConnection.close();


            //process requests from user
            while (true) {
                User user = (User)in.readObject();
                switch (user.getRequest()) {
                    case REGISTER -> {
                        dbConnection = new SQLConnection();
                        SqlResultCode sqlResult = dbConnection.checkNewUserInfo(user);
                        switch (sqlResult) {
                            case EXISTING_USERNAME -> {
                                out.writeObject(SqlResultCode.EXISTING_USERNAME);
                                out.flush();
                            }
                            case REPEATED_EMAIL -> {
                                out.writeObject(SqlResultCode.REPEATED_EMAIL);
                                out.flush();
                            }
                            case SUCCESS -> {
                                AuthDataEncryptor.encryptRegistrationData(user);
                                dbConnection.safeUserData(user);
                                out.writeObject(SqlResultCode.SUCCESS);
                                out.flush();
                                dbConnection.sendConfirmationCode(user);
                            }
                        }
                        dbConnection.close();
                    } case LOGIN -> {
                        dbConnection = new SQLConnection();

                        SqlResultCode accessPermission = dbConnection.checkPermission(user);
                        switch (accessPermission) {
                            case ACCESS_DENIED -> {
                                out.writeObject(SqlResultCode.ACCESS_DENIED);
                                out.flush();
                            }
                            case NOT_CONFIRMED -> {
                                out.writeObject(SqlResultCode.NOT_CONFIRMED);
                                out.flush();
                            }
                            case ALLOW_LOGIN -> {
                                out.writeObject(SqlResultCode.ALLOW_LOGIN);
                                out.flush();
                            }
                        }
                        dbConnection.close();
                    } case CONFIRM_REGISTRATION -> {
                        dbConnection = new SQLConnection();
                        if (user.getEmail() == "") {
                            user.setEmail(dbConnection.getEmail(user.getUsername()));
                        }
                        SqlResultCode codeCorrectness = dbConnection.checkConfirmationCode(user);
                        out.writeObject(codeCorrectness);
                        out.flush();
                        dbConnection.close();
                    }
                }

            }
        } catch (Exception e) {
            try {
                dbConnection = new SQLConnection();
                dbConnection.deletePublicKey(connectionID);
                dbConnection.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            //e.printStackTrace();
        }
    }
}
