package com.networkchat.server;

import com.networkchat.client.ClientSocket;
import com.networkchat.client.ClientStatus;
import com.networkchat.packets.client.*;
import com.networkchat.packets.server.*;
import com.networkchat.security.KeyDistributor;
import com.networkchat.security.RandStringGenerator;
import com.networkchat.security.SHA256;
import com.networkchat.security.idea.Idea;
import com.networkchat.smtp.SSLEmail;
import com.networkchat.sql.SQLConnection;
import com.networkchat.sql.SqlResultCode;
import com.networkchat.utils.ByteArrayConverter;

import javax.crypto.Cipher;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

public class ClientHandler implements Runnable {
    private final ClientSocket socket;

    private final Map<ClientSocket, ClientInfo> clients;

    public ClientHandler(ClientSocket socket, Map<ClientSocket, ClientInfo> clients) {
        this.socket = socket;
        this.clients = clients;
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

        try (ObjectOutputStream out = this.socket.getOut();
             ObjectInputStream in = this.socket.getIn()) {

            KeyPair keyPair = KeyDistributor.getKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            out.writeUnshared(new PublicKeyServerPacket(ServerResponse.OPEN_RSA_KEY, publicKey.getEncoded()));
            out.flush();

            clientPacket = (IdeaKeysClientPacket) in.readObject();

            Cipher decipher = Cipher.getInstance("RSA");
            decipher.init(Cipher.DECRYPT_MODE, privateKey);
            int[] encryptionKey = ByteArrayConverter.byteArrayToIntArray(decipher.doFinal(((IdeaKeysClientPacket) clientPacket).getEncryptKey()));
            int[] decryptionKey = ByteArrayConverter.byteArrayToIntArray(decipher.doFinal(((IdeaKeysClientPacket) clientPacket).getDecryptKey()));
            Idea idea = new Idea(encryptionKey, decryptionKey);
            clients.put(this.socket, new ClientInfo(ClientStatus.NOT_LOGGED_IN, "", encryptionKey, decryptionKey));

            while (true) {
                byte[] encryptedJson = (byte[]) in.readObject();
                String decryptedJson = new String(idea.crypt(encryptedJson, false), StandardCharsets.UTF_8);
                clientPacket = ClientPacket.jsonDeserialize(decryptedJson);

                switch (clientPacket.getRequest()) {
                    case REGISTER -> {
                        String username = ((RegistrationClientPacket) clientPacket).getUsername();
                        String email = ((RegistrationClientPacket) clientPacket).getEmail();
                        String password = ((RegistrationClientPacket) clientPacket).getPassword();
                        SqlResultCode sqlResult = dbConnection.checkNewUserInfo(username, email);
                        switch (sqlResult) {
                            case EXISTING_USERNAME -> {
                                out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.EXISTING_USERNAME).jsonSerialize().getBytes(), true));
                                out.flush();
                            }
                            case REPEATED_EMAIL -> {
                                out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.REPEATED_EMAIL).jsonSerialize().getBytes(), true));
                                out.flush();
                            }
                            case SUCCESS -> {
                                String salt = RandStringGenerator.generateSalt();
                                String encryptedData = SHA256.getHashString(salt + username + password);
                                dbConnection.safeUserData(username, email, salt, encryptedData);
                                out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.SUCCESSFUL_REGISTRATION).jsonSerialize().getBytes(), true));
                                out.flush();
                                String hash = RandStringGenerator.generateVerificationCode();
                                SSLEmail emailConnection = new SSLEmail(username, email);
                                emailConnection.sendConfirmationMessage(hash);
                                dbConnection.safeConfirmationCode(hash, username);
                            }
                        }
                    }
                    case LOGIN -> {
                        String username = ((LoginClientPacket) clientPacket).getUsername();
                        String password = ((LoginClientPacket) clientPacket).getPassword();
                        SqlResultCode accessPermission = dbConnection.checkPermission(username, password);
                        switch (accessPermission) {
                            case ACCESS_DENIED -> {
                                out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.LOGIN_DENIED).jsonSerialize().getBytes(), true));
                                out.flush();
                            }
                            case NOT_CONFIRMED -> {
                                out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.USER_NOT_CONFIRMED).jsonSerialize().getBytes(), true));
                                out.flush();
                            }
                            case ALLOW_LOGIN -> {
                                out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.LOGIN_ALLOWED).jsonSerialize().getBytes(), true));
                                out.flush();
                                synchronized (clients) {
                                    clients.get(this.socket).setStatus(ClientStatus.LOGGED_IN);
                                    clients.get(this.socket).setUsername(username);
                                    for (ClientSocket s : clients.keySet()) {
                                        if (clients.get(s).getStatus() == ClientStatus.LOGGED_IN) {
                                            Idea clientCipher = new Idea(clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey());
                                            s.getOut().writeUnshared(clientCipher.crypt(new UserConnectionServerPacket(ServerResponse.NEW_USER_CONNECTED, username)
                                                    .jsonSerialize().getBytes(), true));
                                            s.getOut().flush();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    case CONFIRM_REGISTRATION -> {
                        String username = ((ConfirmationClientPacket) clientPacket).getUsername();
                        int code = ((ConfirmationClientPacket) clientPacket).getCode();
                        SqlResultCode codeCorrectness = dbConnection.checkConfirmationCode(username, code);
                        switch (codeCorrectness) {
                            case WRONG_CODE -> {
                                out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.INVALID_CODE).jsonSerialize().getBytes(), true));
                                out.flush();
                            }
                            case CORRECT_CODE -> {
                                out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.VALID_CODE).jsonSerialize().getBytes(), true));
                                out.flush();
                            }
                        }
                    } case MESSAGE -> {
                        String sender = ((MessageClientPacket) clientPacket).getSender();
                        String message = ((MessageClientPacket) clientPacket).getMessage();
                        ZonedDateTime dateTime = ((MessageClientPacket) clientPacket).getDateTime();
                        MessageServerPacket serverPacket = new MessageServerPacket(ServerResponse.MESSAGE, sender, message, dateTime);
                        synchronized (clients) {
                            for (ClientSocket s : clients.keySet()) {
                                if (Objects.equals(clients.get(s).getUsername(), sender)) {
                                    serverPacket.setMessageStatus(MessageStatus.IS_SENT);
                                } else {
                                    serverPacket.setMessageStatus(MessageStatus.IS_GET);
                                }
                                Idea clientCipher = new Idea(clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey());
                                s.getOut().writeUnshared(clientCipher.crypt(serverPacket.jsonSerialize().getBytes(), true));
                                s.getOut().flush();
                            }
                        }
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