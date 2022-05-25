package com.feinbergcs;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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

    // COSTS
    public static boolean died=false;
    public static final double WALL_COST = 50;
    public static final double TURRET_COST = 100;
    public static double moneyStore = 0;
    public static int selectedIndex = 0;
    public static final int TOTAL_ITEMS = 2;
    public static final String[] ITEM_NAMES = {"wall", "turret"};

    // OTHER STUFF

    public static double deathCounter = 0;
    public static boolean playedDeathSound = false;

    public static final int MAP_WIDTH = 10000;
    public static final int MAP_HEIGHT = 10000;
    public static boolean[] downKeys = new boolean[256];
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final double MONEY_RATE = 0.01;
    public static double money = 0;
    public static final double AMMO_RATE = 0.002;
    public static double ammo = 50;
    public static int clickX = 0;
    public static int clickY = 0;
    public static int speed = 0;
    public static int mouseX = 0;
    public static int mouseY = 0;
    public static String playerID;
    public ArrayList<Sprite> sprites = new ArrayList<Sprite>();
    public ArrayList<Sprite> spritesToAdd = new ArrayList<Sprite>();
    public boolean onRight = false;
    public static boolean shop = false;
    public static LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();

    private static ArrayList<File> soundtrack = new ArrayList<File>();

    public static void main(String[] args) throws InterruptedException {

        soundtrack.add(new File(Client.class.getResource("/tft.wav").getPath()));
        soundtrack.add(new File(Client.class.getResource("/tetris.wav").getPath()));
        play(soundtrack.get((int)(Math.random()*soundtrack.size())), true);
        //System.out.println("THIS IS THE SOUNDTRAD: " + soundtrack.get((int)(Math.random()*soundtrack.size())));

        Client client = new Client();
        //76.181.240.154
        Socket socket = client.connect("10.13.20.51", 9000);
        ClientThread clientThread = new ClientThread(socket);
        clientThread.start();

        Display d = new Display(WINDOW_WIDTH, WINDOW_HEIGHT, "Client", new Display.Callback() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(11, 173, 14));
                g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
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
                    if(sprite.getId().equals(Client.playerID))
                    {
                        if(died)
                        {
                            if(!playedDeathSound) {
                                moneyStore = money;
                                play(new File(Client.class.getResource("/gameover.wav").getPath()), false);
                                playedDeathSound = true;
                            }
                            //TODO: FIX THIS THING
                            g.setColor(new Color(255, 255, 255));
                            g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
                            g.setFont(new Font("Arial", Font.BOLD, 100));
                            g.setColor(new Color(0, 0, 0));
                            String dead1 = "YOU HAVE DIED!";
                            String dead2 = "Final Score: " + String.format("%.2f", moneyStore == 0 ? money : moneyStore);
                            FontMetrics fm = g.getFontMetrics();
                            g.drawString(dead1, (WINDOW_WIDTH/2) - fm.stringWidth(dead1)/2, (WINDOW_HEIGHT/2) - 120);
                            g.drawString(dead2, (WINDOW_WIDTH/2) - fm.stringWidth(dead2)/2, (WINDOW_HEIGHT/2) - 20);
                            if(deathCounter > 5000) System.exit(0);
                            return;
                        }
                    }
                    double playerCX = client.sprites.get(0).getX()+client.sprites.get(0).getWidth()/2;
                    double playerCY = client.sprites.get(0).getY()+client.sprites.get(0).getHeight()/2;
                    double cx = client.sprites.get(i).getX()+client.sprites.get(0).getWidth()/2;
                    double cy = client.sprites.get(i).getY()+client.sprites.get(0).getHeight()/2;
                    if(Math.sqrt((playerCX-cx)*(playerCX-cx)+(playerCY-cy)*(playerCY-cy))<790)
                        drawImage(g, loadImage(sprite.getImage()), sprite.getX()+((WINDOW_WIDTH/2.0)-playerX), sprite.getY()+((WINDOW_HEIGHT/2.0)-playerY), sprite.getWidth(), sprite.getHeight(), sprite.getAngle());
                }
                if(shop)
                    drawShop(g);
                client.drawMinimap(g, MAP_WIDTH, MAP_HEIGHT);
            }

            @Override
            public void keyPressed(int key) {
                downKeys[key] = true;
                if(key == KeyEvent.VK_SPACE && ammo >= 1) {
                    ammo -= 1;
                    client.onRight = !client.onRight;
                    client.spritesToAdd.add(new Bullet(client.getPlayer(), 1, 1, 1, client.onRight, 8));
                }
                if(key == KeyEvent.VK_C) {
                    shop = true;
                }
                switch(key) {
                    case KeyEvent.VK_1 -> selectedIndex = 0;
                    case KeyEvent.VK_2 -> selectedIndex = 1;
                    case KeyEvent.VK_3 -> selectedIndex = 2;
                    case KeyEvent.VK_4 -> selectedIndex = 3;
                    case KeyEvent.VK_5 -> selectedIndex = 4;
                    case KeyEvent.VK_6 -> selectedIndex = 5;
                    case KeyEvent.VK_7 -> selectedIndex = 6;
                    case KeyEvent.VK_8 -> selectedIndex = 7;
                    case KeyEvent.VK_9 -> selectedIndex = 8;
                    case KeyEvent.VK_0 -> selectedIndex = 9;
                }
                if(selectedIndex >= TOTAL_ITEMS) selectedIndex = TOTAL_ITEMS - 1;
            }

            @Override
            public void keyReleased(int key) {
                downKeys[key] = false;
                if(key == KeyEvent.VK_C) {
                    shop = false;
                }
            }

            @Override
            public void mouseClicked(int x, int y, int button) {
                //play(new File(Client.class.getResource("/hit.wav").getPath()),false);
                clickX = x;
                clickY = y;

                if(shop)
                {
                    if(Math.sqrt((clickX-130)*(clickX-130)+(clickY-160)*(clickY-160))<100)
                    {
                        if(money>500) {
                            money -= 500;
                            ((Player) client.sprites.get(0)).setSpeed(((Player) client.sprites.get(0)).speed + .3);
                        }
                    }
                    else if(Math.sqrt((clickX-380)*(clickX-380)+(clickY-160)*(clickY-160))<100)
                    {
                        if(money>50) {
                            money -= 50;
                            ((Player)client.sprites.get(0)).setWidth((int)(((Player)client.sprites.get(0)).getWidth()*(0.75)));
                            ((Player)client.sprites.get(0)).setHeight((int)(((Player)client.sprites.get(0)).getHeight()*(0.75)));
                        }
                    }
                    else if(Math.sqrt((clickX-600)*(clickX-600)+(clickY-160)*(clickY-160))<100)
                    {
                        if(money>700)
                            ((Player) client.sprites.get(0)).setHealth(((Player) client.sprites.get(0)).health + 50);

                    }
                    return;
                }

                double relX = x - WINDOW_WIDTH/2 + client.getPlayer().x + client.getPlayer().width/2;
                double relY = y - WINDOW_HEIGHT/2 + client.getPlayer().y + client.getPlayer().height/2;
                Player p = client.getPlayer();
                if(p == null) return;
                if(button == MouseEvent.BUTTON1) {
                    if(selectedIndex == 0 && money >= WALL_COST) {
                        Rectangle2D r1 = new Rectangle2D.Double(((int) relX - 32) / 64 * 64 + 32, ((int) relY - 32) / 64 * 64 + 32, 64, 64);
                        for(int i = 0; i < client.sprites.size(); i++) {
                            Sprite sprite = client.sprites.get(i);
                            Rectangle2D r2 = new Rectangle2D.Double(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
                            if(r1.intersects(r2)) {
                                return;
                            }
                        }
                        client.spritesToAdd.add(new Wall(((int) relX - 32) / 64 * 64 + 32, ((int) relY - 32) / 64 * 64 + 32, 64, 64, "/wall.png", 0));
                        money -= WALL_COST;
                    } else if(selectedIndex == 1 && money >= TURRET_COST) {
                        Rectangle2D r1 = new Rectangle2D.Double(relX - 50, (int)relY - 50, 100, 100);
                        for(int i = 0; i < client.sprites.size(); i++) {
                            Sprite sprite = client.sprites.get(i);
                            Rectangle2D r2 = new Rectangle2D.Double(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
                            if(r1.intersects(r2)) {
                                return;
                            }
                        }
                        client.spritesToAdd.add(new Turret((int)relX - 50, (int)relY - 50, 100, 100, "/turret.png"));
                        money -= TURRET_COST;
                    }
                } if (button == MouseEvent.BUTTON3) {
                    for(int i = 0; i < client.sprites.size(); i++) {
                        Sprite sprite = client.sprites.get(i);
                        if(sprite.getImage().contains("wall")) {
                            Rectangle2D wall = new Rectangle2D.Double(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
                            if(wall.contains(relX, relY)) {
                                Wall newWall = new Wall(sprite.toString());
                                newWall.setImage("");
                                client.spritesToAdd.add(newWall);
                                money += WALL_COST / 4;
                                break;
                            }
                        } else if(sprite.getImage().contains("turret")) {
                            Rectangle2D turret = new Rectangle2D.Double(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
                            if(turret.contains(relX, relY)) {
                                Turret newTurret = new Turret(sprite.toString());
                                newTurret.setImage("");
                                client.spritesToAdd.add(newTurret);
                                money += TURRET_COST / 4;
                                break;
                            }
                        }
                    }
                }
            }
        });
        Thread t = new Thread(d);
        t.start();
        client.sprites.add(new Player(25, 25, 100, 100, "/player.png"));
        //client.sprites.add(new Tree(500, 500, 100, 100, "/tree.png"));
        playerID = client.sprites.get(0).getId();
        double lastTime = System.currentTimeMillis();
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
            double delta = System.currentTimeMillis() - lastTime;
            if(died) deathCounter += delta;
            money += delta * MONEY_RATE;
            ammo += delta * AMMO_RATE;
            lastTime = System.currentTimeMillis();
            double time = System.currentTimeMillis();
            String[] messagesa;
            messagesa = message.split(",");
            for (int j = 0; j < client.sprites.size(); j++) {
                Sprite sprite = client.sprites.get(j);
                for (int i = 0; i < messagesa.length - 1; i++) {
                    if(messagesa[i].contains(Client.playerID)) {
                        messagesa[i] = "";
                    }
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
                    } else if (messagesa[i].contains("wall")) {
                        client.sprites.add(new Wall(messagesa[i]));
                    } else if (messagesa[i].contains("turret")) {
                        client.sprites.add(new Turret(messagesa[i]));
                    } else if (messagesa[i].contains("stone")) {
                        client.sprites.add(new Stone(messagesa[i]));
                    } else if (messagesa[i].contains("bush")) {
                        client.sprites.add(new Bush(messagesa[i]));
                    }
                }
            }
            for(int i = 0; i < client.sprites.size(); i++) {
                Sprite sprite = client.sprites.get(i);
                if(!sprite.getId().equals(Client.playerID)) {
                    if(!message.contains(sprite.getId())) {
                        client.sprites.remove(sprite);
                        i--;
                    }
                }
                if(sprite.getImage().equals("")) {
                    System.out.println("removing");
                    client.sprites.remove(sprite);
                    i--;
                }
            }
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
                for(int i = 0; i < client.sprites.size(); i++) {
                    Sprite sprite = client.sprites.get(i);
                    if(sprite.getId().equals(playergot.getId()) || sprite.getImage().contains("bullet") || sprite.getImage().contains("zombie")) continue;
                    Rectangle2D bounds = new Rectangle2D.Double(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
                    if(bounds.contains(playergot.getX() + xdel * delta, playergot.getY())) {
                        xdel = 0;
                    }
                    if(bounds.contains(playergot.getX(), playergot.getY() + ydel * delta)) {
                        ydel = 0;
                    }
                    if(bounds.contains(playergot.getX() + xdel * delta, playergot.getY() + ydel * delta)) {
                        xdel = 0;
                        ydel = 0;
                    }
                }
                playergot.setVX(xdel);
                playergot.setVY(ydel);
                playergot.setX((int)(playergot.getX() + playergot.getVX() * delta));
                playergot.setY((int)(playergot.getY() + playergot.getVY() * delta));
                playergot.setAngle(Math.atan2(mouseY - (double) WINDOW_HEIGHT / 2, mouseX - (double) WINDOW_WIDTH / 2));
                playergot.step(delta);
                for(int i = 0; i < client.sprites.size(); i++) {
                    Sprite sprite = client.sprites.get(i);
                    if(sprite.getId().equals(playergot.getId()) || sprite.getImage().contains("bullet")) continue;
                    client.collide(playergot, sprite);
                    //first one is the one you want to move
                    //second one is the one you want ot move out of
                }
                if(playergot.getX() < 0) {
                    playergot.setX(0);
                }
                if(playergot.getX() > MAP_WIDTH - playergot.getWidth()) {
                    playergot.setX(MAP_WIDTH - playergot.getWidth());
                }
                if(playergot.getY() < 0) {
                    playergot.setY(0);
                }
                if(playergot.getY() > MAP_HEIGHT - playergot.getHeight()) {
                    playergot.setY(MAP_HEIGHT - playergot.getHeight());
                }
                messageBuilder.append(playergot.toString()).append(",");
            }
            for(int i = 0; i < client.spritesToAdd.size(); i++) {
                messageBuilder.append(client.spritesToAdd.get(i).toString()).append(",");
            }
            client.spritesToAdd.clear();
            messageBuilder.append(time);
            clientThread.send(messageBuilder.toString());
            while(d.queue.size() > 1);
            d.queue.add(0);
        }
    }

    public static void play(File file, Boolean repeat)
    {
        try
        {
            final Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));

            clip.addLineListener((event) -> {
                if (event.getType() == LineEvent.Type.STOP)
                {
                    clip.close();
                    if(repeat)
                        play(soundtrack.get((int)(Math.random()*soundtrack.size())), true);
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


    public static void drawShop(Graphics g)
    {
        g.setColor(Color.GRAY);
        g.fillRect(20,20,820-20, WINDOW_HEIGHT  - 30);
        g.setColor(Color.BLUE);
        g.fillRect(30,30,800-20, WINDOW_HEIGHT  - 50);

        g.setColor(Color.GRAY);
        g.fillRect(60,60,200, 200);
        g.setColor(Color.BLACK);
        g.drawString("BUY SPEED:  500$" ,65,  75);
        drawImage(g,loadImage("/fast.png"),60.0,90.0,200,200,-Math.PI/2);

        g.setColor(Color.GRAY);
        g.fillRect(280,60,200, 200);
        g.setColor(Color.BLACK);
        g.drawString("DECREASE SIZE BY 25%:  500$" ,285,  75);
        drawImage(g,loadImage("/wakeUp.png"),280,90.0,200,200,-Math.PI/2);

        g.setColor(Color.GRAY);
        g.fillRect(500,60,200, 200);
        g.setColor(Color.BLACK);
        g.drawString("GAIN HEALTH:  700$" ,505,  75);
        drawImage(g,loadImage("/docotr.png"),500,90.0,200,200,-Math.PI/2);

        g.drawString("Welcome to the shop" ,285-20,  350);
        drawImage(g,loadImage("/joe.png"),285-20,  75+300,220,300,-Math.PI/2);


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
            if(scaledX < 0 || scaledY < 0 || scaledX > minimapSize || scaledY > minimapSize) continue;
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
        String pos = "Position: " + (int)player.x + ", " + (int)player.y;
        g.drawString(pos, WINDOW_WIDTH - metrics.stringWidth(pos) - 10, minimapSize + FONT_SIZE + 20);
        String money = "Money: $" + String.format("%.2f", Client.money);
        g.drawString(money, WINDOW_WIDTH - metrics.stringWidth(money) - 10, minimapSize + FONT_SIZE + 45);
        String health = "Health: " + String.format("%.2f", player.getHealth()) + " HP";
        if(player.getHealth()<=0)
        {
            died=true;
        }
        g.drawString(health, WINDOW_WIDTH - metrics.stringWidth(health) - 10, minimapSize + FONT_SIZE + 70);
        String ammo = "Ammo: " + (int)Client.ammo;
        g.drawString(ammo, WINDOW_WIDTH - metrics.stringWidth(ammo) - 10, minimapSize + FONT_SIZE + 95);

        // hotbar
        g.setColor(Color.WHITE);
        g.fillRect(10, 10, 70 * TOTAL_ITEMS + 10, 80);
        g.setColor(Color.BLACK);
        g.drawRect(10, 10, 70 * TOTAL_ITEMS + 10, 80);
        g.setColor(Color.ORANGE);
        g.fillRect(15 + 70 * selectedIndex, 15, 70, 70);
        for(int i = 0; i < TOTAL_ITEMS; i++){
            String image = "/" + ITEM_NAMES[i] + ".png";
            Image scaled = loadImage(image).getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            g.drawImage(scaled, 20 +70 * i, 20, null);
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

    public void collide(Sprite sprite, Sprite wood) {
        double playerCurrX = ((Player) sprite).getX();
        double playerCurrY = ((Player) sprite).getY();
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
        if ((playerCurrX != ((Player) sprite).getX() || playerCurrY != ((Player) sprite).getY()) && wood.getImage().contains("zombie")) {
            ((Player)sprite).setHealth(((Player)sprite).getHealth()-wood.getWidth()/100.0);
        }
        if ((playerCurrX != ((Player) sprite).getX() || playerCurrY != ((Player) sprite).getY()) && wood.getImage().contains("bullet")) {
            ((Player)sprite).setHealth(((Player)sprite).getHealth()-5);
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
    public Player getPlayer() {
        if(playerID == null) return null;
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
/*

 */
