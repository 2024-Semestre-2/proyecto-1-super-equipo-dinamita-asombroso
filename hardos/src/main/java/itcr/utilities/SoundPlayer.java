package itcr.utilities;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
        FileInputStream fileInputStream = new FileInputStream(soundFileName);
        Player player = new Player(fileInputStream);
        player.play();
      } catch (FileNotFoundException | JavaLayerException e) {
        e.printStackTrace();
      }
    }).start();
  }
}