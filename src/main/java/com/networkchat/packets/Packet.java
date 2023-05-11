package com.networkchat.packets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegistrationPacket.class, name = "Registration packet"),
        @JsonSubTypes.Type(value = LoginPacket.class, name = "Login packet"),
        @JsonSubTypes.Type(value = ConfirmationPacket.class, name = "Confirmation packet")
})

public class Packet implements Serializable {
    private ClientRequest request;

    public Packet() {}

    public Packet(ClientRequest request) {
        this.request = request;
    }

    public ClientRequest getRequest() {
        return request;
    }

    public void setRequest(ClientRequest request) {
        this.request = request;
    }

    public String jsonSerialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public static Packet jsonDeserialize(String jsonValue) throws JsonProcessingException {
        return new ObjectMapper().readValue(jsonValue, Packet.class);
    }
}
