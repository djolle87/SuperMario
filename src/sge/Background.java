package sge;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.text.Segment;

public class Background extends Drawable {
    
    BufferedImage image;
    BufferedImage image1; 
    double spd; 
    double x1,y1; 
    public Background(String a,String b,int off,double spd) throws IOException{
        image = ImageIO.read(getClass().getResource("/resources/"+a));
        image1 = ImageIO.read(getClass().getResource("/resources/"+b));
        x1=image.getWidth();
        y = off;
        y1 = off;
        this.spd = spd;
    }
    
    public void moveLeft(double delta){
        double step = spd*delta;
        x-=step;
        x1-=step;
        if(x+image.getWidth()<0){
            x=x1+image1.getWidth();
        }
        if(x1+image.getWidth()<0){
            x1=x+image1.getWidth();
        }
    }
    public void moveRight(double delta){
        double step = spd*delta;
        x+=step;
        x1+=step;
        if(x>SGEMotor.WIDTH){
            x=x1-image1.getWidth();
        }
        if(x1>SGEMotor.WIDTH){
            x1=x-image1.getWidth();
        }
    }

    @Override
    public void render(Graphics2D g) { 
        g.drawImage(image, (int)x, (int)y, null);
        g.drawImage(image1, (int)x1, (int)y1, null);
    }

    @Override
    public void update(double delta) { 
        
    }
    
}
