//package com.company;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.Date;
//
//public class Main {
//    private static ServerSocket server;
//    static ServerSocket socket;
//    protected final static int port = 8081;
//    static Socket connection;
//
//    static boolean first;
//    static StringBuffer process;
//    static String TimeStamp;
//
//    public static void main(String args[]) throws IOException, ClassNotFoundException{
//        try{
//            System.out.println("porn");
//            String strCurrentLine;
//            server = new ServerSocket(port);
//            //keep listens indefinitely until receives 'exit' call or program terminates
//            while(true) {
//
//                connection = server.accept();
//                //if ()
//                    OutputStream output = connection.getOutputStream();
//                System.out.println(" ");
//                BufferedReader Input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                System.out.println(Input.readLine());
//                //            String method = readInput(Input);
//                //            System.out.println(method);
//
////                            Input.lines().forEach( line -> {
////                                System.out.println(line);
////                            });
//
//
//                Date today = new Date();
//                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
//                output.write(httpResponse.getBytes("UTF-8"));
//                output.close();
//            }
//        } catch (IOException e) {}
//        try {
//            connection.close();
//        }
//        catch (IOException e) {}
//    }
//    public static String readInput(BufferedReader input) throws IOException {
//        String Line = input.readLine();
//        if (Line == "GET / HTTP/1.1") {
//            return "GET";
//        } else if (Line == "LIST / HTTP/1.1") {
//            return "LIST";
//        } else if (Line == "POST / HTTP/1.1") {
//            return "POST";
//        } else if (Line == "DELETE / HTTP/1.1"){
//            return "DELETE";
//        } else {
//            return "no method";
//        }
//
//    }
//}