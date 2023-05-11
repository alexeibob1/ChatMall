package com.networkchat.packets.client;

import java.io.Serializable;

public class ConfirmationClientPacket extends ClientPacket implements Serializable {
    private String email;
    private String code;

    public ConfirmationClientPacket() {}

    public ConfirmationClientPacket(ClientRequest request, String email, String code) {
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
