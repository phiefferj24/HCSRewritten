package com.feinbergcs;

import java.util.UUID;

public class Zombie extends Sprite{

    private boolean seesPlayer = false;
    double t;
    int gotoX;
    int gotoY;

    public Zombie(String info) {
        updateToString(info);
    }
    public Zombie(int x, int y, int width, int height, String image) {
        id = UUID.randomUUID();
        gotoX=(int)(x+(Math.random()*100)-50);
        gotoY=(int)(y+(Math.random()*100)-50);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        t=0;
    }


    @Override
    public void step(double dt) {
        double speed = 1.5;
        System.out.println(t);
        if(!seesPlayer)
        {
            if(t<150) {
                System.out.println(gotoX);
                System.out.println(gotoY);
                t += dt;
                double x =  (gotoX) - (this.x);
                double y = (gotoY) - (this.y);

                double theta = Math.atan2(gotoY,gotoX);
                setX((int)(getX()+speed*Math.cos(theta)));
                setY((int)(getY()+speed*Math.sin(theta)));
            }
            else
            {
                gotoX=(int)(x+(Math.random()*100)-50);
                gotoY=(int)(y+(Math.random()*100)-50);
                t=0;
            }

        }
    }

    @Override
    public String toString() {
        return "[" + id + ";" + x + ";" + y + ";" + width + ";" + height + ";" + image + ";" + angle + ";" + gotoX +";" + gotoY +"]";
    }

    @Override
    public void updateToString(String info) {
        if(info.length() < 2) return;
        String[] split = info.substring(1, info.length() - 1).split(";");
        id = UUID.fromString(split[0]);
        x = Double.parseDouble(split[1]);
        y = Double.parseDouble(split[2]);
        width = Integer.parseInt(split[3]);
        height = Integer.parseInt(split[4]);
        image = split[5];
        angle = Double.parseDouble(split[6]);
        gotoX = Integer.parseInt(split[7]);
        gotoY = Integer.parseInt(split[8]);
    }

}
