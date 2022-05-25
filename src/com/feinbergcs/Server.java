package com.feinbergcs;

import java.net.*;
import java.io.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    public static LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
    public static int ppl = 0;
    public static final int WORLD_WIDTH = 10000;
    public static final int WORLD_HEIGHT = 10000;
    public static void main(String[] args) throws InterruptedException {
        Listener l = new Listener(9001);
        l.start();
        for(int i = 0; i < 9500; i+=Math.random()*1200+400)
            for(int j = 0; j < 9500; j+=Math.random()*1200+400) {
                l.sprites.add(new Tree(i, j, 100, 100, "/tree.png"));

            }

        for(int i = 0; i < 9500; i+=Math.random()*2900+100)
            for(int j = 0; j < 9500; j+=Math.random()*2900+400) {
                l.sprites.add(new Zombie((int)(i+Math.random()*1200), (int)(j+Math.random()*1200), 100, 100,"/zombie.png"));
            }

        double lastTime = System.currentTimeMillis();
        int reccount = 0;
        ArrayList<String> sentPlayers = new ArrayList<>();
        while(true) {
            String message = messages.take();
            reccount++;
            double delta = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();
            if(!message.equals("disconnected")) {
                double time = System.currentTimeMillis();
                String[] split = message.split(",");
                for (int j = 0; j < l.sprites.size(); j++) {
                    Sprite sprite = l.sprites.get(j);
                    for (int i = 0; i < split.length - 1; i++) {
                        if (split[i].contains(sprite.getId())) {
                            sprite.updateToString(split[i]);
                            split[i] = "";
                        }
                    }
                }
                for (int i = 0; i < split.length - 1; i++) {
                    if (!split[i].equals("")) {
                        if (split[i].contains("player")) {
                            l.sprites.add(new Player(split[i]));
                            sentPlayers.add(l.sprites.get(l.sprites.size() - 1).getId());
                        } else if (split[i].contains("bullet")) {
                            l.sprites.add(new Bullet(split[i]));
                        } else if (split[i].contains("tree")) {
                            l.sprites.add(new Tree(split[i]));
                        } else if (split[i].contains("zombie")) {
                            l.sprites.add(new Zombie(split[i]));
                        }
                    }
                }
            }
            if(reccount >= ppl) {
                for(int i = 0; i < l.sprites.size(); i++) {
                    Sprite s = l.sprites.get(i);
                    if(!s.image.contains("player")) {
                        s.step(delta);
                        if(s.image.contains("bullet")) {
                            if(s.x < 0 || s.x > WORLD_WIDTH || s.y < 0 || s.y > WORLD_HEIGHT) {
                                l.sprites.remove(s);
                                i--;
                            }
                        }
                        if(s.image.contains("zombie")) {
                            ((Zombie)s).step(delta,l.sprites);
                        }
                        else
                            s.step(delta);
                    }
                }//TODO delta time?

                StringBuilder messageBuilder = new StringBuilder();
                for(int i = 0; i < l.sprites.size(); i++) {
                    messageBuilder.append(l.sprites.get(i).toString()).append(",");
                }
                messageBuilder.append(0);
                message = messageBuilder.toString();
                l.send(message);
                for(int i = 0; i < l.sprites.size(); i++) {
                    if(l.sprites.get(i).image.contains("player") && !sentPlayers.contains(l.sprites.get(i).getId())) {
                        l.sprites.remove(i);
                        i--;
                    }
                }
                reccount = 0;
                sentPlayers = new ArrayList<>();
            }
        }
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
                    System.out.println("New connection");
                    ppl++;
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
            messages.add(message);
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
                messages.add("disconnected");
                ppl--;
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
