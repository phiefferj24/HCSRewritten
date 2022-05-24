package com.feinbergcs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    public static ArrayList<Integer> downKeys = new ArrayList<>();
    public static int clickX = 0;
    public static int clickY = 0;
    public static int mouseX = 0;
    public static int mouseY = 0;
    public static UUID playerID;
    public ArrayList<Sprite> sprites = new ArrayList<Sprite>();
    public boolean onRight = false;
    public static LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
    public static void main(String[] args) {
        Client client = new Client();
        Socket socket = client.connect("localhost", 9000);
        ClientThread clientThread = new ClientThread(socket);
        clientThread.start();
        Display d = new Display(1280, 720, "Client", new Display.Callback() {
            @Override
            public void paintComponent(Graphics g) {
                double playerX=0;
                double playerY=0;
                for (int i = 0; i < client.sprites.size(); i++) {
                    Sprite sprite = client.sprites.get(i);
                    //System.out.println("outside the for: " + sprites.get(i).getImage());
                    if(sprite.getImage().equals("/player.png"))
                    {
                        //System.out.println("inside the for" + sprites.get(i).getImage());
                        playerX=((Player)client.sprites.get(i)).getX()+(((Player)client.sprites.get(i)).getWidth()/2);
                       playerY=((Player)client.sprites.get(i)).getY()+(((Player)client.sprites.get(i)).getHeight()/2);
                        //TODO: after do the scaling
                       drawImage(g, loadImage(sprite.getImage()), (1280/2)-(((Player)client.sprites.get(i)).getWidth()/2), (720/2)-(((Player)client.sprites.get(i)).getHeight()/2), 2*sprite.getWidth(), 2*sprite.getHeight(), sprite.getAngle());
                        //drawImage(g, loadImage(sprite.getImage()), sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight(), sprite.getAngle());
                    }
                    }
                for(int i=0; i<client.sprites.size();i++)
                {
                    Sprite sprite=client.sprites.get(i);
                    if(!client.sprites.get(i).getImage().equals("/player.png")) {

                        drawImage(g, loadImage(sprite.getImage()), sprite.getX()+((1280/2)-playerX), sprite.getY()+((720/2)-playerY), 2*sprite.getWidth(), 2*sprite.getHeight(), sprite.getAngle());
                        //drawImage(g, loadImage(sprite.getImage()), sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight(), sprite.getAngle());
                    }
                }
            }

            @Override
            public void keyPressed(int key) {
                if(!downKeys.contains((Integer) key)) downKeys.add(key);
            }

            @Override
            public void keyReleased(int key) {
                downKeys.remove((Integer) key);
            }

            @Override
            public void mouseClicked(int x, int y) {
                clickX = x;
                clickY = y;
                client.onRight = !client.onRight;
                client.sprites.add(new Bullet(client.getPlayer(), 1, 1, 1, client.onRight));
            }
        });
        Thread t = new Thread(d);
        t.start();
        client.sprites.add(new Player(0, 0, 50, 50, "/player.png"));
        client.sprites.add(new Tree(500, 500, 100, 100, "/wood.png"));
        playerID = client.sprites.get(0).getId();

        double time = System.currentTimeMillis();
        while(true) {
            double delta = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            mouseX = (int) d.getMouseX();
            mouseY = (int) d.getMouseY();
            StringBuilder message = new StringBuilder();
            for (int i = 0; i < client.sprites.size(); i++) {
                Sprite sprite = client.sprites.get(i);
                if(!(sprite instanceof Player) || sprite.getId().equals(playerID)) {
                    if(sprite.getId().equals(playerID)) sprite.setAngle(Math.atan2(mouseY - sprite.getY() - (double)sprite.getHeight() / 2, mouseX - sprite.getX() - (double)sprite.getWidth() / 2));
                    sprite.step(delta);
                    for(int j = 0; j < client.sprites.size(); j++) {
                        if(i == j) continue;
                        Sprite wood = client.sprites.get(j);
                        if(wood.getImage()=="/wood.png" || wood.getImage()=="/Amogus.png") {
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
                                    sprite.setY((int) woodRect.getMinY() - sprite.getHeight());
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
                                    sprite.setY((int) woodRect.getMaxY());
                                }
                            } else if (woodRect.intersectsLine(left)) {
                                sprite.setX((int) woodRect.getMaxX());
                            } else if (woodRect.intersectsLine(right)) {
                                sprite.setX((int) woodRect.getMinX() - sprite.getWidth());
                            }
                        }
                    }

                    message.append(sprite.toString()).append(",");
                }
            }
            clientThread.send(message.toString());
            while(messages.isEmpty());
            while(!messages.isEmpty()) {
                String[] messagesa = new String[0];
                try {
                    messagesa = messages.take().split(",");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < messagesa.length && !messagesa[i].isEmpty(); i++) {
                    String s = messagesa[i];
                    if (!s.isEmpty()) {
                        String id = s.substring(1).split(";")[0];
                        boolean found = false;
                        for (int j = 0; j < client.sprites.size(); j++) {
                            Sprite sprite = client.sprites.get(j);
                            if (sprite.getId().toString().equals(id)) {
                                found = true;
                                sprite.updateToString(s);
                            }
                        }
                        if (!found) {

                            if(messagesa[i].contains("player.png"))
                                client.sprites.add(new Player(messagesa[i]));
                            if(messagesa[i].contains("wood.png"))
                                client.sprites.add(new Tree(messagesa[i]));
                            if(messagesa[i].contains("zombie.png"))
                                client.sprites.add(new Zombie(messagesa[i]));
                            System.out.println("added " + messagesa[i]);

                            client.sprites.get(client.sprites.size() - 1).updateToString(s);
                        }
                    }
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void drawImage(Graphics g, BufferedImage loadImage, double x, double y, int width, int height, double angle) {
        Image scaled = loadImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        Image rotated = rotateImageByDegrees(scaled, angle * 180 / Math.PI + 90);
        g.drawImage(rotated, (int)x, (int)y, null);
    }
    private static BufferedImage rotateImageByDegrees(Image img, double angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int nw = (int) Math.floor(w * cos + h * sin);
        int nh = (int) Math.floor(h * cos + w * sin);
        BufferedImage rotated = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        int x = w / 2;
        int y = h / 2;
        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    public static BufferedImage loadImage(String path) {
        BufferedImage image = null;
        try {
            URL e = Client.class.getResource(path);
            image = ImageIO.read(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public Socket connect(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }
    public Player getPlayer() {
        for(int i = 0; i < sprites.size(); i++) {
            if (sprites.get(i).getId().toString().equals(playerID.toString())) {
                return (Player) sprites.get(i);
            }
        }
        return null;
    }
    public static class ClientThread extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private boolean running = true;
        public ClientThread(Socket socket) {
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
                while (running) {
                    String input = in.readLine();
                    if (input != null) {
                        messages.put(input);
                    }
                }
            } catch (IOException | InterruptedException e) {
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
    public static double distanceToNearestCorner(Point2D p, Rectangle2D r) {
        double d1 = p.distance(r.getMinX(), r.getMinY());
        double d2 = p.distance(r.getMinX(), r.getMaxY());
        double d3 = p.distance(r.getMaxX(), r.getMinY());
        double d4 = p.distance(r.getMaxX(), r.getMaxY());
        return Math.min(d1, Math.min(d2, Math.min(d3, d4)));
//        if(min == d1) return new Point2D.Double(r.getMinX(), r.getMinY());
//        if(min == d2) return new Point2D.Double(r.getMinX(), r.getMaxY());
//        if(min == d3) return new Point2D.Double(r.getMaxX(), r.getMinY());
//        if(min == d4)  return new Point2D.Double(r.getMaxX(), r.getMaxY());
//        return null;
    }
}
