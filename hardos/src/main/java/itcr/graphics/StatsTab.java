package itcr.graphics;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;
import com.google.gson.JsonObject;

public class StatsTab extends JPanel {
  private JTree cpuTree;
  private JPanel detailsPanel;
  private Map<Integer, Map<String, JsonObject>> cpuStats;

  public StatsTab() {
    setLayout(new BorderLayout());

    cpuTree = new JTree(new DefaultMutableTreeNode("CPUs"));
    cpuTree.setPreferredSize(new Dimension(200, 0));
    cpuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    cpuTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) cpuTree.getLastSelectedPathComponent();
        if (node == null)
          return;

        Object userObject = node.getUserObject();
        if (userObject instanceof ProcessInfo) {
          ProcessInfo processInfo = (ProcessInfo) userObject;
          showProcessDetails(processInfo.cpuId, processInfo.processId);
        } else if (node.getLevel() == 2) {
          showCoreDetails(node);
        } else if (node.getLevel() == 1) {
          showCPUDetails(node);
        }
      }
    });

    detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(cpuTree),
        new JScrollPane(detailsPanel));
    splitPane.setDividerLocation(200);

    add(splitPane, BorderLayout.CENTER);
  }

  public void updateStats(Map<Integer, Map<String, JsonObject>> newStats) {
    this.cpuStats = newStats;
    updateTree();
  }

  private void updateTree() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("CPUs");
    for (Map.Entry<Integer, Map<String, JsonObject>> cpuEntry : cpuStats.entrySet()) {
      DefaultMutableTreeNode cpuNode = new DefaultMutableTreeNode("CPU " + cpuEntry.getKey());
      Map<Integer, DefaultMutableTreeNode> coreNodes = new HashMap<>();
      for (int i = 0; i < 5; i++) {
        coreNodes.put(i, new DefaultMutableTreeNode("Core " + i));
        cpuNode.add(coreNodes.get(i));
      }
      for (Map.Entry<String, JsonObject> processEntry : cpuEntry.getValue().entrySet()) {
        JsonObject processStats = processEntry.getValue();
        int coreId = processStats.get("coreId").getAsInt();
        ProcessInfo processInfo = new ProcessInfo(cpuEntry.getKey(), processEntry.getKey());
        DefaultMutableTreeNode processNode = new DefaultMutableTreeNode(processInfo);
        coreNodes.get(coreId).add(processNode);
      }
      root.add(cpuNode);
    }
    ((DefaultTreeModel) cpuTree.getModel()).setRoot(root);
  }

  private void showProcessDetails(int cpuId, String processId) {
    detailsPanel.removeAll();
    JsonObject stats = cpuStats.get(cpuId).get(processId);
    if (stats != null) {
      addDetailLabel("CPU: " + stats.get("cpuId").getAsInt());
      addDetailLabel("Core: " + stats.get("coreId").getAsInt());
      addDetailLabel("Process ID: " + stats.get("processId").getAsInt());
      addDetailLabel("Start Time: " + stats.get("startTime").getAsString());
      addDetailLabel("Finish Time: " + stats.get("finishTime").getAsString());
      addDetailLabel("Total Core Usage Time: " + stats.get("totalCoreUsageTime").getAsString() + " seconds");
    }
    detailsPanel.revalidate();
    detailsPanel.repaint();
  }

  private void addDetailLabel(String text) {
    JLabel label = new JLabel(text);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    detailsPanel.add(label);
    detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
  }

  private void showCoreDetails(DefaultMutableTreeNode coreNode) {
    detailsPanel.removeAll();
    int processCount = coreNode.getChildCount();
    JLabel coreLabel = new JLabel("Core: " + coreNode.getUserObject());
    coreLabel.setFont(coreLabel.getFont().deriveFont(Font.BOLD, 14f));
    detailsPanel.add(coreLabel);
    detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    JLabel processCountLabel = new JLabel("Number of processes: " + processCount);
    detailsPanel.add(processCountLabel);
    detailsPanel.revalidate();
    detailsPanel.repaint();
  }

  private void showCPUDetails(DefaultMutableTreeNode cpuNode) {
    detailsPanel.removeAll();
    int totalProcesses = 0;
    for (int i = 0; i < cpuNode.getChildCount(); i++) {
      DefaultMutableTreeNode coreNode = (DefaultMutableTreeNode) cpuNode.getChildAt(i);
      totalProcesses += coreNode.getChildCount();
    }
    JLabel cpuLabel = new JLabel("CPU: " + cpuNode.getUserObject());
    cpuLabel.setFont(cpuLabel.getFont().deriveFont(Font.BOLD, 14f));
    detailsPanel.add(cpuLabel);
    detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    JLabel coreCountLabel = new JLabel("Number of cores: " + cpuNode.getChildCount());
    detailsPanel.add(coreCountLabel);
    detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    JLabel processCountLabel = new JLabel("Total number of processes: " + totalProcesses);
    detailsPanel.add(processCountLabel);
    detailsPanel.revalidate();
    detailsPanel.repaint();
  }

  private static class ProcessInfo {
    int cpuId;
    String processId;

    ProcessInfo(int cpuId, String processId) {
      this.cpuId = cpuId;
      this.processId = processId;
    }

    @Override
    public String toString() {
      return "Process " + processId;
    }
  }
}