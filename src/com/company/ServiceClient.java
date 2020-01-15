package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;



public class ServiceClient implements Runnable {

    private Socket clientSocket;
    private BufferedReader in = null;
    private String defaultPath;

    public ServiceClient(Socket client) {
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {

            defaultPath = "C:\\Users\\Fyza\\IdeaProjects\\AFTP_FileServer\\FileFolder\\";

            in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            String clientSelection;
            while ((clientSelection = in.readLine()) != null) {
                switch (clientSelection) {
                    case "LIST":
                        listFiles();
                        continue;
                    case "PUT":
                        putFileOnServer();
                        continue;
                    case "GET":
                        String fileToGet;
                        while ((fileToGet = in.readLine()) != null) {
                            getFileFromServer(fileToGet);
                        }
                        continue;
                    case "DELETE":
                        String fileToDelete;
                        while ((fileToDelete = in.readLine()) != null) {
                            deleteFile(fileToDelete);
                        }
                        continue;
                    default:
                        System.out.println("Incorrect command received.");
                        break;
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void listFiles() throws IOException {
        DataOutputStream dos = null;
        ArrayList<String> files = new ArrayList<String>();
        try {
            File folder = new File(defaultPath);
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
    public void putFileOnServer() {

        // 200 OK
        // 423 Locked


        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());

            String fileName = clientData.readUTF();
            String filePath = defaultPath + fileName;
            OutputStream output = new FileOutputStream(filePath);
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

    public void getFileFromServer(String fileName) {
        String FilePathName;

        //200 Ok *
        //404 Not Found *
        //418 Gone **


        FilePathName = defaultPath + fileName;
        DataOutputStream dos = null;
        try {
            File myFile = new File(FilePathName);  //handle file reading
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            //creating outputstream for return value
            OutputStream os = clientSocket.getOutputStream();
            dos = new DataOutputStream(os);

            dos.writeUTF("200 OK");
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            System.out.println("File "+fileName+" sent to client.");

        } catch (Exception e) {
            System.err.println("File does not exist!");
            try {
                OutputStream os = clientSocket.getOutputStream();
                dos = new DataOutputStream(os);
                dos.writeUTF("404 Not found");
                dos.flush();
            } catch (IOException ex) {
                System.err.println("Cannot send to client");
            }
        }
    }

    private void deleteFile(String fileName) {

        String fullPath = defaultPath + fileName;
        File file = new File(fullPath);

        //functie omheen check of bestaat ja door nee 404 Not found
        //
        //
        if(file.delete())
        {

            try {
                //creating outputstream for return value
                DataOutputStream dos = null;
                OutputStream os = clientSocket.getOutputStream();

                dos = new DataOutputStream(os);
                dos.writeUTF("200 OK");
                dos.flush();

                System.out.println("File deleted successfully, response send to client");

            } catch (IOException e) {
                System.out.println("File deleted successfully, could not return response to client");
            }

        }
        else
        {
            System.out.println("Failed to delete the file, in use 423 locked");
            try {
                DataOutputStream dos = null;
                OutputStream os = clientSocket.getOutputStream();

                dos = new DataOutputStream(os);
                dos.writeUTF("423 OK");
                dos.flush();

            } catch (IOException e) {
                System.out.println("Failed not deleted, cannot send respond to client.");
            }

        }

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
