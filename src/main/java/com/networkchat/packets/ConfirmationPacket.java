package com.networkchat.packets;

import java.io.Serializable;

public class ConfirmationPacket extends Packet implements Serializable {
    private String email;
    private String code;

    public ConfirmationPacket() {}

    public ConfirmationPacket(ClientRequest request, String email, String code) {
        super(request);
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
