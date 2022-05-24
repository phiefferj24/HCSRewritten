package com.feinbergcs;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Handler;

public class Server {
    public static void main(String[] args) {
        Listener l = new Listener(9000);
        l.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            while ((line = in.readLine()) != null) {
                l.send(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Listener extends Thread {
        private ServerSocket serverSocket;
        private ArrayList<ServerThread> serverThreads = new ArrayList<ServerThread>();
        public Listener(int port) {
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ServerThread serverThread = new ServerThread(socket);
                    serverThread.start();
                    serverThreads.add(serverThread);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void send(String message) {
            for (ServerThread serverThread : serverThreads) {
                serverThread.send(message);
            }
        }
    }
    public static class ServerThread extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        public ServerThread(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            try {
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        break;
                    }
                    System.out.println("Recieved: " + input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void send(String message) {
            out.println(message);
        }
    }
}
