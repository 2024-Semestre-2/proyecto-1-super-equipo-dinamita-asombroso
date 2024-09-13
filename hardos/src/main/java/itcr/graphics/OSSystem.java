package itcr.graphics;

import javax.swing.*;
import java.awt.*;

import itcr.controllers.DesktopScreenController;
import itcr.controllers.OSSystemController;

public class OSSystem extends JFrame {
  private CardLayout cardLayout;
  private JPanel container;
  private OSSystemController controller = new OSSystemController(this);
  private DesktopScreenController desktopController  = new DesktopScreenController(this);

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

  public void showLoginScreen() {
    cardLayout.show(container, "LoginScreen");
  }

  public void showDesktopScreen() {
    cardLayout.show(container, "DesktopScreen");
  }

  public OSSystemController getController() {
    return controller;
  }
}