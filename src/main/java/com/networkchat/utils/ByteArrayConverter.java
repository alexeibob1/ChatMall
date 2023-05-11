package com.networkchat.utils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ByteArrayConverter {
    public static byte[] intArrayToByteArray(int[] intArray) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 * intArray.length);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(intArray);
        return byteBuffer.array();
    }

    public static int[] byteArrayToIntArray(byte[] byteArray) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        int[] intArray = new int[byteArray.length / 4];
        int i = 0;
        while (intBuffer.hasRemaining()) {
            intArray[i++] = intBuffer.get();
        }
        return intArray;
    }
}
