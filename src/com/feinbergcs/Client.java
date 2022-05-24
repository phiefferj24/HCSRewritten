package com.feinbergcs;

import java.awt.*;
import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        Client client = new Client();
        Socket socket = client.connect("localhost", 9000);
        ClientThread clientThread = new ClientThread(socket);
        clientThread.start();
        Display d = new Display(1280, 720, "Client", new Display.Callback() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(100, 100, 1080, 520);
            }

            @Override
            public void keyPressed(int key) {

            }

            @Override
            public void keyReleased(int key) {}

            @Override
            public void mouseMoved(int x, int y) {}
        });
        d.repaint();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            while ((line = in.readLine()) != null) {
                clientThread.send(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
