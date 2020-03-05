package com.company;

import java.net.*;
import java.io.*;

public class FileServer {
    private static ServerSocket serverSocket;
    private static Socket clientSocket = null;

    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(25444);
            System.out.println("Server started.");
        } catch (Exception e) {
            System.err.println("Port already in use.");
            System.exit(1);
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Accepted connection : " + clientSocket);

                Thread t = new Thread(new ServiceClient(clientSocket));

                t.start();

                DataOutputStream dos = null;
                OutputStream os = clientSocket.getOutputStream();

                dos = new DataOutputStream(os);
                dos.writeUTF("<AFTP/1.0 200 OK - Server connected");
                dos.flush();
            } catch (Exception e) {
                DataOutputStream dos = null;
                OutputStream os = clientSocket.getOutputStream();

                dos = new DataOutputStream(os);
                dos.writeUTF("<AFTP/1.0 500 Server Error");
                dos.flush();
            }
        }
    }
}
