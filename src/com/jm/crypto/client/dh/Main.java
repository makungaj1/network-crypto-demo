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
    private static final String MY_IP = "192.168.0.12";
    private static final String OTHER_IP = "192.168.0.5";

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


            // Connect to a friend
            boolean connectToAFriend = true;

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            WrapperObject wrapperObject;

            byte[] from;
            byte[] to;
            byte[] objective;

            while (true) {

                if (connectToAFriend) {
                    System.out.println("Connect to a friend");
                    // Request a public key of an IP (Other client: 192.168.0.5)
                    // Encrypt data to send to server
                    cipher.init(Cipher.ENCRYPT_MODE, serverProxy.getSecretKeyWithServer(), serverProxy.getIvWithServer());
                    from = cipher.doFinal(MY_IP.getBytes());
                    System.out.println("From: " + Base64.getEncoder().encodeToString(from) + " (" + MY_IP + ")");
                    to = cipher.doFinal(OTHER_IP.getBytes());
                    System.out.println("To: " + Base64.getEncoder().encodeToString(to) + " (" + OTHER_IP + ")");
                    objective = cipher.doFinal("Initial".getBytes());
                    System.out.println("Objective: " + Base64.getEncoder().encodeToString(objective) + " (initial)");
                    wrapperObject = new WrapperObject(from, to, objective);
                    objToServer.writeObject(wrapperObject);
                    objToServer.flush();
                }

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

                String obj = new String(objective);
                OtherClient otherClient = wrapperObject.getOtherClient();

                if (obj.equalsIgnoreCase("active") || obj.equalsIgnoreCase("initial")) {
                    System.out.println("Other is active");

                    // Calculate the shared secret key with other
                    keyAgreement.doPhase(otherClient.getOtherPublicKey(), true);
                    otherClient.setSecretKeyWithOther(new SecretKeySpec(shortenKey(keyAgreement.generateSecret()), "AES"));

                    // Print other's keys
                    System.out.println("Other's Public Key: " + Base64.getEncoder().encodeToString(otherClient.getOtherPublicKey().getEncoded()));
                    System.out.println("Shared secret key: " + Base64.getEncoder().encodeToString(otherClient.getSecretKeyWithOther().getEncoded()));
                    System.out.println("Shared IV: " + Base64.getEncoder().encodeToString(otherClient.getIvByte()));

                    // If someone wants to talk (sends an initial request, respond back with hello message
                    // To kickoff the chat
                    if (obj.equalsIgnoreCase("initial")) {
                        cipher.init(Cipher.ENCRYPT_MODE, serverProxy.getSecretKeyWithServer(), serverProxy.getIvWithServer());
                        from = cipher.doFinal(MY_IP.getBytes());
                        to = cipher.doFinal(OTHER_IP.getBytes());
                        objective = cipher.doFinal("insta-chat".getBytes());

                        // Initial message
                        // Encrypt it with only the key shared with other. so the server won't see the decrypted msg
                        cipher.init(Cipher.ENCRYPT_MODE, otherClient.getSecretKeyWithOther(), new IvParameterSpec(otherClient.getIvByte()));
                        byte[] initMsg = cipher.doFinal((MY_IP + " is ready to chat").getBytes());

                        WrapperObject wo = new WrapperObject(from, to, objective);
                        wo.setMessageBody(initMsg);

                        objToServer.writeObject(wo);
                        objToServer.flush();
                    }

                } else if (obj.equalsIgnoreCase("insta-chat")) {

                    // Decrypt the message
                    cipher.init(Cipher.DECRYPT_MODE, otherClient.getSecretKeyWithOther(),new IvParameterSpec(otherClient.getIvByte()));
                    byte[] decryptedMsg = cipher.doFinal(wrapperObject.getMessageBody());
                    System.out.println("Encrypted msg: " + Base64.getEncoder().encodeToString(wrapperObject.getMessageBody()));
                    System.out.println("Decrypted msg: " + new String(decryptedMsg));

                    cipher.init(Cipher.ENCRYPT_MODE, serverProxy.getSecretKeyWithServer(), serverProxy.getIvWithServer());
                    from = cipher.doFinal(MY_IP.getBytes());
                    to = cipher.doFinal(OTHER_IP.getBytes());
                    objective = cipher.doFinal("insta-chat".getBytes());

                    // Encrypt it with only the key shared with other. so the server won't see the decrypted msg
                    cipher.init(Cipher.ENCRYPT_MODE, otherClient.getSecretKeyWithOther(), new IvParameterSpec(otherClient.getIvByte()));
                    Scanner input = new Scanner(System.in);

                    System.out.print("You: ");
                    byte[] initMsg = cipher.doFinal(input.nextLine().getBytes());

                    WrapperObject wo = new WrapperObject(from, to, objective);
                    wo.setMessageBody(initMsg);

                    objToServer.writeObject(wo);
                    objToServer.flush();
                } else {
                    System.out.println("Other is inactive");
                }
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
