package com.networkchat.chat;

import java.time.ZonedDateTime;

public class Message {
    private String sender;
    private String receiver;
    private ZonedDateTime dateTime;
    private String message;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(String sender, String receiver, ZonedDateTime dateTime, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.dateTime = dateTime;
        this.message = message;
    }
}
