package com.feinbergcs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.UUID;

public class Pig extends Sprite {
    private double angle = Math.random()*360;

    public Pig(int x, int y, int width, int height, String image) {
        id = UUID.randomUUID().toString();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }
    public Pig(String info) {
        updateToString(info);
    }
    public void step(double dt) {
        double xMov = Math.cos(angle)*3;
        double yMov = Math.sin(angle)*3;
        angle+=.02;
        setX((int)(getX()+xMov));
        setY((int)(getY()+yMov));
    }

    @Override
    public String toString() {
        return "[" + id + ";" + x + ";" + y + ";" + width + ";" + height + ";" + image + ";" + angle + "]";
    }

    @Override
    public void updateToString(String info) {
        if(info.length() < 2) return;
        String[] split = info.substring(1, info.length() - 1).split(";");
        id = split[0];
        x = Double.parseDouble(split[1]);
        y = Double.parseDouble(split[2]);
        width = Integer.parseInt(split[3]);
        height = Integer.parseInt(split[4]);
        image = split[5];
        angle = Double.parseDouble(split[6]);
    }
}
