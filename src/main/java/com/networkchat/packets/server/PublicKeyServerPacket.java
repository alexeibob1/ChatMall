package com.networkchat.packets.server;

import java.io.Serializable;

public class PublicKeyServerPacket extends ServerPacket implements Serializable {
    private static final long serialVersionUID = 8L;
    private byte[] publicKey;

    public PublicKeyServerPacket() {}

    public PublicKeyServerPacket(ServerResponse response, byte[] publicKey) {
        super(response);
        this.publicKey = publicKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
