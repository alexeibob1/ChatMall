package com.networkchat.security.idea;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class Idea {
    private static final int rounds = 8;

    private int[] encryptKey = new int[52];
    private int[] decryptKey = new int[52];

    public Idea() {
        SecureRandom random = new SecureRandom();
        byte[] initVector = new byte[16];
        random.nextBytes(initVector);
        setKey(initVector);
    }

    public Idea(int[] encryptKey, int[] decryptKey) {
        this.encryptKey = encryptKey;
        this.decryptKey = decryptKey;
    }

    public int[] getEncryptKey() {
        return encryptKey;
    }

    public int[] getDecryptKey() {
        return decryptKey;
    }

    public static byte[] crypt(byte[] rawData, boolean toEncrypt, int[] encryptKey, int[] decryptKey) {
        byte[] data = rawData;
        int length = rawData.length;
        if (length % 8 != 0) {
            int delta = 8 - length % 8;
            data = Arrays.copyOf(rawData, delta + length);
            while (delta > 0) {
                data[data.length - delta] = (byte)' ';
                delta--;
            }
        }
        int[] key = toEncrypt ? encryptKey : decryptKey;
        for (int dataPos = 0; dataPos < data.length; dataPos += 8) {
            int x0 = ((data[dataPos] & 0xFF) << 8) | (data[dataPos + 1] & 0xFF);
            int x1 = ((data[dataPos + 2] & 0xFF) << 8) | (data[dataPos + 3] & 0xFF);
            int x2 = ((data[dataPos + 4] & 0xFF) << 8) | (data[dataPos + 5] & 0xFF);
            int x3 = ((data[dataPos + 6] & 0xFF) << 8) | (data[dataPos + 7] & 0xFF);

            int p = 0;
            for (int round = 0; round < rounds; round++) {
                int y0 = mul(x0, key[p++]);
                int y1 = add(x1, key[p++]);
                int y2 = add(x2, key[p++]);
                int y3 = mul(x3, key[p++]);

                int t0 = mul(y0 ^ y2, key[p++]);
                int t1 = add(y1 ^ y3, t0);
                int t2 = mul(t1, key[p++]);
                int t3 = add(t0, t2);

                x0 = y0 ^ t2;
                x1 = y2 ^ t2;
                x2 = y1 ^ t3;
                x3 = y3 ^ t3;
            }

            int r0 = mul(x0, key[p++]);
            int r1 = add(x2, key[p++]);
            int r2 = add(x1, key[p++]);
            int r3 = mul(x3, key[p++]);

            data[dataPos] = (byte)(r0 >> 8);
            data[dataPos + 1] = (byte)r0;
            data[dataPos + 2] = (byte)(r1 >> 8);
            data[dataPos + 3] = (byte)r1;
            data[dataPos + 4] = (byte)(r2 >> 8);
            data[dataPos + 5] = (byte)r2;
            data[dataPos + 6] = (byte)(r3 >> 8);
            data[dataPos + 7] = (byte)r3;
        }
        return data;
    }

    private static int add (int a, int b) {
        return (a + b) & 0xFFFF;
    }

    private static int mul (int a, int b ) {
        long r = (long)a * b;
        if (r != 0) {
            return (int)(r % 0x10001) & 0xFFFF; }
        else {
            return (1 - a - b) & 0xFFFF;
        }
    }

    private static int mulInv (int x) {
        if (x <= 1) {
            return x;
        }
        int y = 0x10001;
        int t0 = 1;
        int t1 = 0;
        while (true) {
            t1 += y / x * t0;
            y %= x;
            if (y == 1) {
                return 0x10001 - t1;
            }
            t0 += x / y * t1;
            x %= y;
            if (x == 1) {
                return t0;
            }
        }
    }

    private static int addInv (int x) {
        return (0x10000 - x) & 0xFFFF;
    }

    protected void setKey(byte[] userKey) {
        if (userKey.length != 16) {
            throw new IllegalArgumentException(); }
        this.encryptKey = new int[rounds * 6 + 4];
        for (int i = 0; i < userKey.length / 2; i++) {
            this.encryptKey[i] = ((userKey[2 * i] & 0xFF) << 8) | (userKey[2 * i + 1] & 0xFF); }
        for (int i = userKey.length / 2; i < this.encryptKey.length; i++) {
            this.encryptKey[i] = ((this.encryptKey[(i + 1) % 8 != 0 ? i - 7 : i - 15] << 9) | (this.encryptKey[(i + 2) % 8 < 2 ? i - 14 : i - 6] >> 7)) & 0xFFFF;
        }

        this.decryptKey = new int[this.encryptKey.length];
        int p = 0;
        int i = rounds * 6;
        this.decryptKey[i] = mulInv(this.encryptKey[p++]);
        this.decryptKey[i + 1] = addInv(this.encryptKey[p++]);
        this.decryptKey[i + 2] = addInv(this.encryptKey[p++]);
        this.decryptKey[i + 3] = mulInv(this.encryptKey[p++]);
        for (int r = rounds - 1; r >= 0; r--) {
            i = r * 6;
            int m = r > 0 ? 2 : 1;
            int n = r > 0 ? 1 : 2;
            this.decryptKey[i + 4] = this.encryptKey[p++];
            this.decryptKey[i + 5] = this.encryptKey[p++];
            this.decryptKey[i] = mulInv(this.encryptKey[p++]);
            this.decryptKey[i + m] = addInv(this.encryptKey[p++]);
            this.decryptKey[i + n] = addInv(this.encryptKey[p++]);
            this.decryptKey[i + 3] = mulInv(this.encryptKey[p++]);
        }
    }
}