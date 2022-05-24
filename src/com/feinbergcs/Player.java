package com.feinbergcs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Player extends Sprite {
    private double speed = 1;

    public Player(int x, int y, int width, int height, String image) {
        id = UUID.randomUUID();
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
        return "[" + id + ";" + x + ";" + y + ";" + width + ";" + height + ";" + image + ";" + angle + "]";
    }

    @Override
    public void updateToString(String info) {
        if(info.length() < 2) return;
        String[] split = info.substring(2, info.length() - 2).split(";");
        id = UUID.fromString(split[0]);
        x = Integer.parseInt(split[1]);
        y = Integer.parseInt(split[2]);
        width = Integer.parseInt(split[3]);
        height = Integer.parseInt(split[4]);
        image = split[5];
        angle = Double.parseDouble(split[6]);
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
