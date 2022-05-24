package com.feinbergcs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Client {
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
                    g.drawImage(loadImage(sprite.getImage()), sprite.getX(), sprite.getY(), null);
                }
            }

            @Override
            public void keyPressed(int key) {

            }

            @Override
            public void keyReleased(int key) {}

            @Override
            public void mouseMoved(int x, int y) {}
        });
        sprites.add(new Player(0, 0, 50, 50, "/circle.png"));
        while(true) {
            d.repaint();
            for (Sprite sprite : sprites) {
                sprite.step();
            }
        }
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
