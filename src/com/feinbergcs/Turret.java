package com.feinbergcs;

import java.util.ArrayList;
import java.util.UUID;

public class Turret extends Sprite{

    private boolean seesZombie = false;
    double t;
    int lookatX;
    int lookatY;

    public Turret(String info) {
        updateToString(info);
    }
    public Turret(int x, int y, int width, int height, String image) {
        id = UUID.randomUUID().toString();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        t=0;
    }



    public void step(double dt, ArrayList<Sprite> sprites) {
        ArrayList<Sprite> players = new ArrayList<>();

        double speed = 5;
        double zombieCX = 0;
        double zombieCY = 0;

        double cx = this.x +width/2;
        double cy = this.y +height/2;


        if(sprites == null)
            return;
        for(int i = 0; i < sprites.size(); i++)
            if((sprites.get(i) instanceof Player)) {
                players.add(sprites.get(i));
            }
        for(Sprite s: sprites)
        {

            seesZombie = false;

            if(s instanceof Player)
            {
                zombieCX = s.getX()+s.getWidth()/2;
                zombieCY = s.getY()+s.getHeight()/2;
                if(Math.sqrt((zombieCX-cx)*(zombieCX-cx)+(zombieCY-cy)*(zombieCY-cy))<300) {

                    seesZombie = true;
                    break;
                }
            }
        }

        if(!seesZombie)
        {
            angle+=.001*dt;

        }
        else
        {
            lookatX=(int)zombieCX;
            lookatY =(int)zombieCY;
            t=0;
        }


        double x =  (lookatX) - (cx);
        double y = (lookatY) - (cy);

        double theta = Math.atan2(y,x);
        setAngle(theta-90);
    }

    @Override
    public void step(double deltatime) {
        step(deltatime,null);
    }

    @Override
    public String toString() {
        return "[" + id + ";" + x + ";" + y + ";" + width + ";" + height + ";" + image + ";" + angle + ";" + lookatX +";" + lookatY +"]";
    }

    @Override
    public void updateToString(String info) {
        if(info.length() < 2) return;
        String[] split = info.substring(1, info.length() - 1).split(";");
        id = split[0];
        x = Double.parseDouble(split[1]);
        y = Double.parseDouble(split[2]);
        width = Integer.parseInt(split[3]);
        height = Integer.parseInt(split[4]);
        image = split[5];
        angle = Double.parseDouble(split[6]);
        lookatX = Integer.parseInt(split[7]);
        lookatY = Integer.parseInt(split[8]);
    }

}
