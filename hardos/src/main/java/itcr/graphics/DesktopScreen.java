package itcr.graphics;

import javax.swing.*;
import java.awt.*;

import itcr.controllers.DesktopScreenController;

public class DesktopScreen extends JPanel {
  private static final int DESKTOP_WIDTH = 1024;
  private static final int DESKTOP_HEIGHT = 768;
  private DesktopScreenController controller;

  public DesktopScreen(DesktopScreenController controller) {
    this.controller = controller;
    setLayout(null);
    setPreferredSize(new Dimension(DESKTOP_WIDTH, DESKTOP_HEIGHT));

    ImageIcon backgroundIcon = new ImageIcon("xp_home_background.jpg");
    Image backgroundImg = backgroundIcon.getImage().getScaledInstance(DESKTOP_WIDTH, DESKTOP_HEIGHT,
        Image.SCALE_SMOOTH);
    JLabel backgroundLabel = new JLabel(new ImageIcon(backgroundImg));
    backgroundLabel.setBounds(0, 0, DESKTOP_WIDTH, DESKTOP_HEIGHT);
    add(backgroundLabel);

    addDesktopIcon("Mi PC", "xp_home_mypc_icon.png", 25, 25, backgroundLabel, "MyComputer");
    addDesktopIcon("Recicle bin", "xp_home_reciclebin_icon.png", 25, 125, backgroundLabel, "RecycleBin");
    addDesktopIcon("File explorer", "xp_home_fileexplorer_icon.png", 25, 225, backgroundLabel, "FileExplorer");
  }

  private void addDesktopIcon(String name, String iconPath, int x, int y, JLabel backgroundLabel,
      String actionCommand) {
    ImageIcon iconImage = new ImageIcon(iconPath);
    Image img = iconImage.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
    JLabel iconLabel = new JLabel(new ImageIcon(img));

    JPanel iconPanel = new JPanel();
    iconPanel.setLayout(new BorderLayout());
    iconPanel.setOpaque(false);

    iconPanel.add(iconLabel, BorderLayout.CENTER);

    JLabel textLabel = new JLabel(name, JLabel.CENTER);
    textLabel.setForeground(Color.WHITE);
    textLabel.setFont(new Font("Arial", Font.BOLD, 12));

    iconPanel.add(textLabel, BorderLayout.SOUTH);

    iconPanel.setBounds(x, y, 100, 100);
    iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    iconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        handleIconClick(actionCommand);
      }
    });

    backgroundLabel.add(iconPanel);
  }

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
      default:
        JOptionPane.showMessageDialog(null, "Acción no definida");
        break;
    }
  }
}
