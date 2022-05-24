package com.feinbergcs;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        Listener l = new Listener(9001);
        l.start();
    }

    public static class Listener extends Thread {
        private ServerSocket serverSocket;
        private ArrayList<ServerThread> serverThreads = new ArrayList<ServerThread>();
        private ArrayList<Sprite> sprites;

        public Listener(int port) {
            sprites = new ArrayList<>();
            //sprites.add(new Tree(50,50,100,100, "/tree.png"));
            //sprites.add(new Zombie(250,250,50,50,"/zombie.png"));
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
            sprites.clear();
            String[] split = m.split(",");
            for (int i = 0; i < split.length - 1; i++) {
                String ma = split[i];
                if(ma.contains("tree")) {
                    sprites.add(new Tree(ma));
                } else if (ma.contains("zombie")) {
                    sprites.add(new Zombie(ma));
                } else if (ma.contains("bullet")) {
                    sprites.add(new Bullet(ma));
                } else if (ma.contains("player")) {
                    sprites.add(new Player(ma));
                } else if (ma.contains("pig")) {
                    sprites.add(new Pig(ma));
                }
            }
            double time = Double.parseDouble(split[split.length - 1]);
            double delta = time - System.currentTimeMillis();
            sprites.forEach((s) -> s.step(delta));//TODO delta time?

            StringBuilder messageBuilder = new StringBuilder();
            for(Sprite s: sprites)
                 messageBuilder.append(s.toString()).append(",");
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
