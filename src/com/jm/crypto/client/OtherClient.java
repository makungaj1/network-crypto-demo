package com.jm.crypto.client;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;
import java.security.PublicKey;

public class OtherClient implements Serializable {
    private byte[] otherIP ;
    private PublicKey otherPublicKey;
    private SecretKey secretKeyWithOther;
    private byte[] ivByte;

    public OtherClient(byte[] otherIP, byte[] ivByte) {
        this.otherIP = otherIP;
        this.ivByte = ivByte;
    }

    public byte[] getOtherIP() {
        return otherIP;
    }

    public void setOtherIP(byte[] otherIP) {
        this.otherIP = otherIP;
    }

    public PublicKey getOtherPublicKey() {
        return otherPublicKey;
    }

    public void setOtherPublicKey(PublicKey otherPublicKey) {
        this.otherPublicKey = otherPublicKey;
    }

    public SecretKey getSecretKeyWithOther() {
        return secretKeyWithOther;
    }

    public void setSecretKeyWithOther(SecretKey secretKeyWithOther) {
        this.secretKeyWithOther = secretKeyWithOther;
    }

    public byte[] getIvByte() {
        return ivByte;
    }

    public void setIvByte(byte[] ivByte) {
        this.ivByte = ivByte;
    }
}
