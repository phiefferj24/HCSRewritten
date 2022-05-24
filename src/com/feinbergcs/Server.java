package com.feinbergcs;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Handler;

public class Server {
    public static void main(String[] args) {
        Listener l = new Listener(9000);
        l.start();
    }

    public static class Listener extends Thread {
        private ServerSocket serverSocket;
        private ArrayList<ServerThread> serverThreads = new ArrayList<ServerThread>();
        private ArrayList<Sprite> sprites;

        public Listener(int port) {
            sprites = new ArrayList<>();
            sprites.add(new Tree(50,50,100,100,"/wood.png"));
            sprites.add(new Zombie(250,250,50,50,"/zombie.png"));
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
                    ServerThread serverThread = new ServerThread(socket, this);
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
        public void onMessage(String message) {
            String m = message;
            System.out.println(message);//[300bf3d3-b601-400b-ba9f-609a2ac6cf29;469;457;150;150;/player.png;-2.3695780122792685],

            sprites.forEach((s) -> s.step(1));//TODO delta time?

            StringBuilder messageBuilder = new StringBuilder("[");
            for(Sprite s: sprites)
                 messageBuilder.append(s.toString()).append("],");
            messageBuilder.append(m);
            message = messageBuilder.toString();


            send(message.substring(0,message.length()-1));
        }
    }
    public static class ServerThread extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Listener listener;
        public ServerThread(Socket socket, Listener listener) {
            this.listener = listener;
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
                    listener.onMessage(input);
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
