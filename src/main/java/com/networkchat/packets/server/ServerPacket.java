package com.networkchat.packets.server;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")

public class ServerPacket implements Serializable {
    private ServerResponse response;

    public ServerPacket() {}

    public ServerPacket(ServerResponse response) {
        this.response = response;
    }

    public ServerResponse getResponse() {
        return response;
    }

    public void setResponse(ServerResponse response) {
        this.response = response;
    }

    public String jsonSerialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.writeValueAsString(this);
    }

    public static ServerPacket jsonDeserialize(String jsonValue) throws JsonProcessingException {
        return new ObjectMapper().registerModule(new JavaTimeModule()).disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE).enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).readValue(jsonValue, ServerPacket.class);
    }

}
