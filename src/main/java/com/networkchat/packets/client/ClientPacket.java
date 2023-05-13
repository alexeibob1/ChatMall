package com.networkchat.packets.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")

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

    public String jsonSerialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public static ClientPacket jsonDeserialize(String jsonValue) throws JsonProcessingException {
        return new ObjectMapper().readValue(jsonValue, ClientPacket.class);
    }
}
