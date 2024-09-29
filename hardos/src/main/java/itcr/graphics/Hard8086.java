package itcr.graphics;

import itcr.controllers.DesktopScreenController;
import itcr.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Hard8086 class represents a custom floating window that simulates the
 * Hard8086 environment.
 * It includes registers, a console, and a memory map.
 */
public class Hard8086 extends FloatingWindow<Scheduler> {
  private Map<Integer, JTextArea> registersAreas;
  private JTextArea consoleArea;
  private JTextField inputField;
  private JTree memoryMapTree;
  private ExecutorService interruptExecutor;
  private final int NUM_CORES = 5;
  private boolean firstStep = true;
  public DesktopScreenController desktopScreenControllerRef = null;
  private ProcessTimelinePanel statsPanel;

  private JTabbedPane tabbedPane;

  /**
   * Constructor for Hard8086.
   * Initializes the Hard8086 simulator window.
   *
   * @param parent    the parent frame
   * @param scheduler the scheduler to manage the simulation
   */
  public Hard8086(JFrame parent, Scheduler scheduler) {
    super(parent, "Hard8086 Simulator", scheduler);
    setSize(950, 600);

    interruptExecutor = Executors.newSingleThreadExecutor();
    startInterruptHandler();
  }

  /**
   * Initializes the components of the Hard8086 simulator.
   */
  @Override
  protected void initComponents() {
    JTabbedPane tabbedPane = new JTabbedPane();

    // Main Tab
    JPanel mainPanel = createMainTab();
    tabbedPane.addTab("Main", mainPanel);

    // Stats Tab
    statsPanel = new ProcessTimelinePanel();
    statsPanel.setVisible(true);
    statsPanel.setPreferredSize(new Dimension(800, 500));
    tabbedPane.addTab("Stats", new JScrollPane(statsPanel));

    this.tabbedPane = tabbedPane;

    add(tabbedPane, BorderLayout.CENTER);
  }

  /**
   * Creates the main tab panel.
   *
   * @return the main tab panel
   */
  private JPanel createMainTab() {
    JPanel mainPanel = new JPanel(new BorderLayout());

    // Left Panel (Registers and Console)
    JPanel leftPanel = new JPanel(new BorderLayout());

    // Registers Panel
    JPanel registersPanel = new JPanel(new GridLayout(1, NUM_CORES));
    registersAreas = new HashMap<>();

    for (int i = 0; i < NUM_CORES; i++) {
      JTextArea registerArea = new JTextArea(10, 15);
      registerArea.setEditable(false);
      registerArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
      registersAreas.put(i, registerArea);
      JScrollPane scrollPane = new JScrollPane(registerArea);
      scrollPane.setBorder(BorderFactory.createTitledBorder("Core " + i + " Registers"));
      registersPanel.add(scrollPane);
    }

    leftPanel.add(registersPanel, BorderLayout.NORTH);

    // Console Area
    consoleArea = new JTextArea(10, 50);
    consoleArea.setEditable(false);
    consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
    consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
    leftPanel.add(consoleScrollPane, BorderLayout.CENTER);

    // Right Panel (Memory Map)
    memoryMapTree = new JTree();
    memoryMapTree.setRootVisible(false);
    expandAllNodes(memoryMapTree, 0, -1);
    JScrollPane memoryMapScrollPane = new JScrollPane(memoryMapTree);
    memoryMapScrollPane.setBorder(BorderFactory.createTitledBorder("Memory Map"));

    // Split Pane
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, memoryMapScrollPane);
    splitPane.setResizeWeight(0.6);
    mainPanel.add(splitPane, BorderLayout.CENTER);

    // Control Panel
    JPanel controlPanel = new JPanel(new BorderLayout());
    inputField = new JTextField(20);
    controlPanel.add(inputField, BorderLayout.CENTER);

    // Button Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton executeButton = new JButton("Execute");
    JButton stepButton = new JButton("Step");
    JButton cleanButton = new JButton("Clean");
    JButton loadFilesButton = new JButton("Load Files");
    JButton updateMemoryMapButton = new JButton("Update Memory Map");

    executeButton.addActionListener(this::executeAllInstructions);
    stepButton.addActionListener(this::executeNextInstruction);
    cleanButton.addActionListener(this::cleanConsole);
    loadFilesButton.addActionListener(this::loadFiles);
    updateMemoryMapButton.addActionListener(this::updateMemoryMap);

    buttonPanel.add(executeButton);
    buttonPanel.add(stepButton);
    buttonPanel.add(cleanButton);
    buttonPanel.add(loadFilesButton);
    buttonPanel.add(updateMemoryMapButton);

    controlPanel.add(buttonPanel, BorderLayout.SOUTH);

    mainPanel.add(controlPanel, BorderLayout.SOUTH);

    updateRegistersDisplay();
    updateMemoryMap(null);

    return mainPanel;
  }

  private void updateStatsTab() {
    SwingUtilities.invokeLater(() -> {
      Map<String, String> currentStatsForProcesses = controller.cpu.statsForProcesses;
      statsPanel.updateStats(currentStatsForProcesses);
    });
  }

  /**
   * Updates the memory map displayed in the memory map tree.
   *
   * @param e the action event
   */
  private void updateMemoryMap(ActionEvent e) {
    MemoryMap memoryMap = controller.getMemoryManager().getMainMemoryMap();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Memory");

    addMemorySection(root, memoryMap.kernel);
    addMemorySection(root, memoryMap.os);
    addMemorySection(root, memoryMap.userSpace);
    addMemorySection(root, memoryMap.secondaryStorage);

    DefaultMutableTreeNode processesNode = new DefaultMutableTreeNode("Allocated Processes");
    for (MemoryMap.MemorySection process : memoryMap.allocatedProcesses) {
      addMemorySection(processesNode, process);
    }
    root.add(processesNode);

    DefaultMutableTreeNode freeSpacesNode = new DefaultMutableTreeNode("Free Spaces");
    for (MemoryMap.MemorySection freeSpace : memoryMap.freeSpaces) {
      addMemorySection(freeSpacesNode, freeSpace);
    }
    root.add(freeSpacesNode);

    DefaultMutableTreeNode stacksNode = new DefaultMutableTreeNode("Allocated Stacks");
    for (MemoryMap.MemorySection stack : memoryMap.allocatedStacks) {
      addMemorySection(stacksNode, stack);
    }
    root.add(stacksNode);

    DefaultMutableTreeNode stringsNode = new DefaultMutableTreeNode("Stored Strings");
    for (MemoryMap.MemorySection string : memoryMap.storedStrings) {
      addMemorySection(stringsNode, string);
    }
    root.add(stringsNode);

    DefaultMutableTreeNode filesNode = new DefaultMutableTreeNode("Stored Files");
    for (MemoryMap.MemorySection file : memoryMap.storedFiles) {
      addMemorySection(filesNode, file);
    }
    root.add(filesNode);

    memoryMapTree.setModel(new DefaultTreeModel(root));
    expandAllNodes(memoryMapTree, 0, -1);
  }

  /**
   * Adds a memory section to the parent node in the memory map tree.
   *
   * @param parent  the parent node
   * @param section the memory section to add
   */
  private void addMemorySection(DefaultMutableTreeNode parent, MemoryMap.MemorySection section) {
    StringBuilder nodeText = new StringBuilder();
    nodeText.append(String.format("%s (%d - %d)", section.name, section.start, section.end));

    if (section.additionalInfo != null && !section.additionalInfo.isEmpty()) {
      nodeText.append(" | ").append(section.additionalInfo);
    }

    DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeText.toString());
    parent.add(node);

    for (MemoryMap.MemorySection subSection : section.subSections) {
      addMemorySection(node, subSection);
    }
  }

  /**
   * Loads files into the memory.
   *
   * @param e the action event
   */
  private void loadFiles(ActionEvent e) {
    FileLoaderDialog dialog = new FileLoaderDialog((JFrame) SwingUtilities.getWindowAncestor(this), controller);
    dialog.setVisible(true);
    List<String> selectedFiles = dialog.getSelectedFiles();

    if (!selectedFiles.isEmpty()) {
      StringBuilder sb = new StringBuilder("Selected files:\n");
      for (String fileName : selectedFiles) {
        String strInstructions = controller.getFileContent(fileName);
        String assemblerErrors = Assembler.validateFormat(strInstructions);
        if (assemblerErrors != null) {
          JOptionPane.showMessageDialog(this, assemblerErrors, "Error " + fileName, JOptionPane.ERROR_MESSAGE);
          return;
        }

        String[] instructions = strInstructions.split("\n");
        itcr.model.Process process = createProcess(instructions);
        for (String instruction : instructions) {
          controller.getMemoryManager().storeInstruction("P" + process.getProcessId(), instruction);
        }
        controller.addProcess(process);
        sb.append(fileName).append("\n");
      }
    }
  }

  /**
   * Creates a process from the given instructions.
   *
   * @param instructions the instructions for the process
   * @return the created process
   */
  private itcr.model.Process createProcess(String[] instructions) {
    int processSize = 0;
    for (String instruction : instructions) {
      if (instruction.substring(0, 2).equals("//")) {
        continue;
      }
      processSize += instruction.split("//")[0].length();
    }

    String processId = "P" + itcr.model.Process.processCounter;

    int baseAddress = this.controller.getMemoryManager().allocateMemory(processId, processSize);

    for (int i = 0; i < instructions.length; i++) {
      if (instructions[i].substring(0, 2).equals("//")) {
        continue;
      }

      controller.getMemoryManager().storeInstruction(processId, instructions[i].split("//")[0]);
    }

    int qtyInstructions = controller.getMemoryManager().getQtyInstructions(processId);
    itcr.model.Process process = new itcr.model.Process(qtyInstructions);

    ProcessControlBlock pcb = new ProcessControlBlock(itcr.model.Process.processCounter++, baseAddress, processSize, 1);
    if (!controller.getMemoryManager().storeBCP(processId, pcb.toJsonString())) {
      System.out.println("Error storing BCP");
    }
    process.setPCB(pcb);
    if (!controller.getMemoryManager().allocateStack(processId)) {
      System.out.println("xx >>> Error allocating stack");
    }

    return process;
  }

  /**
   * Starts the interrupt handler.
   */
  private void startInterruptHandler() {
    interruptExecutor.submit(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          InterruptMessage message = InterruptQueue.takeMessage();
          handleInterrupt(message);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }

  /**
   * Handles an interrupt message.
   *
   * @param message the interrupt message
   */
  private void handleInterrupt(InterruptMessage message) {
    SwingUtilities.invokeLater(() -> {
      switch (message.getCode()) {
        case _10H:
          printToConsole(message.getMessage());
          break;
        case _09H:
        case _08H:
          String input = readFromConsole();
          UserInputHandler.provideInput(message.getProcessId(), input);
          updateRegistersDisplay();
          break;
        default:
          break;
      }
    });
  }

  /**
   * Executes all instructions.
   *
   * @param e the action event
   */
  private void executeAllInstructions(ActionEvent e) {
    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        int count = 10;
        while (count != 0 && !isCancelled()) {
          executeNextInstructionInBackground();
          Thread.sleep(1000);
          count--;
        }
        return null;
      }

      @Override
      protected void done() {
        SwingUtilities.invokeLater(() -> {
          consoleArea.append("All instructions executed==\n");
          updateRegistersDisplay();
          updateMemoryMap(e);
        });
      }
    }.execute();
  }

  /**
   * Executes the next instruction in the background.
   */
  private void executeNextInstructionInBackground() {
    try {
      if (true) { // Change this condition as needed
        if (firstStep) {
          firstStep = false;
          controller.executeInstruction();
        }
        controller.executeInstruction();
        SwingUtilities.invokeLater(this::updateGUI);
      }
    } catch (Exception ex) {
      final String errorMessage = ex.getMessage();
      SwingUtilities.invokeLater(() -> consoleArea.append("Error: " + errorMessage + "\n"));
    }
  }

  /**
   * Updates the GUI components.
   */
  private void updateGUI() {
    updateRegistersDisplay();
    updateMemoryMap(null);
    updateStatsTab();
  }

  /**
   * Executes the next instruction.
   *
   * @param e the action event
   */
  private synchronized void executeNextInstruction(ActionEvent e) {
    try {
      if (true) {
        if (firstStep) {
          this.firstStep = false;
          controller.executeInstruction();
        }
        controller.executeInstruction();
        updateRegistersDisplay();
        updateMemoryMap(e);
        updateStatsTab();
      }
    } catch (Exception ex) {
      System.out.println("Error: " + ex.getMessage());
      consoleArea.append("Error: " + ex.getMessage() + "\n");
    }
  }

  /**
   * Cleans the console area.
   *
   * @param e the action event
   */
  private void cleanConsole(ActionEvent e) {
    consoleArea.setText("");
  }

  /**
   * Updates the display of registers.
   */
  private void updateRegistersDisplay() {
    for (int i = 0; i < NUM_CORES; i++) {
      JTextArea area = registersAreas.get(i);
      area.setText(controller.getRegisters(i) + "\n" + controller.getRegisters(0, i));
    }
  }

  /**
   * Expands all nodes in the JTree.
   *
   * @param tree          the JTree to expand
   * @param startingIndex the starting index
   * @param rowCount      the row count
   */
  private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
    for (int i = startingIndex; i < rowCount; ++i) {
      tree.expandRow(i);
    }

    if (tree.getRowCount() != rowCount) {
      expandAllNodes(tree, rowCount, tree.getRowCount());
    }
  }

  /**
   * Prints a message to the console area.
   *
   * @param message the message to print
   */
  public void printToConsole(String message) {
    consoleArea.append(message + "\n");
  }

  /**
   * Reads input from the console.
   *
   * @return the input from the console
   */
  public String readFromConsole() {
    return JOptionPane.showInputDialog(this, "Enter input:");
  }

  /**
   * Disposes the Hard8086 window and shuts down the interrupt executor.
   */
  @Override
  public void dispose() {
    interruptExecutor.shutdownNow();
    controller.reset();
    desktopScreenControllerRef.changeScheduler(controller);
    super.dispose();
  }
}