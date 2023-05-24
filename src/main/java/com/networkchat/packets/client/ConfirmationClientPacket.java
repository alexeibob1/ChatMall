package com.networkchat.packets.client;

import java.io.Serializable;

public class ConfirmationClientPacket extends ClientPacket implements Serializable {
    private static final long serialVersionUID = 2L;
    private String username;
    private int code;

    public ConfirmationClientPacket() {}

    public ConfirmationClientPacket(ClientRequest request, String username, int code) {
        super(request);
        this.username = username;
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String email) {
        this.username = email;
    }

    public int getCode() {
        return code;
    }

}
