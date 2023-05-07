package com.networkchat.security;

import com.networkchat.client.User;
import com.networkchat.sql.SQLConnection;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
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
        user.setPublicKey(key);
        try (FileOutputStream fileOutputStream = new FileOutputStream("src/main/java/com/networkchat/config/public.key")) {
            fileOutputStream.write(key.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void safePrivateKey(PrivateKey key, SQLConnection dbConnection, User user) {
        byte[] keyBytes = key.getEncoded();
        String strKey = Base64.getEncoder().encodeToString(keyBytes);
        dbConnection.safePrivateKey(strKey, user.getUsername());
    }

    public static PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKey = null;
        File file = new File("src/main/java/com/networkchat/config/public.key");
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            publicKey = new byte[(int)file.length()];
            fileInputStream.read(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
    }
}