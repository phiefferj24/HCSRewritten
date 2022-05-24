package com.feinbergcs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Pig extends Sprite {
    private double angle = Math.random()*360;

    public Pig(int x, int y, int width, int height, String image) {
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
        return "[" + x + "," + y + "," + width + "," + height + "," + image + "]";
    }

    @Override
    public void updateToString(String info) {
        String[] split = info.split(",");
        x = Integer.parseInt(split[0]);
        y = Integer.parseInt(split[1]);
        width = Integer.parseInt(split[2]);
        height = Integer.parseInt(split[3]);
        image = split[4];
    }
}
