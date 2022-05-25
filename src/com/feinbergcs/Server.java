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
            String[] split = m.split(",");
            for(int j = 0; j < sprites.size(); j++) {
                Sprite sprite = sprites.get(j);
                for (int i = 0; i < split.length - 1; i++) {
                    if(split[i].contains(sprite.getId())) {
                        sprite.updateToString(split[i]);
                        split[i] = "";
                    }
                }
            }
            for(int i = 0; i < split.length - 1; i++) {
                if(!split[i].equals("")) {
                    if(split[i].contains("player")) {
                        sprites.add(new Player(split[i]));
                    } else if(split[i].contains("bullet")) {
                        sprites.add(new Bullet(split[i]));
                    } else if(split[i].contains("tree")) {
                        sprites.add(new Tree(split[i]));
                    } else if(split[i].contains("zombie")) {
                        sprites.add(new Zombie(split[i]));
                    }
                }
            }
            double time = Double.parseDouble(split[split.length - 1]);
            double delta = time - System.currentTimeMillis();
            time = System.currentTimeMillis();
            sprites.forEach((s) -> s.step(delta));//TODO delta time?

            StringBuilder messageBuilder = new StringBuilder();
            for(Sprite s: sprites)
                 messageBuilder.append(s.toString()).append(",");
            messageBuilder.append(time);
            message = messageBuilder.toString();
            send(message);
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
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
