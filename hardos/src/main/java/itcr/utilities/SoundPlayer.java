package itcr.utilities;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SoundPlayer {
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