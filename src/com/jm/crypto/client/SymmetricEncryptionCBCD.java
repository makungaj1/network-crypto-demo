package com.jm.crypto.client;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class SymmetricEncryptionCBCD {
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

            // Get shared secret object from the server
            SharedSecret sharedSecret = (SharedSecret) objFromServer.readObject();
            Key key = sharedSecret.getKey();
            IvParameterSpec ivSpec = new IvParameterSpec(sharedSecret.getIvByte());

            System.out.println("Key received from server");
            System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
            System.out.println("ivSec received: " + Base64.getEncoder().encodeToString(ivSpec.getIV()));

            Scanner input = new Scanner(System.in);

            while (true) {
                System.out.print("\nyou: ");
                String msg = input.nextLine();

                // Encrypt msg
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
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
                cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
                byte[] decryptedMsg = cipher.doFinal(messageObject1.getMsg());
                System.out.println("decrypted msg: " + new String(decryptedMsg));
            }

        } catch (IOException | ClassNotFoundException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
}
