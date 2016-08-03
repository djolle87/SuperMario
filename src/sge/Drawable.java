package sge;

import java.awt.Graphics2D;

public abstract class Drawable {
    double x,y;
    public abstract void render(Graphics2D g);
    public abstract void update(double delta);
    public void dispose(){};
}
