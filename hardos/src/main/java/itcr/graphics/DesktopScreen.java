package itcr.graphics;

import javax.swing.*;
import java.awt.*;

import itcr.controllers.DesktopScreenController;

/**
 * DesktopScreen class represents a custom JPanel that simulates a desktop
 * environment.
 * It includes a background image and several desktop icons.
 */
public class DesktopScreen extends JPanel {
  private static final int DESKTOP_WIDTH = 1024;
  private static final int DESKTOP_HEIGHT = 768;
  private DesktopScreenController controller;

  /**
   * Constructor for DesktopScreen.
   * Initializes the desktop with a background image and several icons.
   *
   * @param controller the controller to handle icon actions
   */
  public DesktopScreen(DesktopScreenController controller) {
    this.controller = controller;
    setLayout(null);
    setPreferredSize(new Dimension(DESKTOP_WIDTH, DESKTOP_HEIGHT));

    // Set background image
    ImageIcon backgroundIcon = new ImageIcon("xp_home_background.jpg");
    Image backgroundImg = backgroundIcon.getImage().getScaledInstance(DESKTOP_WIDTH, DESKTOP_HEIGHT,
        Image.SCALE_SMOOTH);
    JLabel backgroundLabel = new JLabel(new ImageIcon(backgroundImg));
    backgroundLabel.setBounds(0, 0, DESKTOP_WIDTH, DESKTOP_HEIGHT);
    add(backgroundLabel);

    // Add desktop icons
    addDesktopIcon("My Computer", "xp_home_mypc_icon.png", 25, 25, backgroundLabel, "MyComputer");
    addDesktopIcon("Recycle Bin", "xp_home_reciclebin_icon.png", 25, 125, backgroundLabel, "RecycleBin");
    addDesktopIcon("File Explorer", "xp_home_fileexplorer_icon.png", 25, 225, backgroundLabel, "FileExplorer");
    addDesktopIcon("Hard8086", "xp_home_hard8086.png", 25, 325, backgroundLabel, "Hard8086");
  }

  /**
   * Adds a desktop icon to the background label.
   *
   * @param name            the name of the icon
   * @param iconPath        the path to the icon image
   * @param x               the x-coordinate of the icon
   * @param y               the y-coordinate of the icon
   * @param backgroundLabel the label to which the icon is added
   * @param actionCommand   the action command associated with the icon
   */
  private void addDesktopIcon(String name, String iconPath, int x, int y, JLabel backgroundLabel,
      String actionCommand) {
    // Load and scale icon image
    ImageIcon iconImage = new ImageIcon(iconPath);
    Image img = iconImage.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
    JLabel iconLabel = new JLabel(new ImageIcon(img));

    // Create icon panel
    JPanel iconPanel = new JPanel();
    iconPanel.setLayout(new BorderLayout());
    iconPanel.setOpaque(false);

    // Add icon and text to panel
    iconPanel.add(iconLabel, BorderLayout.CENTER);
    JLabel textLabel = new JLabel(name, JLabel.CENTER);
    textLabel.setForeground(Color.WHITE);
    textLabel.setFont(new Font("Arial", Font.BOLD, 12));
    iconPanel.add(textLabel, BorderLayout.SOUTH);

    // Set panel properties
    iconPanel.setBounds(x, y, 100, 100);
    iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Add mouse listener for click actions
    iconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        handleIconClick(actionCommand);
      }
    });

    // Add icon panel to background label
    backgroundLabel.add(iconPanel);
  }

  /**
   * Handles the click action for desktop icons.
   *
   * @param actionCommand the action command associated with the clicked icon
   */
  private void handleIconClick(String actionCommand) {
    switch (actionCommand) {
      case "MyComputer":
        controller.openMyComputer();
        break;
      case "RecycleBin":
        controller.openRecycleBin();
        break;
      case "FileExplorer":
        controller.openFileExplorer();
        break;
      case "Hard8086":
        controller.openHard8086();
        break;
      default:
        JOptionPane.showMessageDialog(null, "Undefined action");
        break;
    }
  }
}