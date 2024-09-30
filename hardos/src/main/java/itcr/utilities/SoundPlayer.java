package itcr.utilities;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.InputStream;
import java.io.BufferedInputStream;

/**
 * SoundPlayer class provides functionality to play sound files in a separate
 * thread.
 */
public class SoundPlayer {

  /**
   * Plays a sound file in a separate thread.
   *
   * @param soundFileName the name of the sound file to play
   */
  public static void playSound(String soundFileName) {
    new Thread(() -> {
      try {
        // Cargar el archivo de sonido desde el classpath
        InputStream inputStream = SoundPlayer.class.getResourceAsStream("/" + soundFileName);
        if (inputStream == null) {
          System.out.println("Sonido no encontrado: " + soundFileName);
          return;
        }

        InputStream bufferedIn = new BufferedInputStream(inputStream);

        Player player = new Player(bufferedIn);
        player.play();

      } catch (JavaLayerException e) {
        e.printStackTrace();
      }
    }).start();
  }
}
