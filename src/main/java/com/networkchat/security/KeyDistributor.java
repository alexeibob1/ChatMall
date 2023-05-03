package com.networkchat.security;

import com.networkchat.client.User;
import com.networkchat.sql.SQLConnection;

import javax.crypto.Cipher;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Base64;

public class KeyDistributor {
    public static void generateKeys(User user, SQLConnection dbConnection) throws NoSuchAlgorithmException {
        KeyPair pair = getKeyPair();
        safePublicKey(pair.getPublic(), user);
        safePrivateKey(pair.getPrivate(), dbConnection, user);
    }

    private static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static void safePublicKey(PublicKey key, User user) {
        byte[] byteKey = key.getEncoded();
        user.setPublicKey(byteKey);
        try (FileOutputStream fileOutputStream = new FileOutputStream("src/main/java/com/networkchat/config/public.key")) {
            fileOutputStream.write(byteKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void safePrivateKey(PrivateKey key, SQLConnection dbConnection, User user) {
        byte[] keyBytes = key.getEncoded();
        String strKey = Base64.getEncoder().encodeToString(keyBytes);
        dbConnection.safePrivateKey(strKey, user.getUsername());
    }
}
