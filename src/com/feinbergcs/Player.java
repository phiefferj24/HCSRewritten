package com.feinbergcs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Player extends Sprite {
    private double speed = 1;
    private double vx;
    private double vy;
    public Player(int x, int y, int width, int height, String image, double vx, double vy) {
        id = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        this.vx=vx;
        this.vy=vy;
    }
    public Player(int x, int y, int width, int height, String image) {
        this(x, y, width, height, image, 0, 0);
    }
    public Player(String info) {
        updateToString(info);
    }
    public void step(double dt) {
        double lvx = 0, lvy = 0;
        for (int key : Client.downKeys) {
            if (key == KeyEvent.VK_W) {
                lvy = -speed;
            }
            if (key == KeyEvent.VK_S) {
                lvy = speed;
            }
            if (key == KeyEvent.VK_A) {
                lvx = -speed;
            }
            if (key == KeyEvent.VK_D) {
                lvx = speed;
            }
        }
        x += (vx + lvx) * dt;
        y += (vy + lvy) * dt;
    }
    public void setVX(double vx)
    {
        this.vx=vx;
    }
    public void setVY(double vy)
    {
        this.vy=vy;
    }

    public double getVX()
    {
        return vx;
    }
    public double getVY()
    {
        return vy;
    }
    @Override
    public String toString() {
        return "[" + id + ";" + x + ";" + y + ";" + width + ";" + height + ";" + image + ";" + angle + ";" + vx + ";" + vy + "]";
    }

    @Override
    public void updateToString(String info) {
        if(info.length() < 2) return;
        String[] split = info.substring(2, info.length() - 2).split(";");
        if(id == null) id = UUID.fromString(split[0]);
        x = Integer.parseInt(split[1]);
        y = Integer.parseInt(split[2]);
        width = Integer.parseInt(split[3]);
        height = Integer.parseInt(split[4]);
        image = split[5];
        angle = Double.parseDouble(split[6]);
        vx=Double.parseDouble(split[7]);
        vy=Double.parseDouble(split[8]);
    }

    public Sprite StringToSpite(String info) {
        if(info.length() < 2) return null;
        String[] split = info.substring(2, info.length() - 2).split(";");
        Player player = new Player(Integer.parseInt(split[1]),Integer.parseInt(split[2]),Integer.parseInt(split[3]),Integer.parseInt(split[4]),split[5]);
        player.setUUID(UUID.fromString(split[0]));
        player.setAngle(Double.parseDouble(split[6]));
        return player;
    }


}
