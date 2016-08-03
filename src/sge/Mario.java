package sge;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Mario extends Drawable {

    BufferedImage sheet;
    BufferedImage frame;
    public BoundingBox boundingBox = new BoundingBox(0, 0, 60, 99);
    String activeSequence = "runLeft";
    int dir = 1;
    int posInSequence = 0;
    int spriteSheetCount = 1;
    int[][] activeSequenceArray;

    // audio streams
    AudioInputStream jumpInputStream;
    Clip jumpClip;
    AudioInputStream dieInputStream;
    Clip dieClip;

    boolean jumping = true;
    boolean falling = true;
    BoundingBox platformDocked = null;  // bounding box of platform

    double accUp = 0;
    double gravity = 10;
    double accDown = 0;
    boolean locked = false; // true when Mario is dead
    Stage stage;

    /*
        Animation - sequences of sprite sheets
     */
    Map<String, int[][]> sequences = new HashMap<String, int[][]>() {
        {
            put("standRight", new int[][]{{0, 0}});
            put("standLeft", new int[][]{{520, 107}});
            put("runRight", new int[][]{{65, 0}, {130, 0}, {195, 0}, {260, 0}, {325, 0}, {390, 0}, {455, 0}, {520, 0}});
            put("runLeft", new int[][]{{520, 107}, {455, 107}, {390, 107}, {325, 107}, {260, 107}, {195, 107}, {130, 107}, {65, 107}});
            put("jumpRight", new int[][]{{65, 99}, {130, 99}, {195, 99}, {260, 99}});
            put("jumpLeft", new int[][]{{520, 0}, {455, 0}, {390, 0}, {325, 0}});
        }
    };

    /*
        Mario construcor
     */
    public Mario(Stage stage) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        this.sheet = ImageIO.read(getClass().getResourceAsStream("/resources/mario.png"));  // Mario's sprite sheet
        this.activeSequenceArray = sequences.get(activeSequence);
        this.stage = stage;

        // import audio recources
        InputStream audioSrc = getClass().getResourceAsStream("/resources/jump.wav");
        InputStream bufferedIn = new BufferedInputStream(audioSrc);
        jumpInputStream = AudioSystem.getAudioInputStream(bufferedIn);
        jumpClip = AudioSystem.getClip();
        jumpClip.open(jumpInputStream);

        InputStream dieAudioSrc = getClass().getResourceAsStream("/resources/smb_mariodie.wav");
        InputStream dieBufferedIn = new BufferedInputStream(dieAudioSrc);
        dieInputStream = AudioSystem.getAudioInputStream(dieBufferedIn);
        dieClip = AudioSystem.getClip();
        dieClip.open(dieInputStream);
    }

    /*
        renders Mario
     */
    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.red);
        g.drawImage(frame, null, (int) x, (int) y);
    }

    /*
        sets sequence 
     */
    void setSequence(String seqName) {
        if (this.activeSequence.equals(seqName)) {
            return;
        }
        this.posInSequence = 0;
        this.activeSequence = seqName;
        this.activeSequenceArray = sequences.get(seqName);
    }

    /*
        stops Mario from falling
     */
    void stopFalling() {
        accDown = 0;
        accUp = 0;
        this.falling = false;
        this.jumping = false;
    }

    /*
        Mario dies
     */
    void die() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.locked = true;
        this.jumping = true;
        this.accUp = 30;

        // play sound
        dieClip.stop();
        dieClip.flush();
        dieClip.setFramePosition(0);
        dieClip.start();

    }

    /*
        updates Mario
     */
    @Override
    public void update(double delta) {

        /*
        Check collissions with platforms 
         */
        // there is no platform and mario is not jumping
        if (this.platformDocked != null && !this.jumping) {
            if (this.x + this.boundingBox.w / 2 < platformDocked.x + this.stage.matrix[0] || this.x + this.boundingBox.w / 2 > platformDocked.x + this.stage.matrix[0] + platformDocked.w) {
                this.platformDocked = null;
                this.falling = true;
            }
        }
        // Mario is alive and he is falling
        if (!this.locked && this.falling) {
            for (BoundingBox platform : this.stage.platforms) {
                if (this.y >= (platform.y) - this.boundingBox.h
                        && this.x + this.boundingBox.w / 2 > platform.x + this.stage.matrix[0]
                        && this.x + this.boundingBox.w / 2 < platform.x + this.stage.matrix[0] + platform.w
                        && this.y + this.boundingBox.h < platform.y + platform.h) {
                    this.y = platform.y - this.boundingBox.h + 10;
                    this.platformDocked = platform;
                    stopFalling();  // Mario stops falling. He is on the platform
                }
            }
        }

        /*
        Check collisions with ground
         */
        // Mario is alive and he is falling and he is above ground
        if (!this.locked && this.falling && this.y >= SGEMotor.HEIGHT - (30 + this.boundingBox.h)) {
            this.y = SGEMotor.HEIGHT - (30 + this.boundingBox.h);
            stopFalling();
        }

        /*
        Apply gravity
         */
        if (this.falling) {
            this.y += (accDown);
            //accDown += (40 * delta);
            accDown += (80 * 0.028);
        }
        if (this.jumping) {
            if (!this.falling) {
                this.y -= accUp;
                //accUp -= (80 * delta);
                accUp -= (80 * 0.028);
                if (accUp <= 0) {
                    this.accUp = 0;
                    this.falling = true;
                }
            }
        }

        if (this.locked) {
            return;
        }

        // Animates running to the right
        if (SGEMotor.motor.isKeyPressed(SGEMotor.RIGHT) || SGEMotor.motor.isKeyPressed(SGEMotor.RIGHTK)) {
            if (x < SGEMotor.WIDTH - 30) {
                this.setSequence("runRight");
                dir = 1;
            }
        }

        // Animates running to the left
        if (SGEMotor.motor.isKeyPressed(SGEMotor.LEFT) || SGEMotor.motor.isKeyPressed(SGEMotor.LEFTK)) {
            this.setSequence("runLeft");
            dir = -1;
        }

        // Animates jumping
        if (SGEMotor.motor.isKeyPressed(SGEMotor.FIRE) || SGEMotor.motor.isKeyPressed(SGEMotor.UP) || SGEMotor.motor.isKeyPressed(SGEMotor.UPK)) {
            if (!this.jumping && !this.falling) {
                //accUp = 900 * delta;
                accUp = 900 * 0.028;
                this.jumping = true;
                if (this.activeSequence.equals("standRight")) {
                    this.setSequence("jumpRight");
                }
                if (this.activeSequence.equals("standLeft")) {
                    this.setSequence("jumpLeft");
                }
                jumpClip.stop();
                jumpClip.flush();
                jumpClip.setFramePosition(0);
                jumpClip.start();
            }
        }
        if (SGEMotor.motor.keys.size() == 0) {
            this.setSequence(dir == 1 ? "standRight" : "standLeft");
        }

        int[] pos = this.activeSequenceArray[posInSequence];
        frame = this.sheet.getSubimage(pos[0], pos[1], this.boundingBox.w, this.boundingBox.h);
        if (spriteSheetCount++ % 2 == 0) {
            if (++posInSequence >= this.activeSequenceArray.length) {
                posInSequence = 0;
            }
            spriteSheetCount = 1;
        }

    }
}
