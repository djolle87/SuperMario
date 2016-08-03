package sge;

import game.GameOverLost;
import game.GameOverWin;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;
import game.MainMenu;
import java.awt.Color;
import java.awt.Toolkit;

public class SGEMotor extends JComponent {

    static final Logger log = Logger.getLogger(SGEMotor.class.getName());   // fires log messages
    static int WIDTH, HEIGHT;   // JFrame size
    public static SGEMotor motor;   // static field
    long lastUpdate = System.nanoTime();    // ns for clock
    static int LEFT = 37, RIGHT = 39, DOWN = 40, UP = 38, FIRE = 32;    // Arrow keys
    static int LEFTK = 65, UPK = 87, DOWNK = 83, RIGHTK = 68, FIREK = 32;   // WASD keys
    Set<Integer> keys = new HashSet<Integer>(); // Set of currently pressed keys
    public static Drawable stage; // Game stage
    public static GameOverWin end = new GameOverWin();
    public static JFrame window;
    public static Timer t;

    public boolean isKeyPressed(int key) {  // cheks if key is allready pressed
        return keys.contains(key);
    }

    /*
        method that starts engine
     */
    public static void ignite() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        log.log(Level.INFO, "SGE is starting...");
        WIDTH = 640;
        HEIGHT = 480;
        window = new JFrame("Super Mario 1.2-a1");   // Main jFrame window
        window.setIconImage(Toolkit.getDefaultToolkit().getImage(SGEMotor.class.getResource("/resources/icon.png")));
        window.setSize(WIDTH, HEIGHT);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);

        window.setBackground(new Color(72, 161, 251));
        window.setResizable(false);
        window.setVisible(true);
        motor = new SGEMotor();
        motor.stage = new Stage("level1.txt");
        motor.setSize(WIDTH, HEIGHT);
        window.add(motor);  // add engine to the window
        motor.initKeyHandlers();    // initialize key handlers
        motor.initMainLoop();   // itinialize main loop
        log.log(Level.INFO, "Game is ready.");
    }

    /*
        method that initializes Key Handlers
     */
    public void initKeyHandlers() {
        log.log(Level.INFO, "Initializing key events...");
        this.requestFocusInWindow();
        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                keys.add(e.getKeyCode());   // add KeyCode to the keys
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                keys.remove(e.getKeyCode());    // remove KeyCode to the keys
            }

        });

        log.log(Level.INFO, "Finished initializing key events...");
    }

    /*
        method that paints game stage
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.fillRect(0, 0, 100, 100);
        stage.render(g2);
    }

    /*
        method that initializes main loop
     */
    void initMainLoop() {
        log.log(Level.INFO, "Initializing main loop...");
        t = new Timer(17, (evt) -> {  // timer calls mainLoop() every 17ms (framerate ~ 60fps)
            mainLoop();
        });
        t.start();  // timer started
        log.log(Level.INFO, "Main loop initialized...");
    }

    /*
        method that updates and repaints game stage every delta nano seconds
     */
    void mainLoop() {
        long currentTime = System.nanoTime();
        double delta = ((double) currentTime - (double) lastUpdate) / 1000000000;   // elapsed time between updates in nano seconds
        try {
            stage.update(delta);
        } catch (Exception e) {
        }
        lastUpdate = currentTime;
        this.repaint();
    }

    /*
        method that switches standard game stage with game over stage 
     */
    void switchStage(String gameOver) {
        this.stage.dispose();
        this.stage = null;
        window.setVisible(false);
        window = null;
        motor = null;
        t.stop();
        game.GameOverLost goLost = new GameOverLost();
        goLost.setLocationRelativeTo(null);
        goLost.setVisible(true);
//        GameOverStage gos = new GameOverStage();
//        this.stage = gos;
    }
}
