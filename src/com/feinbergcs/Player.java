package com.feinbergcs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Player extends Sprite {
    public double speed = 1;
    public double vx;
    public double vy;
    public double health;
    public Player(int x, int y, int width, int height, String image, double vx, double vy, double health) {
        id = UUID.randomUUID().toString();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        this.vx=vx;
        this.vy=vy;
        this.health=health;
    }
    public Player(int x, int y, int width, int height, String image) {
        this(x, y, width, height, image, 0, 0, 100);
    }
    public Player(String info) {
        updateToString(info);
    }
    public void step(double dt) {

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

    public double getHealth()
    {
        return health;
    }

    public void setHealth(double health)
    {
        this.health=health;
    }

    @Override
    public String toString() {
        return "[" + id + ";" + x + ";" + y + ";" + width + ";" + height + ";" + image + ";" + angle + ";" + vx + ";" + vy + ";" + health + "]";
    }

    @Override
    public void updateToString(String info) {
        String[] split = info.substring(1, info.length() - 2).split(";");
        id = split[0];
        x = Double.parseDouble(split[1]);
        y = Double.parseDouble(split[2]);
        width = Integer.parseInt(split[3]);
        height = Integer.parseInt(split[4]);
        image = split[5];
        angle = Double.parseDouble(split[6]);
        vx=Double.parseDouble(split[7]);
        vy=Double.parseDouble(split[8]);
    }
}
