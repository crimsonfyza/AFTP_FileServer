package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;



public class ServiceClient implements Runnable {

    /**
     * @param clientSocket   De connectie met de server over een specifieke poort
     * @param in       Buffer om de inputstream heen, haalt input uit de commandline
     * @param defaultPath default location where files are for commands: GET,PUT,DELETE,LIST
     **/

    private Socket clientSocket;
    private BufferedReader in = null;

    public ServiceClient(Socket client) {
        //bind connected client to socket.
        this.clientSocket = client;
    }


    @Override
    public void run() {
        try {

            in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            String clientSelection;
            //start reading client input
            while ((clientSelection = in.readLine()) != null) {
                // A switch that handles input
                String inputArray[] = clientSelection.split(" ", 3);

                switch (inputArray[0]) {
                    case "LIST":
                        //listFiles();
                        continue;
                    case "PUT":
                        putFileOnServer();
                        continue;
                    case "GET":
                        getFileFromServer(inputArray[1]);
                        continue;
                    case "DELETE":

                        deleteFile(inputArray[1]);

                        continue;
                    default:
                        //unknown command has been given
                        returnStatus("<AFTP/1.0 400 Bad request");
                        break;
                }
            }
        } catch (IOException ignored) {
        }
    }
    //
//    private void listFiles() {
//        DataOutputStream dos = null;
//        ArrayList<String> files = new ArrayList<String>();
//        try {
//            File folder = new File(defaultPath);
//            File[] listOfFiles = folder.listFiles();
//
//            for (int i = 0; i < listOfFiles.length; i++) {
//
//                String file;
//                //file = listOfFiles[i].getName() + " " + (listOfFiles[i].lastModified() / 1000L)+ " " + readAllBytes(folder +"\\"+ listOfFiles[i].getName());
//                String getName = listOfFiles[i].getName();
//                Long lastChanged = (listOfFiles[i].lastModified() / 1000L);
//                String readBytes = readAllBytes(folder +"\\"+ listOfFiles[i].getName());
//
//                String output = getName + " " + lastChanged;
//
//                files.add(output);
//            }
//
//            /////////////////////////////
//            OutputStream os = clientSocket.getOutputStream();  //handle file send over socket
//            dos = new DataOutputStream(os); //Sending file name and file size to the server
//            dos.writeUTF("<AFTP/1.0 200 OK\r\n");
//            dos.writeUTF("Content-Length: " + files.size() +"\r\n\r\n");
//            for (String file : files ) {
//                dos.writeUTF(file+ "\r\n");
//            }
//            //List can be writter and send but client cannot fully read it all (yet)
//            dos.flush();
//            dos.close();
//        } catch (IOException ex) {
//            System.err.println("<AFTP/1.0 500 Server Error");
//        }
//    }
    public void putFileOnServer() throws IOException {

        // 200 OK
        // 423 Locked
        String filePath = null;
        OutputStream output = null;
        DataInputStream clientData = null;

        try {
            int bytesRead;

            clientData = new DataInputStream(clientSocket.getInputStream());

            String fileName = clientData.readUTF();
            filePath = fileName;
            output = new FileOutputStream(filePath);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            output.close();

            System.out.println("File "+fileName+" received from client.");
            returnStatus("<AFTP/1.0 200 OK");

        } catch (IOException ex) {
            System.err.println("Client error. Connection closed.");
            File filePathCheck = new File(filePath);
            Boolean defaultPathCheck = filePathCheck.exists();
            //error file couldnt upload
            // so if the file existed, it would be locked, if it didnt exist theres a server error.
            if (defaultPathCheck == true) {
                //overwrite failed
                returnStatus("<AFTP/1.0 423 Locked");
            } else {
                //new file couldnt be made.
                returnStatus("<AFTP/1.0 500 Server Error");
            }
            output.close();

        }
    }

    public void getFileFromServer(String fileName) throws IOException {
        String FilePathName;

        FilePathName = fileName;
        try {
            File myFile = new File(FilePathName);  //handle file reading
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            //creating outputstream for return value
            OutputStream os = clientSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            dos.writeUTF("<AFTP/1.0 200 OK");
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            dos.close();
            System.out.println("File "+fileName+" sent to client.");

        } catch (Exception e) {
            System.err.println("File does not exist!");
            returnStatus("<AFTP/1.0 404 Not found");

        }
    }

    private void deleteFile(String fileName) throws IOException {

        String fullPath = fileName;
        File file = new File(fullPath);

        File checkFolder = new File(fullPath);
        Boolean resultCheckFolder = checkFolder.exists();
        //if folder doesnt exist throw
        if (resultCheckFolder == true) {
            //Run delete function and check if it worked.
            if (file.delete()) {
                returnStatus("<AFTP/1.0 200 OK");
            } else {
                //if file is in use send 423
                returnStatus("<AFTP/1.0 423 Locked");
            }
        } else {
            returnStatus("<AFTP/1.0 404 Not found");
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

    private void returnStatus (String status) throws IOException {
        DataOutputStream dos = null;
        OutputStream os = clientSocket.getOutputStream();

        dos = new DataOutputStream(os);
        dos.writeUTF(status);
        dos.flush();
    }

}