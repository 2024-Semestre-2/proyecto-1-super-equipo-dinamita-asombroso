package itcr.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

class ProcessCard extends JPanel {
  private static final Color CARD_BG = new Color(240, 248, 255);
  private static final Color HEADER_BG = new Color(70, 130, 180);
  private static final Color TEXT_COLOR = new Color(50, 50, 50);
  private static final int ARC_SIZE = 15;

  public ProcessCard(String coreIndex, String stats) {
    setLayout(new BorderLayout());
    setBackground(CARD_BG);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setMinimumSize(new Dimension(200, 150));
    setPreferredSize(new Dimension(250, 200));

    // Header
    JLabel headerLabel = new JLabel(coreIndex, SwingConstants.CENTER);
    headerLabel.setOpaque(true);
    headerLabel.setBackground(HEADER_BG);
    headerLabel.setForeground(Color.WHITE);
    headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    add(headerLabel, BorderLayout.NORTH);

    // Content
    JPanel contentPanel = new JPanel(new GridLayout(0, 1, 5, 5));
    contentPanel.setBackground(CARD_BG);

    String[] lines = stats.split("\n");
    for (String line : lines) {
      if (
        line.contains("Start time:") || 
        line.contains("Finish time:") || 
        line.contains("Total core usage time:") ||
        line.contains("Process ID:")
      ) {
        JLabel infoLabel = new JLabel(line.trim());
        infoLabel.setForeground(TEXT_COLOR);
        contentPanel.add(infoLabel);
      }
    }

    add(contentPanel, BorderLayout.CENTER);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(getBackground());
    g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC_SIZE, ARC_SIZE));
    g2d.dispose();
    super.paintComponent(g);
  }
}