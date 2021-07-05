package com.jm.crypto.client;

import java.io.Serializable;

public class MessageObject implements Serializable {
    private String from;
    private byte[] msg;

    public MessageObject(String from, byte[] msg) {
        this.from = from;
        this.msg = msg;
    }

    public String getFrom() {
        return from;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

}
