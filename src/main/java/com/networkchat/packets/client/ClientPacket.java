package com.networkchat.packets.client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        mapper.registerModule(new JavaTimeModule());
        return mapper.writeValueAsString(this);
    }

    public static ClientPacket jsonDeserialize(String jsonValue) throws JsonProcessingException {
        return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(jsonValue, ClientPacket.class);
    }
}
