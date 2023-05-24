package com.networkchat.packets.client;

import com.networkchat.packets.server.MessageStatus;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class MessageClientPacket extends ClientPacket implements Serializable {
    private static final long serialVersionUID = 5L;
    private String sender;
    private String message;

    private ZonedDateTime dateTime;

    public MessageClientPacket() {}

    public MessageClientPacket(ClientRequest request, String sender, String message, ZonedDateTime dateTime) {
        super(request);
        this.sender = sender;
        this.message = message;
        this.dateTime = dateTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

}
