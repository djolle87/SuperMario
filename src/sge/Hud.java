package sge;

import java.awt.Font;
import java.awt.Graphics2D;

public class Hud extends Drawable { 
    
    String itemsCollected = "0";
    String itemsCount = "0";
    String lives = "0";
    
    Font itemsFont = new Font("Arial", Font.BOLD, 24);
    
    public Hud(){
        
    }
    
    @Override
    public void render(Graphics2D g) {
        g.setFont(itemsFont); 
        int textw = g.getFontMetrics(itemsFont).stringWidth("Coins: " + itemsCollected + " / " + itemsCount); 
        g.drawString("Coins: " + itemsCollected + " / " + itemsCount, SGEMotor.WIDTH-textw-20,30);
        g.drawString("Lives: " + lives, 20,30);
    }

    @Override
    public void update(double delta) {
        
    }
    
}
