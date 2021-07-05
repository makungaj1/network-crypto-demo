package com.jm.crypto.client;

import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;
import java.security.Key;

public class SharedSecret implements Serializable {
    private Key key;
    private byte[] ivByte;

    public SharedSecret(Key key, byte[] ivByte) {
        this.key = key;
        this.ivByte = ivByte;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public byte[] getIvByte() {
        return ivByte;
    }

    public void setIvByte(byte[] ivByte) {
        this.ivByte = ivByte;
    }

}
