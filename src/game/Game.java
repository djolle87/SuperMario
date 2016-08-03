package game;

import java.io.IOException;
import java.util.Arrays;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Game {

    static MainMenu menu;

    public static void main(String[] args) throws IOException, LineUnavailableException, UnsupportedAudioFileException {

        menu = new MainMenu();
        menu.setLocationRelativeTo(null);
        menu.setVisible(true);
        //sge.SGEMotor.ignite();  // Start game engine
    }
}
