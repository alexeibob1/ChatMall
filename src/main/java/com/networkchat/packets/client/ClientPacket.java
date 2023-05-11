package com.networkchat.packets.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegistrationClientPacket.class, name = "Registration packet"),
        @JsonSubTypes.Type(value = LoginClientPacket.class, name = "Login packet"),
        @JsonSubTypes.Type(value = ConfirmationClientPacket.class, name = "Confirmation packet")
})

public class ClientPacket implements Serializable {
    private ClientRequest request;

    public ClientPacket() {}

    public ClientPacket(ClientRequest request) {
        this.request = request;
    }

    public ClientRequest getRequest() {
        return request;
    }

    public void setRequest(ClientRequest request) {
        this.request = request;
    }
}
