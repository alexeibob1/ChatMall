package com.networkchat.packets.client;

public class IdeaKeysClientPacket extends ClientPacket{
    private byte[] encryptKey;

    private byte[] decryptKey;

    public IdeaKeysClientPacket() {}

    public IdeaKeysClientPacket(ClientRequest request, byte[] encryptKey, byte[] decryptKey) {
        super(request);
        this.encryptKey = encryptKey;
        this.decryptKey = decryptKey;
    }

    public byte[] getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(byte[] encryptKey) {
        this.encryptKey = encryptKey;
    }

    public byte[] getDecryptKey() {
        return decryptKey;
    }

    public void setDecryptKey(byte[] decryptKey) {
        this.decryptKey = decryptKey;
    }
}
