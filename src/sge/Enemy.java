package sge;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Enemy extends Drawable {

    BufferedImage sheet;
    BufferedImage currentSprite;
    public BoundingBox boundingBox = new BoundingBox(0, 0, 54, 64);
    Stage stage;
    int fpsCounter = 1;
    int frameIndex = 0;
    double spd = 100;
    double accy = 0;
    String currSequenceName = "right";
    int[][] currentSequence;
    double x1, x2;
    int dir = 1;
    boolean killed = false;
    Clip shout2Clip;
    AudioInputStream shout2InputStream;

    Map<String, int[][]> sequences = new HashMap<String, int[][]>() {
        {
            put("right", new int[][]{{0, 0}, {50, 0}, {100, 0}, {150, 0}, {200, 0}, {250, 0}, {300, 0}});
            put("left", new int[][]{{300, 64}, {250, 64}, {200, 64}, {150, 64}, {100, 64}, {50, 64}, {0, 64}});
        }
    };

    public void kill() {
        this.killed = true;
        this.accy = 10;
        shout2Clip.stop();
        shout2Clip.flush();
        shout2Clip.setFramePosition(0);
        shout2Clip.start();
    }

    public Enemy(double x1, double x2, double y, Stage stage) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        sheet = ImageIO.read(getClass().getResourceAsStream("/resources/enemy.png"));
        currentSequence = sequences.get(currSequenceName);
        currentSprite = sheet.getSubimage(currentSequence[frameIndex][0], currentSequence[frameIndex][1], this.boundingBox.w, this.boundingBox.h);
        Random r = new Random();

        this.x = (double) (r.nextInt((int) (x2 - x1)) + x1);
        this.y = y;
        this.x1 = x1;
        this.x2 = x2;
        this.stage = stage;

        InputStream shout2AudioSrc = getClass().getResourceAsStream("/resources/ahaa.wav");
        InputStream shout2BufferedIn = new BufferedInputStream(shout2AudioSrc);
        shout2InputStream = AudioSystem.getAudioInputStream(shout2BufferedIn);
        shout2Clip = AudioSystem.getClip();
        shout2Clip.open(shout2InputStream);
    }

    @Override
    public void render(Graphics2D g) {
        g.drawImage(currentSprite, (int) (this.x + stage.matrix[0]), (int) this.y, null);
    }

    @Override
    public void update(double delta) {
        this.y += this.accy;
        this.x += (this.dir * this.spd * delta);
        if (this.x > this.x2) {
            this.x = x2;
            this.currSequenceName = "left";
            this.currentSequence = this.sequences.get(this.currSequenceName);
            this.frameIndex = 0;
            this.dir *= -1;
        } else if (this.x < this.x1) {
            this.x = x1;
            this.currSequenceName = "right";
            this.currentSequence = this.sequences.get(this.currSequenceName);
            this.frameIndex = 0;
            this.dir *= -1;
        }
        if (fpsCounter++ % 4 == 0) {
            currentSprite = sheet.getSubimage(currentSequence[frameIndex][0], currentSequence[frameIndex][1], this.boundingBox.w, this.boundingBox.h);
            fpsCounter = 1;
            if (++frameIndex >= currentSequence.length) {
                frameIndex = 0;
            }
        }

    }

}
