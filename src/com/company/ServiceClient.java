package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.security.*;
import java.util.Arrays;


public class ServiceClient implements Runnable {

    private Socket clientSocket;
    private BufferedReader in = null;

    public ServiceClient(Socket client) {
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            String clientSelection;
            while ((clientSelection = in.readLine()) != null) {
                switch (clientSelection) {
                    case "LIST / AFTP/1.0":
                        listFile();
                        continue;
                    case "PUT / AFTP/1.0":
                        receiveFile();
                        continue;
                    case "GET / AFTP/1.0":
                        String outGoingFileName;
                        while ((outGoingFileName = in.readLine()) != null) {
                            sendFile(outGoingFileName);
                        }
                        continue;
                    case "DELETE / AFTP/1.0":
                        deleteFile();
                        continue;
                    case "EXIT":
                        System.exit(1);
                        break;
                    default:
                        System.out.println("Incorrect command received.");
                        break;
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void listFile() throws IOException {
        //@micheal dit is nieuw
        //ik weet helaas niet hoe ik de inhoud correct achter de file+timestamp kan zetten en deze weer goed terug naar de client kan sturen.
        //de methode readAllBytes moet hier mogelijklijk voor aangepast worden.
        DataOutputStream dos = null;
        ArrayList<String> files = new ArrayList<String>();
        try {
            File folder = new File("C:\\Users\\mvandalen\\IdeaProjects\\NetwerkenEindopdrachtServer\\out\\FileFolder");
            File[] listOfFiles = folder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {

                String file = null;
                //set the line to te correct format (fileName UnixTimestamp data)
                //listOfFiles[i].lastModified() / 1000L zorgt ervoor dat de unix timestamp het correcte formaat is.
                file = listOfFiles[i].getName() + " " + (listOfFiles[i].lastModified() / 1000L)+ " " + readAllBytes(folder +"\\"+ listOfFiles[i].getName());
                files.add(file);
            }

            /////////////////////////////
            OutputStream os = clientSocket.getOutputStream();  //handle file send over socket
            /*DataOutputStream*/
            dos = new DataOutputStream(os); //Sending file name and file size to the server
            dos.writeUTF("AFTP/1.0 200 OK\r\n");
            dos.writeUTF("Content-Length: " + files.size() +"\r\n\r\n");
            for (String file : files ) {
                dos.writeUTF(file+ "\r\n");
            }
            dos.flush();
            dos.close();
        } catch (IOException ex) {
            System.err.println("500 Server Error");
        }
    }
    public void receiveFile() {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());

            String fileName = clientData.readUTF();
            OutputStream output = new FileOutputStream(fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            output.close();
            clientData.close();

            System.out.println("File "+fileName+" received from client.");
        } catch (IOException ex) {
            System.err.println("Client error. Connection closed.");
        }
    }

    public void sendFile(String fileName) {

        DataOutputStream dos = null;
        try {
            File myFile = new File(fileName);  //handle file reading
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);


            OutputStream os = clientSocket.getOutputStream();  //handle file send over socket
            /*DataOutputStream*/ dos = new DataOutputStream(os); //Sending file name and file size to the server

            dos.writeUTF("GET AFTP/1.0 200 OK");
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            System.out.println("File "+fileName+" sent to client.");

        } catch (Exception e) {
            System.err.println("File does not exist!");
            try {
                dos.writeUTF("GET AFTP/1.0 404 Not found");
            } catch (IOException ex) {
                System.err.println("Error sending error");
            }
        }
    }

    private void deleteFile() {
    }

    private static String readAllBytes(String filePath)
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return content;
    }
}
