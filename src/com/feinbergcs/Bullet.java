package com.feinbergcs;

import java.util.UUID;

public class Bullet extends Sprite {
    public double vx;
    public double vy;
    public double speed;

    public Bullet(int x, int y, double vx, double vy, double angle, double speed, int size) {
        super.width = size;
        super.height = size;
        this.speed = speed;
        super.id = UUID.randomUUID().toString();
        super.x = x;
        super.y = y;
        super.angle = angle;
        super.image = "/bullet.png";
        this.vx = vx * Math.cos(angle);
        this.vy = vy * Math.sin(angle);
    }

    public Bullet(Sprite from, double vx, double vy, double speed, boolean rightHand, int size) {
        super.width = size;
        super.height = size;
        double handX = (double)from.height * 3 / 8;
        double handY = (double)from.width * 3 / 8 * (rightHand ? 1 : -1);
        double rotatedHandX = handX * Math.cos(from.angle) - handY * Math.sin(from.angle) + from.x + (double)from.width / 2 - (double)width;
        double rotatedHandY = handX * Math.sin(from.angle) + handY * Math.cos(from.angle) + from.y + (double)from.height / 2 - (double)height;
        this.x = (int)rotatedHandX;
        this.y = (int)rotatedHandY;
        this.vx = vx * Math.cos(from.angle);
        this.vy = vy * Math.sin(from.angle);
        this.speed = speed;
        super.id = UUID.randomUUID().toString();
        super.image = "/bullet.png";
        this.angle = from.angle;
    }

    public Bullet(String info) {
        updateToString(info);
    }

    @Override
    public void step(double deltatime) {
        x += vx * deltatime * speed;
        y += vy * deltatime * speed;
    }

    @Override
    public String toString() {
        return "[" + id + ";" + x + ";" + y + ";" + width + ";" + height + ";" + image + ";" + angle + ";" + vx + ";" + vy + ";" + speed + "]";
    }

    @Override
    public void updateToString(String info) {
        if(info.length() < 2) return;
        String[] split = info.substring(2, info.length() - 2).split(";");
        if(id == null) id = UUID.fromString(split[0]).toString();
        x = Double.parseDouble(split[1]);
        y = Double.parseDouble(split[2]);
        width = Integer.parseInt(split[3]);
        height = Integer.parseInt(split[4]);
        image = split[5];
        angle = Double.parseDouble(split[6]);
        vx=Double.parseDouble(split[7]);
        vy=Double.parseDouble(split[8]);
        speed = Double.parseDouble(split[9]);
    }
}
