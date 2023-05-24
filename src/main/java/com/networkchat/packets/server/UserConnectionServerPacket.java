package com.networkchat.packets.server;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class UserConnectionServerPacket extends ServerPacket implements Serializable {
    private static final long serialVersionUID = 10L;
    private ArrayList<String> usernames = new ArrayList<>();

    public UserConnectionServerPacket(ServerResponse response, ArrayList<String> usernames) {
        super(response);
        this.usernames = usernames;
    }

    public UserConnectionServerPacket() {}

    public ArrayList<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(ArrayList<String> username) {
        this.usernames = username;
    }
}
