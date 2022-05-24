package com.feinbergcs;

public abstract class Sprite {
    public int x, y, width, height;
    public String image;
    public void setImage(String image) {
        this.image = image;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
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
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public abstract void step(double deltatime);
    public abstract String toString();
    public abstract void updateToString(String info);
}
