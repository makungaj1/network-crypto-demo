package com.jm.crypto.client;

import java.io.Serializable;

public class WrapperObject implements Serializable {
    private byte[] from;
    private byte[] to;
    private byte[] objective;
    private OtherClient otherClient;
    private byte[] messageBody;

    public WrapperObject(byte[] from, byte[] to, byte[] objective) {
        this.from = from;
        this.to = to;
        this.objective = objective;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(byte[] messageBody) {
        this.messageBody = messageBody;
    }

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }

    public byte[] getTo() {
        return to;
    }

    public void setTo(byte[] to) {
        this.to = to;
    }

    public byte[] getObjective() {
        return objective;
    }

    public void setObjective(byte[] objective) {
        this.objective = objective;
    }

    public OtherClient getOtherClient() {
        return otherClient;
    }

    public void setOtherClient(OtherClient otherClient) {
        this.otherClient = otherClient;
    }

}
