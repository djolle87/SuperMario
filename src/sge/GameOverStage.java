package sge;

import java.awt.Graphics2D;

public class GameOverStage extends Drawable {

    @Override
    public void render(Graphics2D g) {
        g.drawString("game over", 300, 300);
    }

    @Override
    public void update(double delta) {
        
    }
    
}
