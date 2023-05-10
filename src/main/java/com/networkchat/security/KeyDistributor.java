package com.networkchat.security;

import com.networkchat.client.User;
import com.networkchat.sql.SQLConnection;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyDistributor {
    public static void generateKeys(SQLConnection dbConnection, String connectionID) throws NoSuchAlgorithmException {
        KeyPair pair = getKeyPair();
        safePrivateKey(pair.getPrivate(), dbConnection, connectionID);
    }

    public static PublicKey generatePublicKey() throws NoSuchAlgorithmException {
        //KeyPair pair = getKeyPair();
        return getKeyPair().getPublic();
        //byte[] keyBytes = pair.getPublic().getEncoded();
        //return Base64.getEncoder().encodeToString(keyBytes);
    }

    public static KeyPair getKeyPair() throws NoSuchAlgorithmException {
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

    private static void safePrivateKey(PrivateKey key, SQLConnection dbConnection, String connectionID) {
        byte[] keyBytes = key.getEncoded();
        String strKey = Base64.getEncoder().encodeToString(keyBytes);
        dbConnection.safePublicKey(strKey, connectionID);
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