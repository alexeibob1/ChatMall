package com.networkchat.packets.client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")

public class ClientPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    private ClientRequest request;

    public ClientPacket() {}

    public ClientPacket(ClientRequest request) {
        this.request = request;
    }

    public ClientRequest getRequest() {
        return request;
    }

    public String jsonSerialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.writeValueAsString(this);
    }

    public static ClientPacket jsonDeserialize(String jsonValue) throws JsonProcessingException {
        return new ObjectMapper().registerModule(new JavaTimeModule()).disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE).readValue(jsonValue, ClientPacket.class);
    }
}
