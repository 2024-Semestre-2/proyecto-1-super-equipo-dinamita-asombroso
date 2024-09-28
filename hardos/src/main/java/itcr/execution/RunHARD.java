package itcr.execution;

import javax.swing.SwingUtilities;
import itcr.graphics.OSSystem;

/**
 * The RunHARD class serves as the entry point for the application.
 * It initializes the graphical user interface (GUI) and displays the login screen.
 */
public class RunHARD {

  /**
   * The main method is the starting point of the application.
   * It schedules a job for the event-dispatching thread to create and show the GUI.
   *
   * @param args Command-line arguments passed to the application (not used).
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      OSSystem osSystem = new OSSystem();
      osSystem.setVisible(true);
      osSystem.showLoginScreen();
    });
  }
}