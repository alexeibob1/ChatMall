package com.networkchat.security;

import com.networkchat.utils.HexConverter;

import java.security.SecureRandom;

public class RandStringGenerator {
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return HexConverter.bytesToHex(salt);
    }

    public static String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(random.nextInt(100000, 999999));
    }
}
