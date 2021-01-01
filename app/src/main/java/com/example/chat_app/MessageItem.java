package com.example.chat_app;

public class MessageItem {

    String name;
    String time;
    String message;
    String profileUri;

    public MessageItem(String name, String message, String time, String profileUri) {
        this.name = name;
        this.time = time;
        this.message = message;
        this.profileUri = profileUri;
    }

    //firebase DB에 객체로 값을 읽어올 때 파라미터 비어있는 생성자 필요
    public MessageItem() {
    }

    //Getter & Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }
}
