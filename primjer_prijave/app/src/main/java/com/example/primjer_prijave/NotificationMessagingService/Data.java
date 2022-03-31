package com.example.primjer_prijave.NotificationMessagingService;

public class Data {
    private String type;
    private String title;
    private String message;
    private String sender;
    private String receiver;

    public Data(String type, String title, String message, String sender, String receiver) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
