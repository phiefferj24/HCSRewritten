package com.feinbergcs;

public interface Sprite {
    public void setImage(String image);
    public void setX(int x);
    public void setY(int y);
    public void setWidth(int width);
    public void setHeight(int height);
    public String getImage();
    public int getX();
    public int getY();
    public int getWidth();
    public int getHeight();
    public void step(double deltatime);
}
