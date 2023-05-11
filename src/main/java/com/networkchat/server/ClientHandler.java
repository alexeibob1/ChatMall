package com.networkchat.server;

import com.networkchat.packets.client.ClientPacket;
import com.networkchat.packets.client.IdeaKeysClientPacket;
import com.networkchat.packets.client.RegistrationClientPacket;
import com.networkchat.packets.server.PublicKeyServerPacket;
import com.networkchat.packets.server.ServerPacket;
import com.networkchat.packets.server.ServerResponse;
import com.networkchat.security.KeyDistributor;
import com.networkchat.sql.SQLConnection;
import com.networkchat.sql.SqlResultCode;
import com.networkchat.utils.ByteArrayConverter;

import javax.crypto.Cipher;
import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        SQLConnection dbConnection = null;
        ClientPacket clientPacket = null;

        try {
            dbConnection = new SQLConnection();
        } catch (Exception e) {
            System.err.println("Unable to connect to server database");
            e.printStackTrace();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream())) {

            KeyPair keyPair = KeyDistributor.getKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            out.writeUnshared(new PublicKeyServerPacket(ServerResponse.OPEN_RSA_KEY, publicKey.getEncoded()));
            out.flush();

            clientPacket = (IdeaKeysClientPacket) in.readObject();

            Cipher decipher = Cipher.getInstance("RSA");
            decipher.init(Cipher.DECRYPT_MODE, privateKey);
            int[] encryptionByteKey = ByteArrayConverter.byteArrayToIntArray(decipher.doFinal(((IdeaKeysClientPacket) clientPacket).getEncryptKey()));
            int[] decryptionByteKey = ByteArrayConverter.byteArrayToIntArray(decipher.doFinal(((IdeaKeysClientPacket) clientPacket).getDecryptKey()));

            //process requests from user
            while (true) {
                clientPacket = (ClientPacket) in.readObject();

                switch (clientPacket.getRequest()) {
                    case REGISTER -> {
                        SqlResultCode sqlResult = dbConnection.checkNewUserInfo(((RegistrationClientPacket) clientPacket).getUsername(), ((RegistrationClientPacket) clientPacket).getEmail());
                        switch (sqlResult) {
                            case EXISTING_USERNAME -> {
                                out.writeUnshared(new ServerPacket(ServerResponse.EXISTING_USERNAME));
                                out.flush();
                            }
                            case REPEATED_EMAIL -> {
                                out.writeUnshared(new ServerPacket(ServerResponse.REPEATED_EMAIL));
                                out.flush();
                            }
                            case SUCCESS -> {
//                                AuthDataEncryptor.encryptRegistrationData(user);
//                                dbConnection.safeUserData(user);
//                                out.writeObject(SqlResultCode.SUCCESS);
//                                out.flush();
//                                dbConnection.sendConfirmationCode(user);
                            }
                        }
                        dbConnection.close();
                    } case LOGIN -> {

                       // SqlResultCode accessPermission = dbConnection.checkPermission(user);
//                        switch (accessPermission) {
//                            case ACCESS_DENIED -> {
//                                out.writeObject(SqlResultCode.ACCESS_DENIED);
//                                out.flush();
//                            }
//                            case NOT_CONFIRMED -> {
//                                out.writeObject(SqlResultCode.NOT_CONFIRMED);
//                                out.flush();
//                            }
//                            case ALLOW_LOGIN -> {
//                                out.writeObject(SqlResultCode.ALLOW_LOGIN);
//                                out.flush();
//                            }
//                        }
                        dbConnection.close();
                    } case CONFIRM_REGISTRATION -> {
//                        if (user.getEmail() == "") {
//                            user.setEmail(dbConnection.getEmail(user.getUsername()));
//                        }
//                        SqlResultCode codeCorrectness = dbConnection.checkConfirmationCode(user);
//                        out.writeObject(codeCorrectness);
                        out.flush();

                    }
                }

            }
        } catch (Exception e) {
            try {
                assert dbConnection != null;
                dbConnection.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}