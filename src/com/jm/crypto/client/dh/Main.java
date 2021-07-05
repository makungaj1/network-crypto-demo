package com.jm.crypto.client.dh;

import com.jm.crypto.client.*;

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

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            WrapperObject wrapperObject;
            byte[] from;
            byte[] to;
            byte[] objective;
            // Request a public key of an IP (Other client: 192.168.0.5)
                // Encrypt data to send to server
            cipher.init(Cipher.ENCRYPT_MODE, serverProxy.getSecretKeyWithServer(), serverProxy.getIvWithServer());
            from = cipher.doFinal("192.168.0.12".getBytes());
            System.out.println("From: " + Base64.getEncoder().encodeToString(from));
            to = cipher.doFinal("192.168.0.5".getBytes());
            System.out.println("To: " + Base64.getEncoder().encodeToString(to));
            objective = cipher.doFinal("Initial".getBytes());
            System.out.println("Objective: " + Base64.getEncoder().encodeToString(objective));
            wrapperObject = new WrapperObject(from, to, objective);
            objToServer.writeObject(wrapperObject);
            objToServer.flush();

            // Server should return other.publicKey and iv Random
            System.out.println("From server");
            wrapperObject = (WrapperObject) objFromServer.readObject();
            // Decrypt meta dada from server
            cipher.init(Cipher.DECRYPT_MODE, serverProxy.getSecretKeyWithServer(), serverProxy.getIvWithServer());
            from = cipher.doFinal(wrapperObject.getFrom());
            System.out.println("From: (" + Base64.getEncoder().encodeToString(wrapperObject.getFrom()) + ") " + new String(from));
            to = cipher.doFinal(wrapperObject.getTo());
            System.out.println("To: (" + Base64.getEncoder().encodeToString(wrapperObject.getTo()) + ") " + new String(to));
            objective = cipher.doFinal(wrapperObject.getObjective());
            System.out.println("Objective: (" + Base64.getEncoder().encodeToString(wrapperObject.getObjective()) + ") " + new String(objective));

            if (new String(objective).equalsIgnoreCase("success")) {
                System.out.println("Other is active");

                OtherClient otherClient = wrapperObject.getOtherClient();
                // Calculate the shared secret key with other
                keyAgreement.doPhase(otherClient.getOtherPublicKey(), true);
                otherClient.setSecretKeyWithOther(new SecretKeySpec(shortenKey(keyAgreement.generateSecret()), "AES"));
                // Calculate the shared IV
                otherClient.setIvWithOther(new IvParameterSpec(otherClient.getIvByte()));
                // Print other's keys
                System.out.println("Other's Public Key: " + Base64.getEncoder().encodeToString(otherClient.getOtherPublicKey().getEncoded()));
                System.out.println("Shared secret key: " + Base64.getEncoder().encodeToString(otherClient.getSecretKeyWithOther().getEncoded()));
                System.out.println("Shared IV: " + Base64.getEncoder().encodeToString(otherClient.getIvWithOther().getIV()));
            } else {
                System.out.println("Other is inactive");
            }



        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    private static byte[] shortenKey(byte[] s) {
        byte[] b = new byte[16]; // For AES algo
        System.arraycopy(s, 0, b, 0, b.length);
        return b;
     }
}
