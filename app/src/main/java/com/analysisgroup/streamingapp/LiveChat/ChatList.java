package com.analysisgroup.streamingapp.LiveChat;

public class ChatList {

    private String UID, username, msg;

    public ChatList() {
    }

    public ChatList(String UID, String username, String msg) {
        this.UID = UID;
        this.username = username;
        this.msg = msg;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
