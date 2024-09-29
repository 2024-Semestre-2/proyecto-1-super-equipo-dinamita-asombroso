package itcr.graphics;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

class ProcessTimelinePanel extends JPanel {
  private final JPanel cardsPanel;

  public ProcessTimelinePanel() {
    setLayout(new BorderLayout());

    JLabel titleLabel = new JLabel("Process Timeline", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
    add(titleLabel, BorderLayout.NORTH);

    cardsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
    cardsPanel.setBackground(new Color(245, 245, 245));

    JScrollPane scrollPane = new JScrollPane(cardsPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    // prefered size
    scrollPane.setPreferredSize(new Dimension(800, 500));
    add(scrollPane, BorderLayout.CENTER);
  }

  public void updateStats(Map<String, String> stats) {
    cardsPanel.removeAll();
    if (stats.isEmpty()) {
      cardsPanel.add(new JLabel("No active processes", SwingConstants.CENTER));
    } else {
      for (Map.Entry<String, String> entry : stats.entrySet()) {
        System.out.println("xdd");
        System.out.println(entry.getKey() + " zzz " + entry.getValue());

        // first then chars of value
        String core = entry.getValue().substring(1, 9);

        ProcessCard card = new ProcessCard(core, entry.getValue());
        cardsPanel.add(card);
      }
    }
    cardsPanel.revalidate();
    cardsPanel.repaint();
  }
}