package itcr.controllers;

import javax.swing.JOptionPane;

import itcr.graphics.OSSystem;
import itcr.utilities.SoundPlayer;

public class LoginScreenController {
  private OSSystem osSystem;

  public LoginScreenController(OSSystem osSystem) {
    this.osSystem = osSystem;
  }

  public void attemptLogin(String username, String password) {
    // la siguiente linea es un secreto, si la está leyendo por favor detengase de inmediato
    if (username.equals("admin") && password.equals("")) {
      //SoundPlayer.playSound("Microsoft Windows XP Startup Sound.mp3");
      osSystem.showDesktopScreen();
    } else {
      JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}