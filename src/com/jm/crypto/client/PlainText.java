package com.jm.crypto.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class PlainText {
    public static void main(String[] args) {
        try {
            // Connect to server
            Socket server = new Socket(Utils.getServerIP(), Utils.getServerPort());

            System.out.println("Connected to server");
            System.out.println("Server name: " + server.getInetAddress().getHostName());
            System.out.println("Server IP: " + server.getInetAddress().getHostAddress());
            System.out.println("Server PORT: " + server.getLocalPort());

            // Create data input and output streams
            DataInputStream fromServer = new DataInputStream(server.getInputStream());
            DataOutputStream toServer = new DataOutputStream(server.getOutputStream());

            Scanner input = new Scanner(System.in);

            while (true) {
                String msgFromServer = fromServer.readUTF();
                System.out.println(msgFromServer);

                System.out.print("\nyou: ");
                String msg = input.nextLine();

                toServer.writeUTF("client: " + msg);
                toServer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
