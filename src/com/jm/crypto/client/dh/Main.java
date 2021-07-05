package com.jm.crypto.client.dh;

import com.jm.crypto.client.MessageObject;
import com.jm.crypto.client.SharedSecret;
import com.jm.crypto.client.Utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // Generate the client KeyPair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey clientPrivate = keyPair.getPrivate();
            System.out.println("Client Private Key: " + Base64.getEncoder().encodeToString(clientPrivate.getEncoded()));
            PublicKey clientPublic = keyPair.getPublic();
            System.out.println("Client Public key: " + Base64.getEncoder().encodeToString(clientPublic.getEncoded()));

            // Connect to server
            Socket server = new Socket(Utils.getServerIP(), Utils.getServerPort());

            // Create server proxy object
            ServerProxy serverProxy = new ServerProxy(server.getInetAddress().getHostAddress(), server.getPort());
            System.out.println("Connected to server");
            System.out.println("Server IP: " + serverProxy.getServerIP());
            System.out.println("Server PORT: " + serverProxy.getServerPort());

            // Create data input and output streams
            ObjectOutputStream objToServer = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream objFromServer = new ObjectInputStream(server.getInputStream());

            // Send Public key to server
            objToServer.writeObject(clientPublic);
            objToServer.flush();

            // Get from server: 1) Server public key 2) iv Random
            SharedSecret sharedSecret = (SharedSecret) objFromServer.readObject();
            serverProxy.setServerPublicKey((PublicKey) sharedSecret.getKey());
            serverProxy.setIvWithServer(new IvParameterSpec(sharedSecret.getIvByte()));

            // Calculate the shared secret between server and (this) client
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(clientPrivate);
            keyAgreement.doPhase(serverProxy.getServerPublicKey(), true);
            serverProxy.setSecretKeyWithServer(new SecretKeySpec(shortenKey(keyAgreement.generateSecret()), "AES"));

            // Print serverProxy data
            System.out.println("Server Public key: " + Base64.getEncoder().encodeToString(serverProxy.getServerPublicKey().getEncoded()));
            System.out.println("Shared key: " + Base64.getEncoder().encodeToString(serverProxy.getSecretKeyWithServer().getEncoded()));
            System.out.println("Shared iv: " + Base64.getEncoder().encodeToString(serverProxy.getIvWithServer().getIV()));


        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private static byte[] shortenKey(byte[] s) {
        byte[] b = new byte[16]; // For AES algo
        System.arraycopy(s, 0, b, 0, b.length);
        return b;
     }
}
