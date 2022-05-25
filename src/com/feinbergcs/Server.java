package com.feinbergcs;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.net.*;
import java.io.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    public static LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
    public static int ppl = 0;
    public static int numZombs = 0;
    public static final int WORLD_WIDTH = 10000;
    public static final int WORLD_HEIGHT = 10000;

    public static final double ZOMBIE_RATE = 0.0001;

    public static void main(String[] args) throws InterruptedException {
        Listener l = new Listener(9001);
        l.start();
        l.sprites.add(new Turret(700,700,100,100,"/turret.png"));


        for(int i = (int)(Math.random() * 100); i < 9500; i+=Math.random()*1200+400)
            for(int j = (int)(Math.random() * 100); j < 9500; j+=Math.random()*1200+400) {
                int w = (int)(Math.random()*200+100);
                l.sprites.add(new Tree(i, j, w, w, "/tree.png"));

            }

        for(int i = 1000; i < 9500; i+=Math.random()*2900+100)
            for(int j = 1000; j < 9500; j+=Math.random()*2900+400) {
                numZombs++;
                l.sprites.add(new Zombie((int)(i+Math.random()*120), (int)(j+Math.random()*120), 100, 100,"/zombie.png"));
            }

        double lastTime = System.currentTimeMillis();
        int reccount = 0;
        ArrayList<String> sentPlayers = new ArrayList<>();
        //double zombtime = 0;
        while(true) {
            while(numZombs<=80) {
                numZombs++;
                l.sprites.add(new Zombie((int) (Math.random() * 8000), (int) (Math.random() * 8000), 100, 100, "/zombie.png"));
            }

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
                        } else if (split[i].contains("wall")) {
                            l.sprites.add(new Wall(split[i]));
                        } else if (split[i].contains("turret")) {
                            l.sprites.add(new Turret(split[i]));
                        }
                    }
                }
            }
            if(reccount >= ppl) {
                for(int i = 0; i < l.sprites.size(); i++) {
                    Sprite s = l.sprites.get(i);
                    if(!s.image.contains("player")) {
                        if(s.image.contains("bullet")) {
                            Bullet b = (Bullet)s;
                            if(s.x < 0 || s.x > WORLD_WIDTH || s.y < 0 || s.y > WORLD_HEIGHT) {
                                l.sprites.remove(s);
                                i--;
                            } else {
                                for(int j = 0; j < l.sprites.size(); j++) {
                                    Sprite sprite = l.sprites.get(j);
                                    if(sprite.getImage().contains("wall") || sprite.getImage().contains("tree")) {
                                        Line2D bulletPath = new Line2D.Double(b.x, b.y, b.x + b.vx * b.speed * delta, b.y + b.vy * b.speed * delta);
                                        Rectangle2D wall = new Rectangle2D.Double(sprite.x, sprite.y, sprite.width, sprite.height);
                                        if(bulletPath.intersects(wall)) {
                                            l.sprites.remove(s);
                                            i--;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if(s.image.isEmpty()) {
                            l.sprites.remove(s);
                            i--;
                        }
                        if(s.image.contains("zombie")) {
                            ((Zombie)s).step(delta,l.sprites);
                            for(int j = 0; j < l.sprites.size(); j++) {
                                Sprite s2 = l.sprites.get(j);
                                if(s2.image.contains("wall") || s2.image.contains("tree")) {
                                    collide(s,s2);
                                } else if(s2.image.contains("bullet")) {
                                    Bullet b = (Bullet)s2;
                                    Line2D bulletPath = new Line2D.Double(b.x, b.y, b.x + b.vx * b.speed * delta, b.y + b.vy * b.speed * delta);
                                    Rectangle2D zombie = new Rectangle2D.Double(s.x, s.y, s.width, s.height);
                                    if(bulletPath.intersects(zombie)) {
                                        l.sprites.remove(s);
                                        numZombs--;
                                        l.sprites.remove(s2);
                                        i -= 2;
                                        j -= 2;
                                        continue;
                                    }
                                }
                            }
                        }
                        else if(s.image.contains("turret")) {
                            if(((Turret)s).canShoot())
                                l.sprites.add(new Bullet((int)s.getX()+s.getWidth()/2,(int)s.getY()+s.getHeight()/2,1,1,s.getAngle(),.5,10));
                            ((Turret)s).step(delta,l.sprites);
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

    public static boolean doesCollide(Sprite s1, Sprite s2) {
        Rectangle2D r1 = new Rectangle2D.Double(s1.x, s1.y, s1.width, s1.height);
        Rectangle2D r2 = new Rectangle2D.Double(s2.x, s2.y, s2.width, s2.height);
        return r1.intersects(r2) || r2.intersects(r1);
    }

    public static void collide(Sprite sprite, Sprite wood) {
        Rectangle2D.Double woodRect = new Rectangle2D.Double(wood.getX(), wood.getY(), wood.getWidth(), wood.getHeight());
        Line2D top = new Line2D.Double(sprite.getX(), sprite.getY(), sprite.getX() + sprite.getWidth(), sprite.getY());
        Line2D bottom = new Line2D.Double(sprite.getX(), sprite.getY() + sprite.getHeight(), sprite.getX() + sprite.getWidth(), sprite.getY() + sprite.getHeight());
        Line2D left = new Line2D.Double(sprite.getX(), sprite.getY(), sprite.getX(), sprite.getY() + sprite.getHeight());
        Line2D right = new Line2D.Double(sprite.getX() + sprite.getWidth(), sprite.getY(), sprite.getX() + sprite.getWidth(), sprite.getY() + sprite.getHeight());
        if (woodRect.intersectsLine(top)) {
            if (woodRect.intersectsLine(left)) {
                if (Math.abs(woodRect.getMaxX() - sprite.getX()) < Math.abs(woodRect.getMaxY() - sprite.getY())) {
                    sprite.setX((int) woodRect.getMaxX());
                } else {
                    sprite.setY((int) woodRect.getMaxY());
                }
            } else if (woodRect.intersectsLine(right)) {
                if (Math.abs(woodRect.getMinX() - sprite.getX() - sprite.getWidth()) < Math.abs(woodRect.getMaxY() - sprite.getY())) {
                    sprite.setX((int) woodRect.getMinX() - sprite.getWidth());
                } else {
                    sprite.setY((int) woodRect.getMaxY());
                }
            } else {
                sprite.setY((int) woodRect.getMaxY());
            }
        } else if (woodRect.intersectsLine(bottom)) {
            if (woodRect.intersectsLine(left)) {
                if (Math.abs(woodRect.getMaxX() - sprite.getX()) < Math.abs(woodRect.getMinY() - sprite.getY() - sprite.getHeight())) {
                    sprite.setX((int) woodRect.getMaxX());
                } else {
                    sprite.setY((int) woodRect.getMinY() - sprite.getHeight());
                }
            } else if (woodRect.intersectsLine(right)) {
                if (Math.abs(woodRect.getMinX() - sprite.getX() - sprite.getWidth()) < Math.abs(woodRect.getMinY() - sprite.getY() - sprite.getHeight())) {
                    sprite.setX((int) woodRect.getMinX() - sprite.getWidth());
                } else {
                    sprite.setY((int) woodRect.getMinY() - sprite.getHeight());
                }
            } else {
                sprite.setY((int) woodRect.getMinY() - sprite.getHeight());
            }
        } else if (woodRect.intersectsLine(left)) {
            sprite.setX((int) woodRect.getMaxX());
        } else if (woodRect.intersectsLine(right)) {
            sprite.setX((int) woodRect.getMinX() - sprite.getWidth());
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
