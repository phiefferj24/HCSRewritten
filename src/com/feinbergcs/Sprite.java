package com.feinbergcs;

import java.util.UUID;

public abstract class Sprite {
    public double x, y;
    public int width, height;
    public double angle;
    public String image;
    public UUID id;
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public void setUUID(UUID id) {
        this.id = id;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public String getImage() {
        return image;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }
    public double getAngle() {
        return angle;
    }
    public abstract void step(double deltatime);
    public abstract String toString();
    public abstract void updateToString(String info);
}
