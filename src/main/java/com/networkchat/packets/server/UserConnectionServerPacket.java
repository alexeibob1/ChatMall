package com.networkchat.packets.server;

import java.io.Serializable;

public class UserConnectionServerPacket extends ServerPacket implements Serializable {
    private String username;

    public UserConnectionServerPacket(ServerResponse response, String username) {
        super(response);
        this.username = username;
    }

    public UserConnectionServerPacket() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
