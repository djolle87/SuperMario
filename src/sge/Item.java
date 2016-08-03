package sge;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Item extends Drawable {
    
    BufferedImage image;
    Stage stage;
    BoundingBox boundingBox = new BoundingBox(0, 0, 50, 56);
    BufferedImage currentFrameImage;
    int currentFrame = 0;
    int frameCount = 1;
    boolean collected = false;
     
    
    public Item(double x, double y, Stage stage) throws IOException{
        this.image = ImageIO.read(getClass().getResource("/resources/coins.png"));
        this.currentFrameImage = this.image.getSubimage(currentFrame*boundingBox.w, 0, boundingBox.w, boundingBox.h);
        this.stage = stage;
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(Graphics2D g) {
        if(collected) return;
        g.drawImage(this.currentFrameImage, (int)(this.x+this.stage.matrix[0]), (int)this.y, null);
    }

    @Override
    public void update(double delta) {
        if(collected) return;
        if(this.frameCount++%3!=0){
            return;
        }
        this.currentFrameImage = this.image.getSubimage(currentFrame*boundingBox.w, 0, boundingBox.w, boundingBox.h);
        if(++this.currentFrame>=4){
            this.currentFrame=0;
        }
    } 
}
