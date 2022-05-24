package com.feinbergcs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Player implements Sprite {
    private int x;
    private int y;
    private int width;
    private int height;
    private String image;
    private double speed = 1;

    public Player(int x, int y, int width, int height, String image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    @Override
    public void setImage(String image) {

    }

    @Override
    public void setX(int x) {

    }

    @Override
    public void setY(int y) {

    }

    @Override
    public void setWidth(int width) {

    }

    @Override
    public void setHeight(int height) {

    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void step(double dt) {
        for (int key : Client.downKeys) {
            if (key == KeyEvent.VK_W) {
                y -= (int) (dt * speed);
            }
            if (key == KeyEvent.VK_S) {
                y += (int) (dt * speed);
            }
            if (key == KeyEvent.VK_A) {
                x -= (int) (dt * speed);
            }
            if (key == KeyEvent.VK_D) {
                x += (int) (dt * speed);
            }
        }
    }
}
