package itcr.graphics;

import javax.swing.*;
import java.awt.*;

import itcr.controllers.DesktopScreenController;
import itcr.controllers.OSSystemController;

/**
 * OSSystem class represents the main frame of the operating system.
 * It manages the display of different screens using a CardLayout.
 */
public class OSSystem extends JFrame {
  private CardLayout cardLayout;
  private JPanel container;
  private OSSystemController controller = new OSSystemController(this);
  private DesktopScreenController desktopController = new DesktopScreenController(this);

  /**
   * Constructor for OSSystem.
   * Initializes the main frame and sets up the screens.
   */
  public OSSystem() {
    setTitle("Hansol And Randall Distributed Operating System");
    setSize(1024, 768);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    cardLayout = new CardLayout();
    container = new JPanel(cardLayout);

    container.add(new LoginScreen(this), "LoginScreen");
    container.add(new DesktopScreen(desktopController), "DesktopScreen");

    add(container);
  }

  /**
   * Shows the login screen.
   */
  public void showLoginScreen() {
    cardLayout.show(container, "LoginScreen");
  }

  /**
   * Shows the desktop screen.
   */
  public void showDesktopScreen() {
    cardLayout.show(container, "DesktopScreen");
  }

  /**
   * Gets the controller for the OSSystem.
   *
   * @return the OSSystemController
   */
  public OSSystemController getController() {
    return controller;
  }
}