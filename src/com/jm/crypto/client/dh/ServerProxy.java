package com.jm.crypto.client.dh;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.PublicKey;

public class ServerProxy {
    private String serverIP ;
    private int serverPort;
    private PublicKey serverPublicKey;
    private SecretKey secretKeyWithServer;
    private IvParameterSpec ivWithServer;

    public ServerProxy(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public PublicKey getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(PublicKey serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }

    public SecretKey getSecretKeyWithServer() {
        return secretKeyWithServer;
    }

    public void setSecretKeyWithServer(SecretKey secretKeyWithServer) {
        this.secretKeyWithServer = secretKeyWithServer;
    }

    public IvParameterSpec getIvWithServer() {
        return ivWithServer;
    }

    public void setIvWithServer(IvParameterSpec ivWithServer) {
        this.ivWithServer = ivWithServer;
    }

}
