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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
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
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            out = this.socket.getOut();
            in = this.socket.getIn();

            getSessionKeys(out, in);

            while (true) {
//                byte[] encryptedJson = (byte[]) in.readObject();
                int[] encryptionKey = clients.get(socket).getEncryptKey();
                int[] decryptionKey = clients.get(socket).getDecryptKey();
               // String decryptedJson = new String(Idea.crypt(encryptedJson, false, encryptionKey, decryptionKey), StandardCharsets.UTF_8);
                clientPacket = ClientPacket.jsonDeserialize(Idea.crypt((byte[]) in.readObject(), false, encryptionKey, decryptionKey));
                processUserRequest(clientPacket, out);
            }
        } catch (Exception e) {
            System.out.println("Client closed app");
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

    private void getSessionKeys(ObjectOutputStream out, ObjectInputStream in) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
        KeyPair keyPair = KeyDistributor.getKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        out.writeUnshared(new PublicKeyServerPacket(ServerResponse.OPEN_RSA_KEY, publicKey.getEncoded()));
        out.flush();

        IdeaKeysClientPacket clientPacket = (IdeaKeysClientPacket) in.readObject();

        Cipher decipher = Cipher.getInstance("RSA");
        decipher.init(Cipher.DECRYPT_MODE, privateKey);
        int[] encryptionKey = ByteArrayConverter.byteArrayToIntArray(decipher.doFinal(clientPacket.getEncryptKey()));
        int[] decryptionKey = ByteArrayConverter.byteArrayToIntArray(decipher.doFinal(clientPacket.getDecryptKey()));
        clients.put(this.socket, new ClientInfo(ClientStatus.NOT_LOGGED_IN, "", encryptionKey, decryptionKey));
    }

    private void processUserRequest(ClientPacket clientPacket, ObjectOutputStream out) throws IOException, NoSuchAlgorithmException {
        switch (clientPacket.getRequest()) {
            case REGISTER -> {
                processUserRegistration((RegistrationClientPacket) clientPacket, out);
            }
            case LOGIN -> {
                processUserAuthorization((LoginClientPacket) clientPacket, out);
            }
            case CONFIRM_REGISTRATION -> {
                processRegistrationConfirmation((ConfirmationClientPacket) clientPacket, out);
            }
            case MESSAGE -> {
                processUserMessage((MessageClientPacket) clientPacket);
            }
            case DISCONNECT -> {
                processDisconnection(out);
            }
        }
    }

    private void processDisconnection(ObjectOutputStream out) throws IOException {
        clients.get(this.socket).setStatus(ClientStatus.NOT_LOGGED_IN);
        out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.DISCONNECTED).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
        out.flush();
        ArrayList<String> usernames = getListOfLoggedUsernames();
        broadcastUsersList(usernames);
    }

    private void processUserMessage(MessageClientPacket clientPacket) throws IOException {
        String message = clientPacket.getMessage();
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
            sendPersonalMessage(clientPacket, personalGetter);
        } else {
            sendBroadcastMessage(clientPacket);
        }
    }

    private void sendBroadcastMessage(MessageClientPacket clientPacket) throws IOException {
        String sender = clientPacket.getSender();
        String message = clientPacket.getMessage();
        ZonedDateTime dateTime = clientPacket.getDateTime();
        MessageServerPacket serverPacket = new MessageServerPacket(ServerResponse.MESSAGE, sender, message, dateTime);
        synchronized (clients) {
            for (ClientSocket s : clients.keySet()) {
                if (clients.get(s).getStatus() == ClientStatus.LOGGED_IN) {
                    if (Objects.equals(clients.get(s).getUsername(), sender)) {
                        serverPacket.setMessageStatus(MessageStatus.IS_SENT);
                    } else {
                        serverPacket.setMessageStatus(MessageStatus.IS_GET);
                    }
                    s.getOut().writeUnshared(Idea.crypt(serverPacket.jsonSerialize(), true, clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey()));
                    s.getOut().flush();
                }
            }
        }
    }

    private void sendPersonalMessage(MessageClientPacket clientPacket, String personalGetter) throws IOException {
        String message = clientPacket.getMessage();
        String sender = clientPacket.getSender();
        ZonedDateTime dateTime = clientPacket.getDateTime();
        String tempMessage = message;
        message = message.replaceFirst("@" + personalGetter, "").trim();
        if (!message.isEmpty()) {
            synchronized (clients) {
                for (ClientSocket s : clients.keySet()) {
                    if (Objects.equals(clients.get(s).getUsername(), personalGetter)) {
                        MessageServerPacket serverPacket = new MessageServerPacket(ServerResponse.MESSAGE, sender, message, dateTime);
                        serverPacket.setMessageStatus(MessageStatus.IS_PERSONAL_GET);
                        s.getOut().writeUnshared(Idea.crypt(serverPacket.jsonSerialize(), true, clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey()));
                        s.getOut().flush();
                    } else if (Objects.equals(clients.get(s).getUsername(), sender)) {
                        MessageServerPacket serverPacket = new MessageServerPacket(ServerResponse.MESSAGE, sender, tempMessage.trim(), dateTime);
                        serverPacket.setMessageStatus(MessageStatus.IS_SENT);
                        s.getOut().writeUnshared(Idea.crypt(serverPacket.jsonSerialize(), true, clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey()));
                        s.getOut().flush();
                    }
                }
            }
        }
    }

    private void processRegistrationConfirmation(ConfirmationClientPacket clientPacket, ObjectOutputStream out) throws IOException {
        String username = clientPacket.getUsername();
        int code = clientPacket.getCode();
        SqlResultCode codeCorrectness = dbConnection.checkConfirmationCode(username, code);
        switch (codeCorrectness) {
            case WRONG_CODE -> {
                out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.INVALID_CODE).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
                out.flush();
            }
            case CORRECT_CODE -> {
                out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.VALID_CODE).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
                out.flush();
            }
        }
    }

    private void processUserAuthorization(LoginClientPacket clientPacket, ObjectOutputStream out) throws IOException, NoSuchAlgorithmException {
        String username = clientPacket.getUsername();
        String password = clientPacket.getPassword();
        SqlResultCode accessPermission = dbConnection.checkPermission(username, password);
        switch (accessPermission) {
            case ACCESS_DENIED -> {
                out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.LOGIN_DENIED).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
                out.flush();
            }
            case NOT_CONFIRMED -> {
                out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.USER_NOT_CONFIRMED).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
                out.flush();
            }
            case ALLOW_LOGIN -> {
                confirmUserAuthorization(username, out);
            }
        }
    }

    private void confirmUserAuthorization(String username, ObjectOutputStream out) throws IOException {
        ArrayList<String> usernames = getListOfLoggedUsernames();
        if (!usernames.contains(username)) {
            synchronized (clients) {
                clients.get(this.socket).setStatus(ClientStatus.LOGGED_IN);
                clients.get(this.socket).setUsername(username);
            }
            out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.LOGIN_ALLOWED).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
            out.flush();
            usernames.add(username);
            broadcastUsersList(usernames);
        } else {
            out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.ALREADY_LOGGED_IN).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
            out.flush();
        }
    }

    private void processUserRegistration(RegistrationClientPacket clientPacket, ObjectOutputStream out) throws IOException, NoSuchAlgorithmException {
        SqlResultCode sqlResult = dbConnection.checkNewUserInfo(clientPacket.getUsername(), clientPacket.getEmail());
        switch (sqlResult) {
            case EXISTING_USERNAME -> {
                out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.EXISTING_USERNAME).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
                out.flush();
            }
            case REPEATED_EMAIL -> {
                out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.REPEATED_EMAIL).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
                out.flush();
            }
            case SUCCESS -> {
                confirmUserRegistration(clientPacket, out);
            }
        }
    }

    private void confirmUserRegistration(RegistrationClientPacket clientPacket, ObjectOutputStream out) throws NoSuchAlgorithmException, IOException {
        String username = clientPacket.getUsername();
        String email = clientPacket.getEmail();
        String password = clientPacket.getPassword();
        String salt = RandStringGenerator.generateSalt();
        String encryptedData = SHA256.getHashString(salt + username + password);
        SSLEmail emailConnection = new SSLEmail(username, email);
        String hash = RandStringGenerator.generateVerificationCode();
        boolean errorFlag = false;
        try {
            emailConnection.sendConfirmationMessage(hash);
        } catch (Exception e) {
            out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.REPEATED_EMAIL).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
            out.flush();
            errorFlag = true;
        }

        if (!errorFlag) {
            dbConnection.safeUserData(username, email, salt, encryptedData);
            out.writeUnshared(Idea.crypt(new ServerPacket(ServerResponse.SUCCESSFUL_REGISTRATION).jsonSerialize(), true, clients.get(socket).getEncryptKey(), clients.get(socket).getDecryptKey()));
            out.flush();
            dbConnection.safeConfirmationCode(hash, username);
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
                    s.getOut().writeUnshared(Idea.crypt(new UserConnectionServerPacket(ServerResponse.NEW_USER_CONNECTED, usernames)
                            .jsonSerialize(), true, clients.get(s).getEncryptKey(), clients.get(s).getDecryptKey()));
                    s.getOut().flush();
                }
            }
        }
    }
}