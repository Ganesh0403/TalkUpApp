package com.example.talkup;

public class Messages {
    private String from,type,message,to,messageId,time,date,name,key;

    public Messages() {
    }

    public Messages(String from, String type, String message, String to, String messageId, String time, String date, String name, String key) {
        this.from = from;
        this.type = type;
        this.message = message;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.date = date;
        this.name = name;
        this.key = key;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() { return message; }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey(){return key;}

    public void setKey(String key){ this.key = key;}
}
