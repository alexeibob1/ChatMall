package com.networkchat.security;

import com.networkchat.client.User;
import com.networkchat.sql.SQLConnection;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthDataEncryptor {
    public static void encryptUserData(User user) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, user.getPublicKey());
        String salt = generateSalt();
        user.setSalt(salt);
        String message = user.getSalt() + user.getUsername() + user.getPassword();
        byte[] encryptedMessage = encryptCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        user.setEncryptedData(Base64.getEncoder().encodeToString(encryptedMessage));
    }

    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
