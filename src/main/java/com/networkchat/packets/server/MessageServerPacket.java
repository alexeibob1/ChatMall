package com.networkchat.packets.server;

import com.networkchat.packets.client.ClientPacket;
import com.networkchat.packets.client.ClientRequest;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class MessageServerPacket extends ServerPacket implements Serializable {
    private String sender;
    private String message;

    private ZonedDateTime dateTime;

    private MessageStatus messageStatus;

    public MessageServerPacket() {}

    public MessageServerPacket(ServerResponse response, String sender, String message, ZonedDateTime dateTime) {
        super(response);
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

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }
}
