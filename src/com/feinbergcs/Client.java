package com.feinbergcs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Client {
    public static ArrayList<Integer> downKeys = new ArrayList<>();
    public static void main(String[] args) {
        ArrayList<Sprite> sprites = new ArrayList<Sprite>();
        Client client = new Client();
        Socket socket = client.connect("localhost", 9000);
        ClientThread clientThread = new ClientThread(socket);
        clientThread.start();
        Display d = new Display(1280, 720, "Client", new Display.Callback() {
            @Override
            public void paintComponent(Graphics g) {
                for (Sprite sprite : sprites) {
                    drawScaledImage(g, loadImage(sprite.getImage()), sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
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
            public void mouseMoved(int x, int y) {}
        });
        Thread t = new Thread(d);
        t.start();
        sprites.add(new Player(0, 0, 50, 50, "/player.png"));
        double time = System.currentTimeMillis();
        while(true) {
            double delta = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            for (Sprite sprite : sprites) {
                sprite.step(delta);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void drawScaledImage(Graphics g, BufferedImage loadImage, int x, int y, int width, int height) {
        Image scaled = loadImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        g.drawImage(scaled, x, y, null);
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
                        System.out.println("Recieved: " + input);
                    }
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
