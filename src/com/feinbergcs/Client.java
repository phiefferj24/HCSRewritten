package com.feinbergcs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
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
    public static LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
    public static void main(String[] args) {
        ArrayList<Sprite> sprites = new ArrayList<Sprite>();
        Client client = new Client();
        Socket socket = client.connect("localhost", 9000);
        ClientThread clientThread = new ClientThread(socket);
        clientThread.start();
        Display d = new Display(1280, 720, "Client", new Display.Callback() {
            @Override
            public void paintComponent(Graphics g) {
                int count = 0;
                for (int i = 0; i < sprites.size(); i++) {
                    Sprite sprite = sprites.get(i);
                    count++;
                    drawPlayerImage(g, loadImage(sprite.getImage()), sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight(), sprite.getAngle());
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
            }
        });
        Thread t = new Thread(d);
        t.start();
        sprites.add(new Player(0, 0, 150, 150, "/player.png"));
        playerID = sprites.get(0).getId();
       sprites.add(new Tree(50,50,100,100,"/tree.png"));
        double time = System.currentTimeMillis();
        while(true) {
            double delta = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            mouseX = (int) d.getMouseX();
            mouseY = (int) d.getMouseY();
            StringBuilder message = new StringBuilder();
            for (Sprite sprite : sprites) {
                if(sprite.getId().equals(playerID)) {
                    sprite.setAngle(Math.atan2(mouseY - sprite.getY() - (double)sprite.getHeight() / 2, mouseX - sprite.getX() - (double)sprite.getWidth() / 2));
                    sprite.step(delta);
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
                        for (Sprite sprite : sprites) {
                            if (sprite.getId().toString().equals(id)) {
                                found = true;
                                sprite.updateToString(s);
                            }
                        }
                        if (!found) {
                            System.out.println("New player: " + s);
                            sprites.add(new Player(0, 0, 150, 150, "/player.png"));
                            sprites.get(sprites.size() - 1).updateToString(s);
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

    private static void drawPlayerImage(Graphics g, BufferedImage loadImage, int x, int y, int width, int height, double angle) {
        Image scaled = loadImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        Image rotated = rotateImageByDegrees(scaled, angle * 180 / Math.PI + 90);
        g.drawImage(rotated, x, y, null);
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
}
