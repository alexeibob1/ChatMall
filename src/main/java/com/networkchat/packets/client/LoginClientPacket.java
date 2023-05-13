package com.networkchat.packets.client;

import java.io.Serializable;

public class LoginClientPacket extends ClientPacket implements Serializable {
    private String username;

    private String password;

    public LoginClientPacket() {}

    public LoginClientPacket(ClientRequest request, String username, String password) {
        super(request);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}