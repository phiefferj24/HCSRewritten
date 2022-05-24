package com.feinbergcs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Player extends Sprite {
    private double speed = 1;

    public Player(int x, int y, int width, int height, String image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }
    public Player(String info) {
        updateToString(info);
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
