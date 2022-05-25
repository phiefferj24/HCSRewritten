package com.feinbergcs;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.KeyEvent;
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
    public static boolean[] downKeys = new boolean[256];
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static int clickX = 0;
    public static int clickY = 0;
    public static int mouseX = 0;
    public static int mouseY = 0;
    public static String playerID;
    public ArrayList<Sprite> sprites = new ArrayList<Sprite>();
    public boolean onRight = false;
    public static LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();

    private static ArrayList<File> soundtrack = new ArrayList<File>();

    public static void main(String[] args) throws InterruptedException {

        soundtrack.add(new File(Client.class.getResource("/tft.wav").getPath()));
        //soundtrack.add(new File(Client.class.getResource("/tetris.wav").getPath()));
        play(soundtrack.get((int)(Math.random()*soundtrack.size())), true);
        //System.out.println("THIS IS THE SOUNDTRAD: " + soundtrack.get((int)(Math.random()*soundtrack.size())));
        Client client = new Client();
        Socket socket = client.connect("localhost", 9001);
        ClientThread clientThread = new ClientThread(socket);
        clientThread.start();
        Display d = new Display(WINDOW_WIDTH, WINDOW_HEIGHT, "Client", new Display.Callback() {
            @Override
            public void paintComponent(Graphics g) {
                double playerX=0;
                double playerY=0;
                Player p = client.getPlayer();
                if(p != null) {
                    playerX = p.x + p.width/2;
                    playerY = p.y + p.height/2;
                }
                for (int i = 0; i < client.sprites.size() && p != null; i++) {
                    Sprite sprite = client.sprites.get(i);
                    //System.out.println("outside the for: " + sprites.get(i).getImage());
                    drawImage(g, loadImage(sprite.getImage()), sprite.getX()+((WINDOW_WIDTH/2)-playerX), sprite.getY()+((WINDOW_HEIGHT/2)-playerY), sprite.getWidth(), sprite.getHeight(), sprite.getAngle());
                }
                client.drawMinimap(g, 10000, 10000);
            }

            @Override
            public void keyPressed(int key) {
                downKeys[key] = true;
            }

            @Override
            public void keyReleased(int key) {
                downKeys[key] = false;
            }

            @Override
            public void mouseClicked(int x, int y) {
                //play(new File(Client.class.getResource("/hit.wav").getPath()),false);
                clickX = x;
                clickY = y;
                client.onRight = !client.onRight;
                client.sprites.add(new Bullet(client.getPlayer(), 1, 1, 1, client.onRight, 8));
            }
        });
        Thread t = new Thread(d);
        t.start();
        client.sprites.add(new Player(0, 0, 100, 100, "/player.png"));
        client.sprites.add(new Tree(500, 500, 100, 100, "/tree.png"));
        playerID = client.sprites.get(0).getId();

        double time = System.currentTimeMillis();
        StringBuilder fm = new StringBuilder();
        for(int i = 0; i < client.sprites.size(); i++) {
            Sprite sprite = client.sprites.get(i);
            fm.append(sprite.toString()).append(",");
        }
        fm.append(System.currentTimeMillis());
        clientThread.send(fm.toString());
        while(true) {

            //while(messages.isEmpty());
            String message = messages.take();
            String[] messagesa;
            messagesa = message.split(",");
            for (int j = 0; j < client.sprites.size(); j++) {
                Sprite sprite = client.sprites.get(j);
                for (int i = 0; i < messagesa.length - 1; i++) {
                    if (messagesa[i].contains(sprite.getId())) {
                        sprite.updateToString(messagesa[i]);
                        messagesa[i] = "";
                    }
                }
            }
            for (int i = 0; i < messagesa.length - 1; i++) {
                if (!messagesa[i].equals("")) {
                    if (messagesa[i].contains("player")) {
                        client.sprites.add(new Player(messagesa[i]));
                    } else if (messagesa[i].contains("bullet")) {
                        client.sprites.add(new Bullet(messagesa[i]));
                    } else if (messagesa[i].contains("tree")) {
                        client.sprites.add(new Tree(messagesa[i]));
                    } else if (messagesa[i].contains("zombie")) {
                        client.sprites.add(new Zombie(messagesa[i]));
                    }
                }
            }
            time = messagesa[messagesa.length - 1].equals("") ? time : Double.parseDouble(messagesa[messagesa.length - 1]);
            double delta = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            mouseX = (int) d.getMouseX();
            mouseY = (int) d.getMouseY();
            StringBuilder messageBuilder = new StringBuilder();
            Player playergot = client.getPlayer();
            if(playergot != null) {
                double xdel = 0;
                double ydel = 0;
                if(Client.downKeys[KeyEvent.VK_W]) {
                    ydel -= playergot.speed;
                } if(Client.downKeys[KeyEvent.VK_S]) {
                    ydel += playergot.speed;
                } if(Client.downKeys[KeyEvent.VK_A]) {
                    xdel -= playergot.speed;
                } if(Client.downKeys[KeyEvent.VK_D]) {
                    xdel += playergot.speed;
                }
                playergot.setVX(xdel);
                playergot.setVY(ydel);
                playergot.setX((int)(playergot.getX() + playergot.getVX() * delta));
                playergot.setY((int)(playergot.getY() + playergot.getVY() * delta));
            }
            for (int i = 0; i < client.sprites.size(); i++) {
                Sprite sprite = client.sprites.get(i);
                if (!(sprite instanceof Player) || sprite.getId().equals(playerID)) {
                    if (sprite.getId().equals(playerID)) {
                        sprite.setAngle(Math.atan2(mouseY - (double) WINDOW_HEIGHT / 2, mouseX - (double) WINDOW_WIDTH / 2));
                    }
                    sprite.step(delta);
                    for (int j = 0; j < client.sprites.size(); j++) {
                        if (i == j) continue;
                        Sprite wood = client.sprites.get(j);
                        if (wood.getImage() == "/tree.png" || wood.getImage() == "/Amogus.png") {
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
                    messageBuilder.append(sprite.toString()).append(",");
                }
            }

            messageBuilder.append(time);
            clientThread.send(messageBuilder.toString());
            d.repaint();
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void play(File file, Boolean repeat)
    {
        try
        {
            final Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));

            clip.addLineListener(new LineListener()
            {
                @Override
                public void update(LineEvent event)
                {
                    if (event.getType() == LineEvent.Type.STOP)
                    {
                        clip.close();
                        if(repeat)
                            play(soundtrack.get((int)(Math.random()*soundtrack.size())), true);
                    }
                }
            });

            clip.open(AudioSystem.getAudioInputStream(file));
            clip.start();

        }
        catch (Exception exc)
        {
            exc.printStackTrace(System.out);
        }
    }


    public void drawMinimap(Graphics g, int mapWidth, int mapHeight){
        int minimapSize = WINDOW_WIDTH / 5;
        g.setColor(Color.WHITE);
        g.fillRect(WINDOW_WIDTH - minimapSize - 10, 10, minimapSize, minimapSize);
        g.setColor(Color.BLACK);
        g.drawRect(WINDOW_WIDTH - minimapSize - 10, 10, minimapSize, minimapSize);
        final double DOT_SIZE = 4;
        Player player = getPlayer();
        if(player == null) return;
        for(int i = 0; i < sprites.size(); i++){
            Sprite sprite = sprites.get(i);
            double scaledX = sprite.getX() * minimapSize / mapWidth;
            double scaledY = sprite.getY() * minimapSize / mapHeight;
            if(sprite.image.contains("player")){
                if(sprite.id.toString().equals(player.id.toString())) {
                    g.setColor(Color.BLUE);
                } else {
                    g.setColor(Color.GREEN);
                }
                g.fillOval((int)(WINDOW_WIDTH - minimapSize - 10 + scaledX - DOT_SIZE/2), (int)(10 + scaledY - DOT_SIZE/2), (int)(DOT_SIZE), (int)(DOT_SIZE));
            } else if(sprite.image.contains("zombie")){
                g.setColor(Color.RED);
                g.fillOval((int)(WINDOW_WIDTH - minimapSize - 10 + scaledX - DOT_SIZE/2), (int)(10 + scaledY - DOT_SIZE/2), (int)(DOT_SIZE), (int)(DOT_SIZE));
            }
        }
        final int FONT_SIZE = 20;
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        FontMetrics metrics = g.getFontMetrics();
        String pos = "(" + (int)player.x + ", " + (int)player.y + ")";
        g.drawString(pos, WINDOW_WIDTH - metrics.stringWidth(pos) - 10, minimapSize + FONT_SIZE + 20);
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
                        messages.add(input);
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
    public static double distanceToNearestCorner(Point2D p, Rectangle2D r) {
        double d1 = p.distance(r.getMinX(), r.getMinY());
        double d2 = p.distance(r.getMinX(), r.getMaxY());
        double d3 = p.distance(r.getMaxX(), r.getMinY());
        double d4 = p.distance(r.getMaxX(), r.getMaxY());
        return Math.min(d1, Math.min(d2, Math.min(d3, d4)));
    }
}
