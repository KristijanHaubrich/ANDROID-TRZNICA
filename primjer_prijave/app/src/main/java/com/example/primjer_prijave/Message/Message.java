package com.example.primjer_prijave.Message;

public class Message {
    private final String type = "message";
    private String messageType;
    private String body;
    private String sender;
    private String receiver;
    private int id;

    public Message(){}

    public Message(String messageType, String body, String sender, String receiver, int id) {
        this.messageType = messageType;
        this.body = body;
        this.sender = sender;
        this.receiver = receiver;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

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
}
