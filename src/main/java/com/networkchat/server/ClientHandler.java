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
import com.networkchat.utils.DialogWindow;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ClientHandler implements Runnable {
    private final ClientSocket socket;

    private final Map<ClientSocket, ClientInfo> clients;
    private final SQLConnection dbConnection;

    public ClientHandler(ClientSocket socket, Map<ClientSocket, ClientInfo> clients, SQLConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.socket = socket;
        this.clients = clients;
    }

    public void run() {
        ClientPacket clientPacket;
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
                                SSLEmail emailConnection = new SSLEmail(username, email);
                                String hash = RandStringGenerator.generateVerificationCode();
                                boolean errorFlag = false;
                                try {
                                    emailConnection.sendConfirmationMessage(hash);
                                } catch (Exception e) {
                                    out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.REPEATED_EMAIL).jsonSerialize().getBytes(), true));
                                    out.flush();
                                    errorFlag = true;
                                }

                                if (!errorFlag) {
                                    dbConnection.safeUserData(username, email, salt, encryptedData);
                                    out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.SUCCESSFUL_REGISTRATION).jsonSerialize().getBytes(), true));
                                    out.flush();
                                    dbConnection.safeConfirmationCode(hash, username);
                                }
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
                                ArrayList<String> usernames = getListOfLoggedUsernames();
                                if (!usernames.contains(username)) {
                                    synchronized (clients) {
                                        clients.get(this.socket).setStatus(ClientStatus.LOGGED_IN);
                                        clients.get(this.socket).setUsername(username);
                                    }
                                    out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.LOGIN_ALLOWED).jsonSerialize().getBytes(), true));
                                    out.flush();
                                    usernames.add(username);
                                    broadcastUsersList(usernames);
                                } else {
                                    out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.ALREADY_LOGGED_IN).jsonSerialize().getBytes(), true));
                                    out.flush();
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
                    }
                    case MESSAGE -> {
                        String sender = ((MessageClientPacket) clientPacket).getSender();
                        String message = ((MessageClientPacket) clientPacket).getMessage();
                        ZonedDateTime dateTime = ((MessageClientPacket) clientPacket).getDateTime();
                        ArrayList<String> usernames = getListOfLoggedUsernames();
                        boolean isPersonalMessage = false;
                        String personalGetter = "";
                        if (message.trim().startsWith("@") && usernames.contains(message.trim().split(" ")[0].trim().replaceAll("@", ""))) {
                            personalGetter = message.split(" ")[0].trim().replaceAll("@", "");
                            if (!personalGetter.equals(clients.get(socket).getUsername())) {
                                isPersonalMessage = true;
                            }
                        }
                        if (isPersonalMessage) {
                            String tempMessage = message;
                            message = message.replaceFirst("@" + personalGetter, "").trim();
                            if (!message.isEmpty()) {
                                synchronized (clients) {
                                    for (ClientSocket s : clients.keySet()) {
                                        if (Objects.equals(clients.get(s).getUsername(), personalGetter)) {
                                            MessageServerPacket serverPacket = new MessageServerPacket(ServerResponse.MESSAGE, sender, message, dateTime);
                                            serverPacket.setMessageStatus(MessageStatus.IS_PERSONAL_GET);
                                            Idea clientCipher = new Idea(clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey());
                                            s.getOut().writeUnshared(clientCipher.crypt(serverPacket.jsonSerialize().getBytes(), true));
                                            s.getOut().flush();
                                        } else if (Objects.equals(clients.get(s).getUsername(), sender)) {
                                            MessageServerPacket serverPacket = new MessageServerPacket(ServerResponse.MESSAGE, sender, tempMessage.trim(), dateTime);
                                            serverPacket.setMessageStatus(MessageStatus.IS_SENT);
                                            Idea clientCipher = new Idea(clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey());
                                            s.getOut().writeUnshared(clientCipher.crypt(serverPacket.jsonSerialize().getBytes(), true));
                                            s.getOut().flush();
                                        }
                                    }
                                }
                            }
                        } else {
                            MessageServerPacket serverPacket = new MessageServerPacket(ServerResponse.MESSAGE, sender, message, dateTime);
                            synchronized (clients) {
                                for (ClientSocket s : clients.keySet()) {
                                    if (clients.get(s).getStatus() == ClientStatus.LOGGED_IN) {
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
                    case DISCONNECT -> {
                        System.out.println("Client disconnected");
                        clients.get(this.socket).setStatus(ClientStatus.NOT_LOGGED_IN);
                        out.writeUnshared(idea.crypt(new ServerPacket(ServerResponse.DISCONNECTED).jsonSerialize().getBytes(), true));
                        out.flush();
                        ArrayList<String> usernames = getListOfLoggedUsernames();
                        broadcastUsersList(usernames);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected");
            clients.remove(this.socket);
            try {
                this.socket.getSocket().close();
                ArrayList<String> usernames = getListOfLoggedUsernames();
                broadcastUsersList(usernames);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private ArrayList<String> getListOfLoggedUsernames() {
        ArrayList<String> usernames = new ArrayList<>();
        synchronized (clients) {
            for (ClientSocket s : clients.keySet()) {
                if (clients.get(s).getStatus() == ClientStatus.LOGGED_IN) {
                    usernames.add(clients.get(s).getUsername());
                }
            }
        }
        return usernames;
    }

    private void broadcastUsersList(ArrayList<String> usernames) throws IOException {
        synchronized (clients) {
            for (ClientSocket s : clients.keySet()) {
                if (clients.get(s).getStatus() == ClientStatus.LOGGED_IN) {
                    Idea clientCipher = new Idea(clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey());
                    s.getOut().writeUnshared(clientCipher.crypt(new UserConnectionServerPacket(ServerResponse.NEW_USER_CONNECTED, usernames)
                            .jsonSerialize().getBytes(), true));
                    s.getOut().flush();
                }
            }
        }
    }
}