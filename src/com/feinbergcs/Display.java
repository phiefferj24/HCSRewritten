package com.feinbergcs;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.*;
import javax.swing.*;

public class Display extends JComponent implements KeyListener, MouseListener, Runnable
{
    public interface Callback
    {
        void paintComponent(Graphics g);
        void keyPressed(int key);
        void keyReleased(int key);
        void mouseMoved(int x, int y);

    }
    private static Map<String, Image> images = new HashMap<String, Image>();

    public static Image getImage(String name)
    {
        try
        {
            Image image = images.get(name);
            if (image == null)
            {
                URL url = Display.class.getResource(name);
                if (url == null)
                    throw new RuntimeException("unable to load image:  " + name);
                image = ImageIO.read(url);
                images.put(name, image);
            }
            return image;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private JFrame frame;
    private int mouseX;
    private int mouseY;
    private Queue<KeyEvent> keys;
    private Callback callback;

    public Display(final int width, final int height, String name, Callback paintCallback)
    {
        callback = paintCallback;
        keys = new ConcurrentLinkedQueue<KeyEvent>();
        mouseX = -1;
        mouseY = -1;

        try
        {
            SwingUtilities.invokeAndWait(new Runnable() { public void run() {
                frame = new JFrame();
                frame.setTitle(name);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                setPreferredSize(new Dimension(width, height));
                setFocusable(true);
                requestFocusInWindow();
                addKeyListener(Display.this);
                addMouseListener(Display.this);
                frame.getContentPane().add(Display.this);

                frame.pack();
                frame.setVisible(true);
            }});
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void paintComponent(Graphics g)
    {
        try
        {
            callback.paintComponent(g);
        }
        catch(Exception e)
        {
            e.printStackTrace();  //show error
            setVisible(false);  //stop drawing so we don't keep getting the same error
        }
    }

    public void run()
    {
        while (true)
        {
            repaint();
            try { Thread.sleep(10); } catch(Exception e) { }

            while (!keys.isEmpty())
            {
                KeyEvent event = keys.poll();
                if (event.getID() == KeyEvent.KEY_PRESSED)
                    callback.keyPressed(event.getKeyCode());
                else if (event.getID() == KeyEvent.KEY_RELEASED)
                    callback.keyReleased(event.getKeyCode());
                else
                    throw new RuntimeException("Unexpected event type:  " + event.getID());
            }

            if (mouseX != -1)
            {
                callback.mouseMoved(mouseX, mouseY);
                mouseX = -1;
                mouseY = -1;
            }
        }
    }

    public void keyPressed(KeyEvent e)
    {
        keys.add(e);
    }

    public void keyReleased(KeyEvent e)
    {
        keys.add(e);
    }

    public void keyTyped(KeyEvent e)
    {
        //ignored
    }

    public void mousePressed(MouseEvent e)
    {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }
}