package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;


public class ServiceClient implements Runnable {
    private Socket clientSocket;
    private BufferedReader in = null;
    private String share;
    private String shareName;
    private ArrayList<String> listFiles;
    private String invokedCommand;

    /**
     * Binds the connecting client to a socket
     *
     * @param client The client's socket
     **/
    public ServiceClient( Socket client ) {
        this.clientSocket = client;
    }


    /**
     * This method keeps the program running using a while loop and decides what method to call, depending on the user input.
     **/
    @Override
    public void run() {
        try {
            share = "Share\\";
            shareName = "Share";

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String clientSelection;
            // start reading client input
            while ((clientSelection = in.readLine()) != null) {
                // A switch that handles input
                String inputArray[] = clientSelection.split(" ", 3);
                invokedCommand = clientSelection;

                switch (inputArray[0]) {
                    case "LIST":
                    case "SYNCH":
                        listFiles();
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
                        // In case an unknown command is given, a 400 response is given
                        returnStatus("<AFTP/1.0 400 Bad request");
                        createLogRow("<AFTP/1.0 400 Bad request");
                        break;
                }
            }
        } catch (IOException ignored) {

        }
    }


    /**
     * The function associated with the LIST command, which sends a list with all the files on the server to the client.
     **/
    private void listFiles() throws IOException {
        listFiles = new ArrayList<>();
        folderWalker(share);

        // Creating output stream for return value
        OutputStream os = clientSocket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);

        ArrayList<String> UTF = new ArrayList<>();
        UTF.add("<AFTP/1.0 200 OK");
        createLogRow("<AFTP/1.0 200 OK");

        for (String path : listFiles) {
            File tempFile = new File(path);

            String currentName = path;
            long currentDate = tempFile.lastModified();
            UTF.add(currentName + " "+ currentDate);
        }

        dos.writeUTF(UTF.toString());
    }


    /**
     * Walks through all subfolders on the client, depending on the given path.
     * This function is used in the LIST command.
     *
     * @param path The path which is being checked
     **/
    public void folderWalker( String path ) {
        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                // Get all folders
                String[] editPathName = f.getAbsolutePath().split(shareName);
                String outValue = shareName + editPathName[1];
                listFiles.add(outValue);

                folderWalker( f.getAbsolutePath());

            }
            else {
                String[] editPathName = f.getAbsolutePath().split(shareName);
                String outValue = shareName + editPathName[1];
                listFiles.add(outValue);
            }
        }
    }


    /**
     * The function associated with the PUT command, which puts a file on the server.
     **/
    public void putFileOnServer() throws IOException {
        String filePath = null;
        OutputStream output = null;
        DataInputStream clientData = null;

        try {
            int bytesRead;

            clientData = new DataInputStream(clientSocket.getInputStream());

            String fileName = clientData.readUTF();
            filePath = share + fileName;
            output = new FileOutputStream(filePath);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];

            // For every iteration, a part of 1024 bytes is received through the file output stream
            // The progress is checked through the long "size", this long is initially the size of the file and in every loop
            // the part that is received is substracted from the long, thus keeping track of the progress
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            output.close();

            returnStatus("<AFTP/1.0 200 OK");
            createLogRow("<AFTP/1.0 200 OK");

        } catch (IOException ex) {
            System.err.println("Client error. Connection closed.");
            File filePathCheck = new File(filePath);
            Boolean defaultPathCheck = filePathCheck.exists();

            // Error: file couldn't upload
            // If the file existed, it would be locked, if it didn't exist there's a server error.
            if (defaultPathCheck == true) {
                returnStatus("<AFTP/1.0 423 Locked");
                createLogRow("<AFTP/1.0 423 Locked");
            } else {
                returnStatus("<AFTP/1.0 500 Server Error");
                createLogRow("<AFTP/1.0 500 Server Error");
            }
            output.close();
        }
    }


    /**
     * The function associated with the GET command, which sends a file to the client.
     *
     * @param fileName The name of the file which is going to be sent to the client
     **/
    public void getFileFromServer( String fileName ) {
        String fullPath = "Share\\" + fileName;

        try {
            OutputStream os = clientSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            File myFile = new File(fullPath);

            if(!(myFile.exists())) {
                // In case the file doesn't exist a 404 response is printed
                dos.writeUTF("<AFTP/1.0 404 Not found");
                createLogRow("<AFTP/1.0 404 Not found");
                dos.flush();
            } else {
                byte[] byteArray = new byte[(int) myFile.length()];

                FileInputStream fis = new FileInputStream(myFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis);
                dis.readFully(byteArray, 0, byteArray.length);

                dos.writeUTF("<AFTP/1.0 200 OK");
                createLogRow("<AFTP/1.0 200 OK");
                dos.writeLong(byteArray.length);
                dos.write(byteArray, 0, byteArray.length);
                dos.flush();
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }


    /**
     * The function associated with the DELETE command, which deletes a file on the server.
     *
     * @param fileName The name of the file which is going to be deleted
     **/
    private void deleteFile( String fileName ) throws IOException {
        String fullPath = share + fileName;
        File file = new File(fullPath);

        File checkFolder = new File(fullPath);
        Boolean resultCheckFolder = checkFolder.exists();

        // First a check if the folder exists, if it doesn't exist a 404 response is returned to the client
        if (resultCheckFolder == true) {
            if (file.delete()) {
                // If a file is successfully deleted a 200 response is returned to the client
                returnStatus("<AFTP/1.0 200 OK");
                createLogRow("<AFTP/1.0 200 OK");
            } else {
                // If a file is in use a 423 response is returned to the client
                returnStatus("<AFTP/1.0 423 Locked");
                createLogRow("<AFTP/1.0 423 Locked");
            }
        } else {
            returnStatus("<AFTP/1.0 404 Not found");
            createLogRow("<AFTP/1.0 404 Not found");
        }
    }


    /**
     * Sends a repsonse to the client
     *
     * @param status The response that is being send to the client
     **/
    private void returnStatus( String status ) throws IOException {
        DataOutputStream dos = null;
        OutputStream os = clientSocket.getOutputStream();

        dos = new DataOutputStream(os);
        dos.writeUTF(status);
        dos.flush();
    }

    private String createTimestamp () {
        Date date = new Date();
        Timestamp ts=new Timestamp(date.getTime());
        return ts.toString();
    }

    private String getClientIP () {
        return clientSocket.getRemoteSocketAddress().toString();
    }

    private void createLogRow (String status) throws IOException {
        String timeStamp = createTimestamp();
        String clientIP = getClientIP();

        String logRow = timeStamp + " || " + clientIP + " || " + invokedCommand + " || " + status;
        System.out.println(logRow);
        usingBufferedWritter(logRow);
    }

    public static void usingBufferedWritter(String addLog) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(
                new FileWriter("logfile.txt", true)  //Set true for append mode
        );
        writer.newLine();   //Add new line
        writer.write(addLog);
        writer.close();
    }
}