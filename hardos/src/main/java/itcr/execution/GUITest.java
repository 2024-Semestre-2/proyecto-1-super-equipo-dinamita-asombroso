package itcr.execution;

import javax.swing.SwingUtilities;
import itcr.graphics.OSSystem;

public class GUITest {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      OSSystem osSystem = new OSSystem();
      osSystem.setVisible(true);
      osSystem.showLoginScreen();
    });
  }
}
