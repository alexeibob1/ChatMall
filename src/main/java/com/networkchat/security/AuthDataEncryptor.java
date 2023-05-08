package com.networkchat.security;

import com.networkchat.client.User;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthDataEncryptor {
    public static void encryptRegistrationData(User user) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String salt = generateSalt();
        user.setSalt(salt);
        String message = user.getSalt() + user.getUsername() + user.getPassword();
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        user.setEncryptedData(bytesToHex(hash));
    }

    public static String encryptLoginData(String salt, String username, String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String message = salt + username + password;
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    public static void encryptLoginData(User user) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher encryptCipher = Cipher.getInstance("RSA");

    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
