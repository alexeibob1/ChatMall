package com.networkchat.packets.server;

import com.networkchat.client.ClientStatus;

public class ClientInfo {
    private ClientStatus status;
    private String username;
    private int[] encryptKey;
    private int[] decryptKey;

    public ClientInfo(ClientStatus status, String username, int[] encryptKey, int[] decryptKey) {
        this.status = status;
        this.username = username;
        this.encryptKey = encryptKey;
        this.decryptKey = decryptKey;
    }

    public ClientInfo() {}

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int[] getEncryptKey() {
        return encryptKey;
    }

    public int[] getDecryptKey() {
        return decryptKey;
    }

}
