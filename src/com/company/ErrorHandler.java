package com.company;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ErrorHandler {

    private Socket clientSocket;
    OutputStream os = clientSocket.getOutputStream();
    DataOutputStream dos = new DataOutputStream(os);

    public String notFound;
    {
        try {
            dos.writeUTF("AFTP/1.0 404 Not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ErrorHandler() throws IOException {
    }
}