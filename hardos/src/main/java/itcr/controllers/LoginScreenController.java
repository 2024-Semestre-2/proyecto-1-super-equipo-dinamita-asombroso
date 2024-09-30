package itcr.controllers;

import javax.swing.JOptionPane;
import itcr.graphics.OSSystem;
import itcr.utilities.SoundPlayer;

/**
 * The LoginScreenController class manages the login process for the application.
 * It verifies user credentials and transitions to the desktop screen upon successful login.
 */
public class LoginScreenController {
  private final OSSystem osSystem;

  /**
   * Constructs a LoginScreenController with the specified OSSystem.
   *
   * @param osSystem the OSSystem instance
   */
  public LoginScreenController(OSSystem osSystem) {
    this.osSystem = osSystem;
  }

  /**
   * Attempts to log in with the provided username and password.
   *
   * @param username the username entered by the user
   * @param password the password entered by the user
   */
  public void attemptLogin(String username, String password) {
    if ("admin".equals(username) && "".equals(password)) {
      SoundPlayer.playSound("Microsoft Windows XP Startup Sound.mp3");
      osSystem.showDesktopScreen();
    } else {
      JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}