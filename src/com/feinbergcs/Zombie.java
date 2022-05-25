package com.feinbergcs;

import java.util.ArrayList;
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
        id = UUID.randomUUID().toString();
        gotoX=(int)(x+(Math.random()*100)-50);
        gotoY=(int)(y+(Math.random()*100)-50);
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
        double playerCX = 0;
        double playerCY = 0;

        double cx = this.x +width/2;
        double cy = this.y +height/2;


        if(sprites == null)
            return;
        for(int i = 0; i < sprites.size(); i++)
            if((sprites.get(i) instanceof Player)) {
                players.add(sprites.get(i));
            }
        for(Sprite s: players)
        {

            seesPlayer = false;

            if(s instanceof Player)
            {
                playerCX = s.getX()+s.getWidth()/2;
                playerCY = s.getY()+s.getHeight()/2;
                if(Math.sqrt((playerCX-cx)*(playerCX-cx)+(playerCY-cy)*(playerCY-cy))<300) {

                    seesPlayer = true;
                    break;
                }
            }
        }

        if(!seesPlayer)
        {
            if(t<2500000)
                t += dt;
            else
            {
                gotoX=(int)(int)(cx+(Math.random()*100)-50);
                gotoY=(int)(int)(cy+(Math.random()*100)-50);
                t=0;
            }

        }
        else
        {
            gotoX=(int)playerCX;
            gotoY=(int)playerCY;
            t=0;
        }


        double x =  (gotoX) - (cx);
        double y = (gotoY) - (cy);

        double theta = Math.atan2(y,x);
        setAngle(theta-90);
        if(Math.sqrt((cx-gotoX)*(cx-gotoX)+(cy-gotoY)*(cy-gotoY))>20)
            setX((int)(getX()+speed*Math.cos(theta)));
        setY((int)(getY()+speed*Math.sin(theta)));
    }

    @Override
    public void step(double deltatime) {
        step(deltatime,null);
    }

    @Override
    public String toString() {
        return "[" + id + ";" + x + ";" + y + ";" + width + ";" + height + ";" + image + ";" + angle + ";" + gotoX +";" + gotoY +"]";
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
        gotoX = Integer.parseInt(split[7]);
        gotoY = Integer.parseInt(split[8]);
    }

}
