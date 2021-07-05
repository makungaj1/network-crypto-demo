package com.jm.crypto.client;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;

public class AsymmetricEncryption {
    public static void main(String[] args) {
        try {
            // Connect to server
            Socket server = new Socket(Utils.getServerIP(), Utils.getServerPort());

            System.out.println("Connected to server");
            System.out.println("Server name: " + server.getInetAddress().getHostName());
            System.out.println("Server IP: " + server.getInetAddress().getHostAddress());
            System.out.println("Server PORT: " + server.getLocalPort());

            // Create data input and output streams
            ObjectOutputStream objToServer = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream objFromServer = new ObjectInputStream(server.getInputStream());

            // Generate Private/Public keys
            KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
            kpGen.initialize(1024);
            KeyPair keyPair = kpGen.generateKeyPair();

            Key privateKey = keyPair.getPrivate();
            System.out.println("Client Private key: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            Key publicKey = keyPair.getPublic();
            System.out.println("Client Public key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));

            // Get Server Public key
            Key serverPublicKey = (Key) objFromServer.readObject();
            System.out.println("Server Public key: " + Base64.getEncoder().encodeToString(serverPublicKey.getEncoded()));

            // Send client public key to server
            objToServer.writeObject(publicKey);

            Scanner input = new Scanner(System.in);

            while (true) {
                System.out.print("\nyou: ");
                String msg = input.nextLine();

                // Encrypt msg
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                byte[] encryptedMsg = cipher.doFinal(msg.getBytes());
                System.out.println("encrypted msg");
                System.out.println(Base64.getEncoder().encodeToString(encryptedMsg));

                // Send message to server
                MessageObject messageObject = new MessageObject("client", encryptedMsg);
                objToServer.writeObject(messageObject);
                objToServer.flush();

                // Get message from Server
                MessageObject messageObject1 = (MessageObject) objFromServer.readObject();
                System.out.println("From: " + messageObject1.getFrom());
                System.out.println("Encrypted msg: " + Base64.getEncoder().encodeToString(messageObject1.getMsg()));

                // Decrypt message
                cipher.init(Cipher.DECRYPT_MODE, serverPublicKey);
                byte[] decryptedMsg = cipher.doFinal(messageObject1.getMsg());
                System.out.println("decrypted msg: " + new String(decryptedMsg));
            }

        } catch (IOException | ClassNotFoundException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }
}
