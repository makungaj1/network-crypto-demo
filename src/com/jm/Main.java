package com.jm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Server started");
            // Create a server socket
            ServerSocket server = new ServerSocket(8000);
            System.out.println("Local Port: " + server.getLocalPort());
            System.out.println("IP: " + server.getInetAddress().getHostAddress());

            // Listen for connection
            Socket clientSocket = server.accept();
            System.out.println("Client connected");
            System.out.println("Client IP: " + clientSocket.getInetAddress().getHostAddress());

            // Create data input and output streams
            DataInputStream inputFromClient = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outputToClient = new DataOutputStream(clientSocket.getOutputStream());

            int count = 0;
            while (true) {
                System.out.println("Loop " + count);
                double radius;

                try {
                    radius = inputFromClient.readDouble();

                } catch (EOFException e) {
                    System.out.println("Client de-connected");
                    break;
                }

                System.out.println("Radius received: " + radius);

                double area = radius * radius * Math.PI;
                System.out.println("Area calculated: " + area);
                System.out.println("---------------------------------End loop " + count);

                outputToClient.writeDouble(area);

                count++;
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
