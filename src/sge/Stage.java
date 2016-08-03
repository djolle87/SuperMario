package sge;

import game.GameOverWin;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import static sge.SGEMotor.motor;
import static sge.SGEMotor.window;

public class Stage extends Drawable {

    Mario mario;
    Background bg1;
    Background bg2;
    Background bg3;
    BufferedImage platform200;
    BufferedImage platform300;
    public double[] matrix = {0, 0};
    double matrixSpd = 200;
    Hud hud;     // Head-up display

    List<BoundingBox> platforms = new ArrayList<BoundingBox>(); // List of platforms in game stage
    List<Enemy> enemies = new ArrayList<Enemy>();   // List of enemies in game stage
    List<Item> items = new ArrayList<Item>();   // List of items (coins)

    //  sounds
    Clip clip;
    AudioInputStream coinInputStream;
    Clip coinClip;
    AudioInputStream audioInputStream;
    Clip shoutOneClip;
    AudioInputStream shoutOneInputStream;

    //  scores
    int lives = 3;
    int coinsMinimum = 2;
    int itemsCollected = 0;
    int itemsCount = 0;

    /*
    Stage constructors
     */
    public Stage() throws IOException {

    }

    public Stage(String descriptionFile) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        this.hud = new Hud();   // create HUD for scores
        this.bg1 = new Background("back1.png", "back1.png", -30, 100);  // create main background1 (using two overlaping images)
        //this.bg1.x = -300; ovde je bio problem
        this.bg2 = new Background("back2.png", "back2.png", SGEMotor.HEIGHT - 130, 250);    // background2 (trees)
        this.bg3 = new Background("back3.png", "back3.png", -30, 80);   // create background3 (clouds)
        this.bg3.x = 20;
        this.platform200 = ImageIO.read(getClass().getResourceAsStream("/resources/platform200.png"));  // small platform
        this.platform300 = ImageIO.read(getClass().getResourceAsStream("/resources/platform300.png"));  // big platform
        this.mario = new Mario(this);   // create Mario
        this.mario.x = SGEMotor.WIDTH / 2 - this.mario.boundingBox.w / 2;   // place Mario in the center
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/resources/" + descriptionFile))); // create reader
        String line;
        String mode = "";

        /*
        reading description file properties (positions of platforms, enemies, coins; numer of lives and coins)
         */
        while ((line = br.readLine()) != null) {
            if (line.equals("platforms")) {
                mode = "platforms";
                continue;
            } else if (line.equals("enemies")) {
                mode = "enemies";
                continue;
            } else if (line.equals("coins")) {
                mode = "coins";
                continue;
            } else if (line.equals("lives")) {
                mode = "lives";
                continue;
            } else if (line.equals("coinsMinimum")) {
                mode = "coinsMinimum";
                continue;
            }

            /*
                set platforms, enemies, coins, number of coins and lives
             */
            switch (mode) {
                case "platforms":
                    String[] platform = line.split(",");
                    platforms.add(new BoundingBox(Integer.parseInt(platform[0]), Integer.parseInt(platform[1]), Integer.parseInt(platform[2]), Integer.parseInt(platform[3])));
                    break;
                case "enemies":
                    String[] enemy = line.split(",");
                    enemies.add(new Enemy(Integer.parseInt(enemy[0]), Integer.parseInt(enemy[1]), Integer.parseInt(enemy[2]), this));
                    break;
                case "coins":
                    String[] item = line.split(",");
                    itemsCount++;
                    items.add(new Item(Integer.parseInt(item[0]), Integer.parseInt(item[1]), this));
                    break;
                case "lives":
                    this.lives = Integer.parseInt(line.toString());
                    break;
                case "coinsMinimum":
                    this.coinsMinimum = Integer.parseInt(line.toString());
                    break;
            }
        }

        // read coin .wav file, bacground theme music .wav file
        InputStream coinAudioSrc = getClass().getResourceAsStream("/resources/smb_coin.wav");
        InputStream coinBufferedIn = new BufferedInputStream(coinAudioSrc);
        coinInputStream = AudioSystem.getAudioInputStream(coinBufferedIn);
        coinClip = AudioSystem.getClip();
        coinClip.open(coinInputStream);

        InputStream shoutOneAudioSrc = getClass().getResourceAsStream("/resources/woohoo.wav");
        InputStream shoutOneBufferedIn = new BufferedInputStream(shoutOneAudioSrc);
        shoutOneInputStream = AudioSystem.getAudioInputStream(shoutOneBufferedIn);
        shoutOneClip = AudioSystem.getClip();
        shoutOneClip.open(shoutOneInputStream);

        InputStream audioSrc = getClass().getResourceAsStream("/resources/back.wav");
        InputStream bufferedIn = new BufferedInputStream(audioSrc);
        audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /*
        respawn
     */
    public void respawn() {
        this.matrix[0] = 0;
        this.matrix[1] = 0;
        mario.y = 0;
        mario.locked = false;
        mario.dir = 1;
    }

    /*
        checks for colision between bounding boxes
     */
    public boolean checkCollission(BoundingBox b1, BoundingBox b2) {
        return (Math.abs((b2.x + b2.w / 2) - (b1.x + b1.w / 2)) < (b2.w - 10)
                && Math.abs((b2.y + b2.h / 2) - (b1.y + b1.h / 2)) < (b2.h - 10));
    }

    /*
        renders game stage
     */
    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, SGEMotor.WIDTH, SGEMotor.HEIGHT);
        bg1.render(g);
        bg2.render(g);
        bg3.render(g);
        this.hud.render(g);

        for (BoundingBox platform : platforms) {
            if (platform.x + matrix[0] < 0 - platform.w || platform.x + matrix[0] > SGEMotor.WIDTH) {
                continue;
            }
            g.drawImage(platform.w > 200 ? platform300 : platform200, (int) (platform.x + matrix[0]), (int) (platform.y + matrix[1]), null);
        }

        mario.render(g);

        for (Enemy e : enemies) {
            if (e.x + matrix[0] < 0 - e.boundingBox.w || e.x + matrix[0] > SGEMotor.WIDTH) {
                continue;
            }
            e.render(g);
        }
        for (Item i : items) {
            if (i.x + matrix[0] < 0 - i.boundingBox.w || i.x + matrix[0] > SGEMotor.WIDTH) {
                continue;
            }
            i.render(g);
        }
    }

    /*
        updates game stage
     */
    @Override
    public void update(double delta) {
        if (SGEMotor.motor.isKeyPressed(SGEMotor.RIGHT)||SGEMotor.motor.isKeyPressed(SGEMotor.RIGHTK)) {
            matrix[0] -= (matrixSpd * delta);
            bg1.moveLeft(delta);
            bg2.moveLeft(delta);
            bg3.moveLeft(delta);
        }
        if (SGEMotor.motor.isKeyPressed(SGEMotor.LEFT)||SGEMotor.motor.isKeyPressed(SGEMotor.LEFTK)) {
            matrix[0] += (matrixSpd * delta);
            bg1.moveRight(delta);
            bg2.moveRight(delta);
            bg3.moveRight(delta);
        }
        for (Enemy e : enemies) {
            e.update(delta);
            if (e.killed) {
                continue;
            }
            if (!mario.locked && checkCollission(new BoundingBox((int) mario.x, (int) mario.y, mario.boundingBox.w, mario.boundingBox.h), new BoundingBox((int) (e.x + matrix[0]), (int) e.y, e.boundingBox.w, e.boundingBox.h))) {
                if (mario.falling && mario.x + (mario.boundingBox.w / 2) >= (e.x + matrix[0]) - 10 && mario.x + (mario.boundingBox.w / 2) <= (e.x + e.boundingBox.w + matrix[0]) + 10) {
                    e.kill();
                } else {
                    try {
                        mario.die();
                    } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {

                    }
                }
            }
        }
        for (Item i : items) {
            i.update(delta);
            if (!i.collected && !mario.locked && checkCollission(new BoundingBox((int) mario.x, (int) mario.y, mario.boundingBox.w, mario.boundingBox.h), new BoundingBox((int) (i.x + matrix[0]), (int) i.y, i.boundingBox.w, i.boundingBox.h))) {
                itemsCollected++;
                i.collected = true;
                coinClip.stop();
                shoutOneClip.stop();
                coinClip.flush();
                shoutOneClip.flush();
                coinClip.setFramePosition(0);
                shoutOneClip.setFramePosition(0);
                coinClip.start();
                shoutOneClip.start();
                System.out.println(itemsCollected);
            }
        }
        mario.update(delta);

        if (itemsCollected >= coinsMinimum) {
            sge.SGEMotor.stage.dispose();
            sge.SGEMotor.stage = null;
            window.setVisible(false);
            window = null;
            motor = null;
            sge.SGEMotor.t.stop();
            game.GameOverWin gow = new GameOverWin();
            gow.setVisible(true);
            gow.setLocationRelativeTo(null);
        }

        if (mario.y > SGEMotor.HEIGHT + 200) {
            lives--;
            if (lives < 0) {
                SGEMotor.motor.switchStage("gameOver");
            }
            respawn();
        }
        hud.itemsCollected = String.valueOf(itemsCollected);
        hud.itemsCount = String.valueOf(coinsMinimum);
        hud.lives = String.valueOf(lives);
    }

    /*
        dispose method - stops audio playback
     */
    public void dispose() {
        try {
            audioInputStream.close();
            coinInputStream.close();
            clip.stop();
        } catch (IOException ex) {
            Logger.getLogger(Stage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
