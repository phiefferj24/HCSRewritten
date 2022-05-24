package com.feinbergcs;

import java.util.UUID;

public class Bullet extends Sprite {
    public double vx;
    public double vy;
    public double speed;

    public Bullet(int x, int y, double vx, double vy, double angle, double speed) {
        this.speed = speed;
        super.id = UUID.randomUUID();
        super.x = x;
        super.y = y;
        super.angle = angle;
        super.image = "/bullet.png";
        this.vx = vx * Math.cos(angle);
        this.vy = vy * Math.sin(angle);
        super.width = 10;
        super.height = 10;
    }

    public Bullet(Sprite from, double vx, double vy, double speed, boolean rightHand) {
        double handX = (double)from.height * 3 / 8;
        double handY = 0;
        if(rightHand) {
            handY += (double)from.width * 3 / 8;
        } else {
            handY -= (double)from.width * 3 / 8;
        }
        double rotatedHandX = handX * Math.cos(from.angle) - handY * Math.sin(from.angle) + from.x + (double)from.width / 2;
        double rotatedHandY = handX * Math.sin(from.angle) + handY * Math.cos(from.angle) + from.y + (double)from.height / 2;
        this.x = (int)rotatedHandX;
        this.y = (int)rotatedHandY;
        this.vx = vx * Math.cos(from.angle);
        this.vy = vy * Math.sin(from.angle);
        this.speed = speed;
        super.id = UUID.randomUUID();
        super.image = "/bullet.png";
        super.width = 10;
        super.height = 10;
        this.angle = from.angle;
    }

    @Override
    public void step(double deltatime) {
        x += vx * deltatime * speed;
        y += vy * deltatime * speed;
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
        x = Double.parseDouble(split[1]);
        y = Double.parseDouble(split[2]);
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
